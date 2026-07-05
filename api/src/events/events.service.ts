import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { mkdir, writeFile } from 'fs/promises';
import { join } from 'path';
import { randomUUID } from 'crypto';
import { NotificationsService } from '../notifications/notifications.service';
import { UsersRepository } from '../users/users.repository';
import { CreateEventDto, UpdateEventDto } from './dto/event.dto';
import { EventsRepository } from './events.repository';
import { Event } from '../../drizzle/schema';

const MAX_COVER_BYTES = 2 * 1024 * 1024;
const ALLOWED_MIME_TYPES = new Set(['image/jpeg', 'image/png']);

@Injectable()
export class EventsService {
  constructor(
    private readonly eventsRepository: EventsRepository,
    private readonly usersRepository: UsersRepository,
    private readonly notificationsService: NotificationsService,
    private readonly configService: ConfigService,
  ) {}

  async create(userId: string, dto: CreateEventDto): Promise<Event> {
    const defaultThreshold = this.configService.get<number>(
      'ARRIVAL_THRESHOLD',
      150,
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

    if (dto.status) {
      return this.updateStatus(event, userId, dto.status);
    }

    const { status: _status, ...rest } = dto;
    const updated = await this.eventsRepository.update(event.id, {
      ...rest,
      startsAt: rest.startsAt ? new Date(rest.startsAt) : undefined,
    });

    if (!updated) {
      throw new NotFoundException('Event not found');
    }

    return updated;
  }

  private async updateStatus(
    event: Event,
    userId: string,
    status: 'completed' | 'cancelled',
  ): Promise<Event> {
    if (event.organizerId !== userId) {
      throw new ForbiddenException('Only the organizer can perform this action');
    }

    if (event.status === 'completed' || event.status === 'cancelled') {
      throw new BadRequestException('Event is already finished');
    }

    const updated = await this.eventsRepository.update(event.id, {
      status,
      completedAt: status === 'completed' ? new Date() : event.completedAt,
    });

    if (!updated) {
      throw new NotFoundException('Event not found');
    }

    const participants = await this.eventsRepository.findParticipants(event.id);
    const userIds = participants.map((p) => p.userId);

    if (status === 'completed') {
      await this.notificationsService.notifyEventManuallyCompleted(
        userIds,
        event.title,
        event.id,
      );
    } else {
      await this.notificationsService.notifyEventCancelled(
        userIds,
        event.title,
        event.id,
      );
    }

    return updated;
  }

  async uploadCover(
    eventId: string,
    userId: string,
    file: Express.Multer.File,
  ): Promise<Event> {
    await this.assertOrganizer(eventId, userId);

    if (!file) {
      throw new BadRequestException('Cover image file is required');
    }

    if (!ALLOWED_MIME_TYPES.has(file.mimetype)) {
      throw new BadRequestException('Only JPEG and PNG images are allowed');
    }

    if (file.size > MAX_COVER_BYTES) {
      throw new BadRequestException('Cover image must be 2 MB or smaller');
    }

    const uploadsDir = this.configService.get<string>('UPLOADS_DIR', 'uploads');
    const coverDir = join(uploadsDir, 'event-covers');
    await mkdir(coverDir, { recursive: true });

    const extension = file.mimetype === 'image/png' ? 'png' : 'jpg';
    const filename = `${eventId}-${randomUUID()}.${extension}`;
    const absolutePath = join(coverDir, filename);
    await writeFile(absolutePath, file.buffer);

    const coverImageUrl = `/uploads/event-covers/${filename}`;
    const updated = await this.eventsRepository.update(eventId, {
      coverImageUrl,
    });

    if (!updated) {
      throw new NotFoundException('Event not found');
    }

    return updated;
  }

  async delete(eventId: string, userId: string): Promise<void> {
    await this.assertOrganizer(eventId, userId);
    await this.eventsRepository.delete(eventId);
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
