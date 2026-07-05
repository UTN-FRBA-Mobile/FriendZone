import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Inject,
  Injectable,
  NotFoundException,
  forwardRef,
} from '@nestjs/common';
import { FriendsService } from '../friends/friends.service';
import { EventsRepository } from '../events/events.repository';
import { EventsService } from '../events/events.service';
import { NotificationsService } from '../notifications/notifications.service';
import { UsersRepository } from '../users/users.repository';
import {
  CreateInvitationDto,
  UpdateInvitationDto,
} from './dto/invitation.dto';
import { InvitationsRepository } from './invitations.repository';
import { Invitation } from '../../drizzle/schema';
import { EventsGateway } from '../websocket/events.gateway';

export interface PendingInvitationWithEvent {
  id: string;
  eventId: string;
  inviteeId: string;
  invitedById: string;
  status: string;
  createdAt: string;
  eventTitle: string;
  eventStartsAt: string;
  organizerDisplayName: string;
}

@Injectable()
export class InvitationsService {
  constructor(
    private readonly invitationsRepository: InvitationsRepository,
    private readonly eventsRepository: EventsRepository,
    private readonly eventsService: EventsService,
    private readonly usersRepository: UsersRepository,
    private readonly friendsService: FriendsService,
    private readonly notificationsService: NotificationsService,
    @Inject(forwardRef(() => EventsGateway))
    private readonly eventsGateway: EventsGateway,
  ) {}

  async create(
    eventId: string,
    organizerId: string,
    dto: CreateInvitationDto,
  ): Promise<Invitation> {
    const event = await this.eventsService.assertOrganizer(eventId, organizerId);

    const invitee = await this.usersRepository.findByEmailOrUsername(
      dto.emailOrUsername,
    );
    if (!invitee) {
      throw new NotFoundException('User not found');
    }

    if (invitee.id === organizerId) {
      throw new BadRequestException('Cannot invite yourself');
    }

    await this.friendsService.assertFriends(organizerId, invitee.id);

    const existing = await this.invitationsRepository.findByEventAndInvitee(
      eventId,
      invitee.id,
    );
    if (existing) {
      throw new ConflictException('User already invited to this event');
    }

    const invitation = await this.invitationsRepository.create({
      eventId,
      inviteeId: invitee.id,
      invitedById: organizerId,
      status: 'pending',
    });

    const organizer = await this.usersRepository.findById(organizerId);
    await this.notificationsService.notifyInvitation(
      invitee.id,
      event.title,
      event.id,
      invitation.id,
      event.startsAt.toISOString(),
      organizer?.displayName ?? 'Organizer',
    );

    return invitation;
  }

  async findMinePending(userId: string): Promise<PendingInvitationWithEvent[]> {
    const rows =
      await this.invitationsRepository.findPendingByInviteeId(userId);
    return rows.map((row) => ({
      id: row.invitation.id,
      eventId: row.invitation.eventId,
      inviteeId: row.invitation.inviteeId,
      invitedById: row.invitation.invitedById,
      status: row.invitation.status,
      createdAt: row.invitation.createdAt.toISOString(),
      eventTitle: row.event.title,
      eventStartsAt: row.event.startsAt.toISOString(),
      organizerDisplayName: row.organizer.displayName,
    }));
  }

  async findByEvent(
    eventId: string,
    userId: string,
  ): Promise<Invitation[]> {
    const event = await this.eventsRepository.findById(eventId);
    if (!event) {
      throw new NotFoundException('Event not found');
    }

    const isOrganizer = event.organizerId === userId;
    const isParticipant = await this.eventsRepository.isParticipant(
      eventId,
      userId,
    );

    if (!isOrganizer && !isParticipant) {
      throw new ForbiddenException('Not authorized to view invitations');
    }

    const all = await this.invitationsRepository.findByEventId(eventId);

    if (isOrganizer) {
      return all;
    }

    return all.filter((inv) => inv.inviteeId === userId);
  }

  async respond(
    invitationId: string,
    userId: string,
    dto: UpdateInvitationDto,
  ): Promise<Invitation> {
    const invitation = await this.invitationsRepository.findById(invitationId);
    if (!invitation) {
      throw new NotFoundException('Invitation not found');
    }

    if (invitation.inviteeId !== userId) {
      throw new ForbiddenException('Not your invitation');
    }

    if (invitation.status !== 'pending') {
      throw new BadRequestException('Invitation already responded to');
    }

    const updated = await this.invitationsRepository.updateStatus(
      invitationId,
      dto.status,
    );

    if (!updated) {
      throw new NotFoundException('Invitation not found');
    }

    if (dto.status === 'accepted') {
      const user = await this.usersRepository.findById(userId);
      if (!user) {
        throw new NotFoundException('User not found');
      }

      const existingParticipant =
        await this.eventsRepository.findParticipant(
          invitation.eventId,
          userId,
        );

      if (!existingParticipant) {
        await this.eventsRepository.addParticipant({
          eventId: invitation.eventId,
          userId,
          role: 'participant',
          sharingLocation: user.locationSharingEnabled,
        });

        this.eventsGateway.broadcastParticipantJoined(invitation.eventId, {
          userId,
          displayName: user.displayName,
        });
      }
    }

    await this.notificationsService.resolveInvitationNotification(
      userId,
      invitationId,
    );

    return updated;
  }
}
