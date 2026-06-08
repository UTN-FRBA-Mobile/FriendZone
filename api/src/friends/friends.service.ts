import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { NotificationsService } from '../notifications/notifications.service';
import { UsersRepository } from '../users/users.repository';
import { toSafeUser, SafeUser } from '../common/utils/user.mapper';
import { CreateFriendRequestDto, RespondFriendRequestDto } from './dto/friend.dto';
import { FriendsRepository } from './friends.repository';

export interface FriendRequestWithUser {
  id: string;
  requesterId: string;
  addresseeId: string;
  status: string;
  createdAt: string;
  respondedAt: string | null;
  requester: SafeUser;
}

@Injectable()
export class FriendsService {
  constructor(
    private readonly friendsRepository: FriendsRepository,
    private readonly usersRepository: UsersRepository,
    private readonly notificationsService: NotificationsService,
  ) {}

  async listFriends(userId: string): Promise<SafeUser[]> {
    const friends = await this.friendsRepository.listFriends(userId);
    return friends.map(toSafeUser);
  }

  async listIncomingRequests(userId: string): Promise<FriendRequestWithUser[]> {
    const rows = await this.friendsRepository.listIncomingPending(userId);
    return rows.map((row) => ({
      id: row.id,
      requesterId: row.requesterId,
      addresseeId: row.addresseeId,
      status: row.status,
      createdAt: row.createdAt.toISOString(),
      respondedAt: row.respondedAt?.toISOString() ?? null,
      requester: toSafeUser(row.requester),
    }));
  }

  async countIncomingRequests(userId: string): Promise<number> {
    return this.friendsRepository.countIncomingPending(userId);
  }

  async createRequest(
    userId: string,
    dto: CreateFriendRequestDto,
  ): Promise<FriendRequestWithUser> {
    const target = await this.usersRepository.findByEmailOrUsername(
      dto.emailOrUsername,
    );
    if (!target) {
      throw new NotFoundException('User not found');
    }
    if (target.id === userId) {
      throw new BadRequestException('Cannot add yourself');
    }

    const alreadyFriends = await this.friendsRepository.areFriends(
      userId,
      target.id,
    );
    if (alreadyFriends) {
      throw new ConflictException('Already friends');
    }

    const pending = await this.friendsRepository.findPendingBetween(
      userId,
      target.id,
    );
    if (pending) {
      if (pending.requesterId === userId) {
        throw new ConflictException('Friend request already sent');
      }
      throw new ConflictException('This user already sent you a request');
    }

    const request = await this.friendsRepository.createRequest(
      userId,
      target.id,
    );

    const requester = await this.usersRepository.findById(userId);
    if (!requester) {
      throw new NotFoundException('User not found');
    }

    await this.notificationsService.notifyFriendRequest(
      target.id,
      requester.displayName,
      request.id,
    );

    return {
      id: request.id,
      requesterId: request.requesterId,
      addresseeId: request.addresseeId,
      status: request.status,
      createdAt: request.createdAt.toISOString(),
      respondedAt: request.respondedAt?.toISOString() ?? null,
      requester: toSafeUser(requester),
    };
  }

  async respondToRequest(
    userId: string,
    requestId: string,
    dto: RespondFriendRequestDto,
  ): Promise<void> {
    const request = await this.friendsRepository.findRequestById(requestId);
    if (!request) {
      throw new NotFoundException('Friend request not found');
    }
    if (request.addresseeId !== userId) {
      throw new ForbiddenException('Not allowed to respond to this request');
    }
    if (request.status !== 'pending') {
      throw new BadRequestException('Request already responded');
    }

    await this.friendsRepository.updateRequestStatus(requestId, dto.status);

    if (dto.status === 'accepted') {
      await this.friendsRepository.createFriendship(
        request.requesterId,
        request.addresseeId,
      );
    }

    await this.notificationsService.resolveFriendRequestNotification(
      userId,
      requestId,
    );
  }

  async assertFriends(userIdA: string, userIdB: string): Promise<void> {
    const areFriends = await this.friendsRepository.areFriends(userIdA, userIdB);
    if (!areFriends) {
      throw new ForbiddenException('Can only invite friends');
    }
  }
}
