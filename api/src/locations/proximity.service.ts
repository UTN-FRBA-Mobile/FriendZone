import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { haversineDistance } from '../common/utils/haversine';
import { EventsRepository } from '../events/events.repository';
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
  constructor(
    private readonly eventsRepository: EventsRepository,
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
      this.configService.get<number>('ARRIVAL_THRESHOLD', 150);

    let arrived = participant.arrived;

    if (!participant.arrived && distance <= threshold) {
      await this.eventsRepository.updateParticipant(
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

      if (userId === event.organizerId) {
        await this.notificationsService.notifyOrganizerSelfArrived(
          event.organizerId,
          event.id,
          event.title,
        );
      } else {
        await this.notificationsService.notifyParticipantArrived(
          event.organizerId,
          user.displayName,
          event.id,
          event.title,
        );
      }
    }

    return { arrived, eventCompleted: false, participant };
  }
}
