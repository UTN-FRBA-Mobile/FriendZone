import { Module } from '@nestjs/common';
import { UsersModule } from '../users/users.module';
import { FirebaseNotificationProvider } from './firebase-notification.provider';
import { NOTIFICATION_PROVIDER } from './notification.provider';
import { NotificationsService } from './notifications.service';

@Module({
  imports: [UsersModule],
  providers: [
    NotificationsService,
    FirebaseNotificationProvider,
    {
      provide: NOTIFICATION_PROVIDER,
      useExisting: FirebaseNotificationProvider,
    },
  ],
  exports: [NotificationsService],
})
export class NotificationsModule {}
