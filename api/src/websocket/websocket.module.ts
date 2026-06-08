import { Module, forwardRef } from '@nestjs/common';
import { JwtModule } from '@nestjs/jwt';
import { EventsModule } from '../events/events.module';
import { UsersModule } from '../users/users.module';
import { EventsGateway } from './events.gateway';

@Module({
  imports: [
    JwtModule.register({}),
    EventsModule,
    UsersModule,
  ],
  providers: [EventsGateway],
  exports: [EventsGateway],
})
export class WebsocketModule {}
