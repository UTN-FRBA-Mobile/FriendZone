import { Module } from '@nestjs/common';
import { NotificationsModule } from '../notifications/notifications.module';
import { UsersModule } from '../users/users.module';
import { FriendsController } from './friends.controller';
import { FriendsRepository } from './friends.repository';
import { FriendsService } from './friends.service';

@Module({
  imports: [UsersModule, NotificationsModule],
  controllers: [FriendsController],
  providers: [FriendsRepository, FriendsService],
  exports: [FriendsRepository, FriendsService],
})
export class FriendsModule {}
