import { Module, forwardRef } from '@nestjs/common';
import { FriendsModule } from '../friends/friends.module';
import { EventsModule } from '../events/events.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { UsersModule } from '../users/users.module';
import { WebsocketModule } from '../websocket/websocket.module';
import { InvitationsController } from './invitations.controller';
import { InvitationsRepository } from './invitations.repository';
import { InvitationsService } from './invitations.service';

@Module({
  imports: [
    FriendsModule,
    EventsModule,
    UsersModule,
    NotificationsModule,
    forwardRef(() => WebsocketModule),
  ],
  controllers: [InvitationsController],
  providers: [InvitationsService, InvitationsRepository],
  exports: [InvitationsService, InvitationsRepository],
})
export class InvitationsModule {}
