import { Inject, Injectable } from '@nestjs/common';
import { and, count, desc, eq, inArray, isNull, or } from 'drizzle-orm';
import {
  userNotifications,
  UserNotification,
  NewUserNotification,
} from '../../drizzle/schema';
import { DRIZZLE } from '../drizzle/drizzle.module';
import type { DrizzleDB } from '../drizzle/drizzle.module';
import { ACTIONABLE_NOTIFICATION_TYPES } from './notification.provider';

@Injectable()
export class UserNotificationsRepository {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDB) {}

  private inboxFilter(userId: string) {
    return and(
      eq(userNotifications.userId, userId),
      isNull(userNotifications.resolvedAt),
      or(
        inArray(userNotifications.type, [...ACTIONABLE_NOTIFICATION_TYPES]),
        isNull(userNotifications.readAt),
      ),
    );
  }

  async create(data: NewUserNotification): Promise<UserNotification> {
    const [row] = await this.db
      .insert(userNotifications)
      .values(data)
      .returning();
    return row;
  }

  async findInbox(userId: string): Promise<UserNotification[]> {
    return this.db
      .select()
      .from(userNotifications)
      .where(this.inboxFilter(userId))
      .orderBy(desc(userNotifications.createdAt));
  }

  async countInbox(userId: string): Promise<number> {
    const [result] = await this.db
      .select({ value: count() })
      .from(userNotifications)
      .where(this.inboxFilter(userId));
    return Number(result?.value ?? 0);
  }

  async findByIdForUser(
    id: string,
    userId: string,
  ): Promise<UserNotification | undefined> {
    const [row] = await this.db
      .select()
      .from(userNotifications)
      .where(
        and(eq(userNotifications.id, id), eq(userNotifications.userId, userId)),
      )
      .limit(1);
    return row;
  }

  async markRead(id: string, userId: string): Promise<UserNotification | undefined> {
    const [row] = await this.db
      .update(userNotifications)
      .set({ readAt: new Date() })
      .where(
        and(
          eq(userNotifications.id, id),
          eq(userNotifications.userId, userId),
          isNull(userNotifications.readAt),
        ),
      )
      .returning();
    return row;
  }

  async resolveByRequestId(userId: string, requestId: string): Promise<void> {
    const rows = await this.db
      .select()
      .from(userNotifications)
      .where(
        and(
          eq(userNotifications.userId, userId),
          isNull(userNotifications.resolvedAt),
        ),
      );

    const toResolve = rows.filter((row) => row.data?.requestId === requestId);
    for (const row of toResolve) {
      await this.db
        .update(userNotifications)
        .set({ resolvedAt: new Date() })
        .where(eq(userNotifications.id, row.id));
    }
  }

  async resolveByInvitationId(
    userId: string,
    invitationId: string,
  ): Promise<void> {
    const rows = await this.db
      .select()
      .from(userNotifications)
      .where(
        and(
          eq(userNotifications.userId, userId),
          isNull(userNotifications.resolvedAt),
        ),
      );

    const toResolve = rows.filter(
      (row) => row.data?.invitationId === invitationId,
    );
    for (const row of toResolve) {
      await this.db
        .update(userNotifications)
        .set({ resolvedAt: new Date() })
        .where(eq(userNotifications.id, row.id));
    }
  }
}
