import { Inject, Injectable, Logger } from '@nestjs/common';
import { UsersRepository } from '../users/users.repository';
import {
  NOTIFICATION_PROVIDER,
  NotificationType,
} from './notification.provider';
import type { NotificationProvider } from './notification.provider';
import { UserNotificationsRepository } from './user-notifications.repository';

@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(
    @Inject(NOTIFICATION_PROVIDER)
    private readonly notificationProvider: NotificationProvider,
    private readonly usersRepository: UsersRepository,
    private readonly userNotificationsRepository: UserNotificationsRepository,
  ) {}

  async notifyUser(
    userId: string,
    title: string,
    body: string,
    type: NotificationType,
    data?: Record<string, string>,
  ): Promise<void> {
    const notification = await this.userNotificationsRepository.create({
      userId,
      type,
      title,
      body,
      data,
    });

    const user = await this.usersRepository.findById(userId);
    if (!user?.fcmToken) {
      this.logger.debug(`No FCM token for user ${userId}`);
      return;
    }

    await this.notificationProvider.send(user.fcmToken, {
      title,
      body,
      data: { type, notificationId: notification.id, ...data },
    });
  }

  async notifyUsers(
    userIds: string[],
    title: string,
    body: string,
    type: NotificationType,
    data?: Record<string, string>,
  ): Promise<void> {
    await Promise.all(
      userIds.map((userId) =>
        this.notifyUser(userId, title, body, type, data),
      ),
    );
  }

  async notifyFriendRequest(
    addresseeId: string,
    requesterDisplayName: string,
    requestId: string,
  ): Promise<void> {
    await this.notifyUser(
      addresseeId,
      'New Friend Request',
      `${requesterDisplayName} wants to be your friend`,
      NotificationType.FRIEND_REQUEST,
      { requestId, requesterDisplayName },
    );
  }

  async notifyInvitation(
    inviteeId: string,
    eventTitle: string,
    eventId: string,
    invitationId: string,
    eventStartsAt: string,
    organizerDisplayName: string,
  ): Promise<void> {
    await this.notifyUser(
      inviteeId,
      'New Event Invitation',
      `You have been invited to "${eventTitle}"`,
      NotificationType.INVITATION_CREATED,
      {
        eventId,
        invitationId,
        eventTitle,
        eventStartsAt,
        organizerDisplayName,
      },
    );
  }

  async notifyParticipantArrived(
    organizerId: string,
    participantName: string,
    eventId: string,
    eventTitle: string,
  ): Promise<void> {
    await this.notifyUser(
      organizerId,
      'Participant Arrived',
      `${participantName} has arrived at the event location`,
      NotificationType.PARTICIPANT_ARRIVED,
      { eventId, eventTitle, participantName },
    );
  }

  async notifyEventCompleted(
    userIds: string[],
    eventTitle: string,
    eventId: string,
  ): Promise<void> {
    await this.notifyUsers(
      userIds,
      'Event Completed',
      `Everyone has arrived at "${eventTitle}"!`,
      NotificationType.EVENT_COMPLETED,
      { eventId, eventTitle },
    );
  }

  async resolveFriendRequestNotification(
    userId: string,
    requestId: string,
  ): Promise<void> {
    await this.userNotificationsRepository.resolveByRequestId(
      userId,
      requestId,
    );
  }

  async resolveInvitationNotification(
    userId: string,
    invitationId: string,
  ): Promise<void> {
    await this.userNotificationsRepository.resolveByInvitationId(
      userId,
      invitationId,
    );
  }
}
