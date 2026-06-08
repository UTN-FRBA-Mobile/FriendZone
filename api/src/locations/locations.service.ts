import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { EventsRepository } from '../events/events.repository';
import { EventsService } from '../events/events.service';
import { UsersRepository } from '../users/users.repository';
import { EventsGateway } from '../websocket/events.gateway';
import { UpdateLocationDto, UpdateSharingDto } from './dto/location.dto';
import { ProximityService } from './proximity.service';
import { toSafeUser } from '../common/utils/user.mapper';

@Injectable()
export class LocationsService {
  constructor(
    private readonly eventsRepository: EventsRepository,
    private readonly eventsService: EventsService,
    private readonly usersRepository: UsersRepository,
    private readonly proximityService: ProximityService,
    private readonly eventsGateway: EventsGateway,
  ) {}

  async updateSharing(
    eventId: string,
    userId: string,
    dto: UpdateSharingDto,
  ) {
    await this.eventsService.assertParticipant(eventId, userId);

    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (dto.enabled && !user.locationSharingEnabled) {
      throw new BadRequestException(
        'Global location sharing must be enabled first',
      );
    }

    const participant = await this.eventsRepository.updateParticipant(
      eventId,
      userId,
      { sharingLocation: dto.enabled },
    );

    if (!participant) {
      throw new NotFoundException('Participant not found');
    }

    return participant;
  }

  async updateLocation(
    eventId: string,
    userId: string,
    dto: UpdateLocationDto,
  ) {
    const event = await this.eventsService.assertParticipant(eventId, userId);

    if (event.status === 'completed' || event.status === 'cancelled') {
      throw new BadRequestException('Event is no longer active');
    }

    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    const participant = await this.eventsRepository.findParticipant(
      eventId,
      userId,
    );
    if (!participant) {
      throw new NotFoundException('Participant not found');
    }

    if (!user.locationSharingEnabled || !participant.sharingLocation) {
      throw new ForbiddenException('Location sharing is not enabled');
    }

    const updated = await this.eventsRepository.updateParticipant(
      eventId,
      userId,
      {
        lastLatitude: dto.latitude,
        lastLongitude: dto.longitude,
        lastLocationAt: new Date(),
      },
    );

    this.eventsGateway.broadcastLocationUpdated(eventId, {
      userId,
      displayName: user.displayName,
      latitude: dto.latitude,
      longitude: dto.longitude,
      updatedAt: new Date().toISOString(),
    });

    const proximity = await this.proximityService.checkProximity(
      event,
      userId,
      dto.latitude,
      dto.longitude,
    );

    return {
      participant: updated,
      proximity,
    };
  }

  async getParticipants(eventId: string, userId: string) {
    await this.eventsService.assertParticipant(eventId, userId);

    const participants =
      await this.eventsRepository.findParticipants(eventId);

    const result = await Promise.all(
      participants.map(async (p) => {
        const user = await this.usersRepository.findById(p.userId);
        if (!user) return null;
        return {
          ...p,
          user: toSafeUser(user),
        };
      }),
    );

    return result.filter(Boolean);
  }
}
