import {
  ForbiddenException,
  Inject,
  Injectable,
  NotFoundException,
  forwardRef,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { UsersRepository } from '../users/users.repository';
import { CreateEventDto, UpdateEventDto } from './dto/event.dto';
import { EventsRepository } from './events.repository';
import { Event } from '../../drizzle/schema';
import { EventsGateway } from '../websocket/events.gateway';

@Injectable()
export class EventsService {
  constructor(
    private readonly eventsRepository: EventsRepository,
    private readonly usersRepository: UsersRepository,
    private readonly configService: ConfigService,
    @Inject(forwardRef(() => EventsGateway))
    private readonly eventsGateway: EventsGateway,
  ) {}

  async create(userId: string, dto: CreateEventDto): Promise<Event> {
    const defaultThreshold = this.configService.get<number>(
      'ARRIVAL_THRESHOLD',
      500,
    );

    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    const event = await this.eventsRepository.create({
      organizerId: userId,
      title: dto.title,
      description: dto.description,
      latitude: dto.latitude,
      longitude: dto.longitude,
      address: dto.address,
      startsAt: new Date(dto.startsAt),
      arrivalThresholdM: dto.arrivalThresholdM ?? defaultThreshold,
      trackingLeadMinutes: dto.trackingLeadMinutes ?? 30,
      status: 'scheduled',
    });

    await this.eventsRepository.addParticipant({
      eventId: event.id,
      userId,
      role: 'organizer',
      sharingLocation: user.locationSharingEnabled,
    });

    return event;
  }

  async findMine(userId: string): Promise<Event[]> {
    return this.eventsRepository.findByUserId(userId);
  }

  async findById(eventId: string, userId: string): Promise<Event> {
    const event = await this.eventsRepository.findById(eventId);
    if (!event) {
      throw new NotFoundException('Event not found');
    }

    const isParticipant = await this.eventsRepository.isParticipant(
      eventId,
      userId,
    );
    if (!isParticipant) {
      throw new ForbiddenException('Not a participant of this event');
    }

    return event;
  }

  async update(
    eventId: string,
    userId: string,
    dto: UpdateEventDto,
  ): Promise<Event> {
    const event = await this.assertOrganizer(eventId, userId);

    const updated = await this.eventsRepository.update(event.id, {
      ...dto,
      startsAt: dto.startsAt ? new Date(dto.startsAt) : undefined,
    });

    if (!updated) {
      throw new NotFoundException('Event not found');
    }

    return updated;
  }

  async delete(eventId: string, userId: string): Promise<void> {
    await this.assertOrganizer(eventId, userId);
    await this.eventsRepository.delete(eventId);
    this.eventsGateway.broadcastEventDeleted(eventId);
  }

  async leave(eventId: string, userId: string): Promise<void> {
    const event = await this.assertParticipant(eventId, userId);
    if (event.organizerId === userId) {
      throw new ForbiddenException(
        'Organizer cannot leave the event. Delete it instead.',
      );
    }
    await this.eventsRepository.removeParticipant(eventId, userId);
    this.eventsGateway.broadcastParticipantLeft(eventId, userId);
  }

  async assertOrganizer(eventId: string, userId: string): Promise<Event> {
    const event = await this.eventsRepository.findById(eventId);
    if (!event) {
      throw new NotFoundException('Event not found');
    }
    if (event.organizerId !== userId) {
      throw new ForbiddenException('Only the organizer can perform this action');
    }
    return event;
  }

  async assertParticipant(eventId: string, userId: string): Promise<Event> {
    const event = await this.eventsRepository.findById(eventId);
    if (!event) {
      throw new NotFoundException('Event not found');
    }
    const isParticipant = await this.eventsRepository.isParticipant(
      eventId,
      userId,
    );
    if (!isParticipant) {
      throw new ForbiddenException('Not a participant of this event');
    }
    return event;
  }

  async markCompleted(eventId: string): Promise<Event | undefined> {
    return this.eventsRepository.update(eventId, {
      status: 'completed',
      completedAt: new Date(),
    });
  }
}
