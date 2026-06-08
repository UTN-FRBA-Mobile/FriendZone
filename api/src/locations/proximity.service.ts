import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { haversineDistance } from '../common/utils/haversine';
import { EventsRepository } from '../events/events.repository';
import { EventsService } from '../events/events.service';
import { NotificationsService } from '../notifications/notifications.service';
import { UsersRepository } from '../users/users.repository';
import { EventsGateway } from '../websocket/events.gateway';
import { Event, EventParticipant } from '../../drizzle/schema';

export interface ProximityResult {
  arrived: boolean;
  eventCompleted: boolean;
  participant?: EventParticipant;
}

@Injectable()
export class ProximityService {
  private readonly logger = new Logger(ProximityService.name);

  constructor(
    private readonly eventsRepository: EventsRepository,
    private readonly eventsService: EventsService,
    private readonly usersRepository: UsersRepository,
    private readonly notificationsService: NotificationsService,
    private readonly eventsGateway: EventsGateway,
    private readonly configService: ConfigService,
  ) {}

  async checkProximity(
    event: Event,
    userId: string,
    latitude: number,
    longitude: number,
  ): Promise<ProximityResult> {
    const participant = await this.eventsRepository.findParticipant(
      event.id,
      userId,
    );
    if (!participant) {
      return { arrived: false, eventCompleted: false };
    }

    const user = await this.usersRepository.findById(userId);
    if (!user?.locationSharingEnabled || !participant.sharingLocation) {
      return { arrived: false, eventCompleted: false, participant };
    }

    const distance = haversineDistance(
      { latitude, longitude },
      { latitude: event.latitude, longitude: event.longitude },
    );

    const threshold =
      event.arrivalThresholdM ??
      this.configService.get<number>('ARRIVAL_THRESHOLD', 500);

    let arrived = participant.arrived;
    let eventCompleted = false;

    if (!participant.arrived && distance <= threshold) {
      const updated = await this.eventsRepository.updateParticipant(
        event.id,
        userId,
        {
          arrived: true,
          arrivedAt: new Date(),
        },
      );

      arrived = true;

      this.eventsGateway.broadcastParticipantArrived(event.id, {
        userId,
        displayName: user.displayName,
        arrivedAt: new Date().toISOString(),
      });

      await this.notificationsService.notifyParticipantArrived(
        event.organizerId,
        user.displayName,
        event.id,
        event.title,
      );

      if (updated) {
        eventCompleted = await this.checkAllArrived(event);
      }
    }

    return { arrived, eventCompleted, participant };
  }

  private async checkAllArrived(event: Event): Promise<boolean> {
    const participants = await this.eventsRepository.findParticipants(
      event.id,
    );

    const sharingParticipants: EventParticipant[] = [];

    for (const p of participants) {
      const user = await this.usersRepository.findById(p.userId);
      if (user?.locationSharingEnabled && p.sharingLocation) {
        sharingParticipants.push(p);
      }
    }

    if (sharingParticipants.length === 0) {
      return false;
    }

    const allArrived = sharingParticipants.every((p) => p.arrived);

    if (allArrived && event.status !== 'completed') {
      await this.eventsService.markCompleted(event.id);

      this.eventsGateway.broadcastEventCompleted(event.id, {
        completedAt: new Date().toISOString(),
      });

      const userIds = participants.map((p) => p.userId);
      await this.notificationsService.notifyEventCompleted(
        userIds,
        event.title,
        event.id,
      );

      this.logger.log(`Event ${event.id} completed — all participants arrived`);
      return true;
    }

    return false;
  }
}
