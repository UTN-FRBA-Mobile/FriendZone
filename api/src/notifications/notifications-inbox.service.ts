import {
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { UserNotification } from '../../drizzle/schema';
import { InboxNotificationDto } from './dto/inbox.dto';
import { isActionableNotificationType } from './notification.provider';
import { UserNotificationsRepository } from './user-notifications.repository';

@Injectable()
export class NotificationsInboxService {
  constructor(
    private readonly userNotificationsRepository: UserNotificationsRepository,
  ) {}

  async getInbox(userId: string): Promise<InboxNotificationDto[]> {
    const rows = await this.userNotificationsRepository.findInbox(userId);
    return rows.map((row) => this.toDto(row));
  }

  async getBadgeCount(userId: string): Promise<number> {
    return this.userNotificationsRepository.countInbox(userId);
  }

  async markRead(userId: string, notificationId: string): Promise<InboxNotificationDto> {
    const row = await this.userNotificationsRepository.findByIdForUser(
      notificationId,
      userId,
    );
    if (!row) {
      throw new NotFoundException('Notification not found');
    }
    if (isActionableNotificationType(row.type)) {
      throw new ForbiddenException('Actionable notifications cannot be marked read');
    }
    if (row.readAt) {
      return this.toDto(row);
    }
    const updated = await this.userNotificationsRepository.markRead(
      notificationId,
      userId,
    );
    if (!updated) {
      throw new NotFoundException('Notification not found');
    }
    return this.toDto(updated);
  }

  private toDto(row: UserNotification): InboxNotificationDto {
    return {
      id: row.id,
      type: row.type as InboxNotificationDto['type'],
      title: row.title,
      body: row.body,
      createdAt: row.createdAt.toISOString(),
      actionable: isActionableNotificationType(row.type),
      read: row.readAt != null,
      data: row.data ?? {},
    };
  }
}
