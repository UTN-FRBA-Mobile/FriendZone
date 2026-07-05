import { Inject, Injectable } from '@nestjs/common';
import { and, eq } from 'drizzle-orm';
import {
  events,
  invitations,
  users,
  Invitation,
  NewInvitation,
  Event,
  User,
} from '../../drizzle/schema';
import { DRIZZLE } from '../drizzle/drizzle.module';
import type { DrizzleDB } from '../drizzle/drizzle.module';

@Injectable()
export class InvitationsRepository {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDB) {}

  async create(data: NewInvitation): Promise<Invitation> {
    const [invitation] = await this.db
      .insert(invitations)
      .values(data)
      .returning();
    return invitation;
  }

  async findById(id: string): Promise<Invitation | undefined> {
    const [invitation] = await this.db
      .select()
      .from(invitations)
      .where(eq(invitations.id, id))
      .limit(1);
    return invitation;
  }

  async findByEventId(eventId: string): Promise<Invitation[]> {
    return this.db
      .select()
      .from(invitations)
      .where(eq(invitations.eventId, eventId));
  }

  async findByEventAndInvitee(
    eventId: string,
    inviteeId: string,
  ): Promise<Invitation | undefined> {
    const [invitation] = await this.db
      .select()
      .from(invitations)
      .where(
        and(
          eq(invitations.eventId, eventId),
          eq(invitations.inviteeId, inviteeId),
        ),
      )
      .limit(1);
    return invitation;
  }

  async updateStatus(
    id: string,
    status: 'accepted' | 'rejected',
  ): Promise<Invitation | undefined> {
    const [invitation] = await this.db
      .update(invitations)
      .set({ status })
      .where(eq(invitations.id, id))
      .returning();
    return invitation;
  }

  async findPendingByInviteeId(inviteeId: string): Promise<
    Array<{
      invitation: Invitation;
      event: Event;
      organizer: User;
    }>
  > {
    const rows = await this.db
      .select({
        invitation: invitations,
        event: events,
        organizer: users,
      })
      .from(invitations)
      .innerJoin(events, eq(invitations.eventId, events.id))
      .innerJoin(users, eq(events.organizerId, users.id))
      .where(
        and(
          eq(invitations.inviteeId, inviteeId),
          eq(invitations.status, 'pending'),
        ),
      );

    return rows;
  }
}
