import { Module, forwardRef } from '@nestjs/common';
import { EventsModule } from '../events/events.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { UsersModule } from '../users/users.module';
import { WebsocketModule } from '../websocket/websocket.module';
import { LocationsController } from './locations.controller';
import { LocationsService } from './locations.service';
import { ProximityService } from './proximity.service';

@Module({
  imports: [
    EventsModule,
    UsersModule,
    NotificationsModule,
    forwardRef(() => WebsocketModule),
  ],
  controllers: [LocationsController],
  providers: [LocationsService, ProximityService],
  exports: [LocationsService, ProximityService],
})
export class LocationsModule {}
