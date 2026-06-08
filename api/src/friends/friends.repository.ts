import { Inject, Injectable } from '@nestjs/common';
import { and, count, eq, inArray, or } from 'drizzle-orm';
import {
  friendRequests,
  friendships,
  users,
  FriendRequest,
  Friendship,
  User,
} from '../../drizzle/schema';
import { DRIZZLE } from '../drizzle/drizzle.module';
import type { DrizzleDB } from '../drizzle/drizzle.module';
import { sortedUserPair } from './friends.utils';

@Injectable()
export class FriendsRepository {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDB) {}

  async areFriends(userIdA: string, userIdB: string): Promise<boolean> {
    const { userIdLow, userIdHigh } = sortedUserPair(userIdA, userIdB);
    const [row] = await this.db
      .select({ id: friendships.id })
      .from(friendships)
      .where(
        and(
          eq(friendships.userIdLow, userIdLow),
          eq(friendships.userIdHigh, userIdHigh),
        ),
      )
      .limit(1);
    return !!row;
  }

  async findPendingBetween(
    userIdA: string,
    userIdB: string,
  ): Promise<FriendRequest | undefined> {
    const [row] = await this.db
      .select()
      .from(friendRequests)
      .where(
        and(
          eq(friendRequests.status, 'pending'),
          or(
            and(
              eq(friendRequests.requesterId, userIdA),
              eq(friendRequests.addresseeId, userIdB),
            ),
            and(
              eq(friendRequests.requesterId, userIdB),
              eq(friendRequests.addresseeId, userIdA),
            ),
          ),
        ),
      )
      .limit(1);
    return row;
  }

  async findRequestById(id: string): Promise<FriendRequest | undefined> {
    const [row] = await this.db
      .select()
      .from(friendRequests)
      .where(eq(friendRequests.id, id))
      .limit(1);
    return row;
  }

  async createRequest(
    requesterId: string,
    addresseeId: string,
  ): Promise<FriendRequest> {
    const existing = await this.db
      .select()
      .from(friendRequests)
      .where(
        and(
          eq(friendRequests.requesterId, requesterId),
          eq(friendRequests.addresseeId, addresseeId),
        ),
      )
      .limit(1);

    if (existing[0]) {
      if (existing[0].status === 'rejected') {
        const [updated] = await this.db
          .update(friendRequests)
          .set({
            status: 'pending',
            respondedAt: null,
            createdAt: new Date(),
          })
          .where(eq(friendRequests.id, existing[0].id))
          .returning();
        return updated;
      }
      return existing[0];
    }

    const [created] = await this.db
      .insert(friendRequests)
      .values({ requesterId, addresseeId, status: 'pending' })
      .returning();
    return created;
  }

  async updateRequestStatus(
    id: string,
    status: 'accepted' | 'rejected',
  ): Promise<FriendRequest | undefined> {
    const [updated] = await this.db
      .update(friendRequests)
      .set({ status, respondedAt: new Date() })
      .where(eq(friendRequests.id, id))
      .returning();
    return updated;
  }

  async createFriendship(userIdA: string, userIdB: string): Promise<Friendship> {
    const { userIdLow, userIdHigh } = sortedUserPair(userIdA, userIdB);
    const [created] = await this.db
      .insert(friendships)
      .values({ userIdLow, userIdHigh })
      .onConflictDoNothing()
      .returning();
    if (created) {
      return created;
    }
    const [existing] = await this.db
      .select()
      .from(friendships)
      .where(
        and(
          eq(friendships.userIdLow, userIdLow),
          eq(friendships.userIdHigh, userIdHigh),
        ),
      )
      .limit(1);
    return existing;
  }

  async listFriends(userId: string): Promise<User[]> {
    const rows = await this.db
      .select()
      .from(friendships)
      .where(
        or(
          eq(friendships.userIdLow, userId),
          eq(friendships.userIdHigh, userId),
        ),
      );

    if (rows.length === 0) {
      return [];
    }

    const friendIds = rows.map((row) =>
      row.userIdLow === userId ? row.userIdHigh : row.userIdLow,
    );

    return this.db.select().from(users).where(inArray(users.id, friendIds));
  }

  async listIncomingPending(userId: string): Promise<
    Array<FriendRequest & { requester: User }>
  > {
    const rows = await this.db
      .select({
        request: friendRequests,
        requester: users,
      })
      .from(friendRequests)
      .innerJoin(users, eq(friendRequests.requesterId, users.id))
      .where(
        and(
          eq(friendRequests.addresseeId, userId),
          eq(friendRequests.status, 'pending'),
        ),
      );

    return rows.map((row) => ({
      ...row.request,
      requester: row.requester,
    }));
  }

  async countIncomingPending(userId: string): Promise<number> {
    const [result] = await this.db
      .select({ value: count() })
      .from(friendRequests)
      .where(
        and(
          eq(friendRequests.addresseeId, userId),
          eq(friendRequests.status, 'pending'),
        ),
      );
    return Number(result?.value ?? 0);
  }
}
