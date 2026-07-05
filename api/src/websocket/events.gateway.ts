import {
  ConnectedSocket,
  MessageBody,
  OnGatewayConnection,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from '@nestjs/websockets';
import { Logger, UnauthorizedException, Inject, forwardRef } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { Server, Socket } from 'socket.io';
import { EventsRepository } from '../events/events.repository';
import { UsersRepository } from '../users/users.repository';
import { JwtPayload } from '../auth/strategies/jwt.strategy';

export enum WsEvent {
  LOCATION_UPDATED = 'location.updated',
  PARTICIPANT_JOINED = 'participant.joined',
  PARTICIPANT_ARRIVED = 'participant.arrived',
  EVENT_COMPLETED = 'event.completed',
  EVENT_DELETED = 'event.deleted',
  PARTICIPANT_LEFT = 'participant.left',
}

@WebSocketGateway({ cors: { origin: '*' } })
export class EventsGateway implements OnGatewayConnection {
  @WebSocketServer()
  server!: Server;

  private readonly logger = new Logger(EventsGateway.name);

  constructor(
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
    @Inject(forwardRef(() => EventsRepository))
    private readonly eventsRepository: EventsRepository,
    private readonly usersRepository: UsersRepository,
  ) {}

  async handleConnection(client: Socket) {
    try {
      const token =
        (client.handshake.auth?.token as string | undefined) ??
        (client.handshake.query?.token as string | undefined) ??
        client.handshake.headers.authorization?.replace('Bearer ', '');

      if (!token) {
        throw new UnauthorizedException('Missing token');
      }

      const payload = this.jwtService.verify<JwtPayload>(token, {
        secret: this.configService.getOrThrow<string>('JWT_SECRET'),
      });

      const user = await this.usersRepository.findById(payload.sub);
      if (!user) {
        throw new UnauthorizedException('Invalid token');
      }

      client.data.userId = user.id;
      this.logger.debug(`Client connected: ${user.id}`);
    } catch {
      client.disconnect();
    }
  }

  @SubscribeMessage('join')
  async handleJoin(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { eventId: string },
  ) {
    const userId = client.data.userId as string;
    if (!userId) {
      throw new UnauthorizedException();
    }

    const isParticipant = await this.eventsRepository.isParticipant(
      data.eventId,
      userId,
    );
    if (!isParticipant) {
      throw new UnauthorizedException('Not a participant');
    }

    const room = this.eventRoom(data.eventId);
    await client.join(room);
    return { joined: room };
  }

  @SubscribeMessage('leave')
  async handleLeave(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { eventId: string },
  ) {
    await client.leave(this.eventRoom(data.eventId));
    return { left: this.eventRoom(data.eventId) };
  }

  broadcastLocationUpdated(eventId: string, payload: unknown) {
    this.server
      .to(this.eventRoom(eventId))
      .emit(WsEvent.LOCATION_UPDATED, payload);
  }

  broadcastParticipantJoined(eventId: string, payload: unknown) {
    this.server
      .to(this.eventRoom(eventId))
      .emit(WsEvent.PARTICIPANT_JOINED, payload);
  }

  broadcastParticipantArrived(eventId: string, payload: unknown) {
    this.server
      .to(this.eventRoom(eventId))
      .emit(WsEvent.PARTICIPANT_ARRIVED, payload);
  }

  broadcastEventCompleted(eventId: string, payload: unknown) {
    this.server
      .to(this.eventRoom(eventId))
      .emit(WsEvent.EVENT_COMPLETED, payload);
  }

  broadcastEventDeleted(eventId: string) {
    this.server
      .to(this.eventRoom(eventId))
      .emit(WsEvent.EVENT_DELETED, { eventId });
  }

  broadcastParticipantLeft(eventId: string, userId: string) {
    this.server
      .to(this.eventRoom(eventId))
      .emit(WsEvent.PARTICIPANT_LEFT, { eventId, userId });
  }

  private eventRoom(eventId: string): string {
    return `event:${eventId}`;
  }
}
