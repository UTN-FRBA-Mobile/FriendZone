import { Module } from '@nestjs/common';
import { UsersModule } from '../users/users.module';
import { FirebaseNotificationProvider } from './firebase-notification.provider';
import { NOTIFICATION_PROVIDER } from './notification.provider';
import { NotificationsInboxController } from './notifications-inbox.controller';
import { NotificationsInboxService } from './notifications-inbox.service';
import { NotificationsService } from './notifications.service';
import { UserNotificationsRepository } from './user-notifications.repository';

@Module({
  imports: [UsersModule],
  controllers: [NotificationsInboxController],
  providers: [
    NotificationsService,
    NotificationsInboxService,
    UserNotificationsRepository,
    FirebaseNotificationProvider,
    {
      provide: NOTIFICATION_PROVIDER,
      useExisting: FirebaseNotificationProvider,
    },
  ],
  exports: [NotificationsService],
})
export class NotificationsModule {}
