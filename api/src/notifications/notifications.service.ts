import { Inject, Injectable, Logger } from '@nestjs/common';
import { UsersRepository } from '../users/users.repository';
import {
  NOTIFICATION_PROVIDER,
  NotificationType,
} from './notification.provider';
import type { NotificationProvider } from './notification.provider';

@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(
    @Inject(NOTIFICATION_PROVIDER)
    private readonly notificationProvider: NotificationProvider,
    private readonly usersRepository: UsersRepository,
  ) {}

  async notifyUser(
    userId: string,
    title: string,
    body: string,
    type: NotificationType,
    data?: Record<string, string>,
  ): Promise<void> {
    const user = await this.usersRepository.findById(userId);
    if (!user?.fcmToken) {
      this.logger.debug(`No FCM token for user ${userId}`);
      return;
    }

    await this.notificationProvider.send(user.fcmToken, {
      title,
      body,
      data: { type, ...data },
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

  async notifyInvitation(
    inviteeId: string,
    eventTitle: string,
    eventId: string,
  ): Promise<void> {
    await this.notifyUser(
      inviteeId,
      'New Event Invitation',
      `You have been invited to "${eventTitle}"`,
      NotificationType.INVITATION_CREATED,
      { eventId },
    );
  }

  async notifyParticipantArrived(
    organizerId: string,
    participantName: string,
    eventId: string,
  ): Promise<void> {
    await this.notifyUser(
      organizerId,
      'Participant Arrived',
      `${participantName} has arrived at the event location`,
      NotificationType.PARTICIPANT_ARRIVED,
      { eventId },
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
      { eventId },
    );
  }
}
