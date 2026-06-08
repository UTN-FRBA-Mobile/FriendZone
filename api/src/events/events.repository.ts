import { Inject, Injectable } from '@nestjs/common';
import { and, eq } from 'drizzle-orm';
import {
  events,
  eventParticipants,
  Event,
  NewEvent,
  EventParticipant,
} from '../../drizzle/schema';
import { DRIZZLE } from '../drizzle/drizzle.module';
import type { DrizzleDB } from '../drizzle/drizzle.module';

@Injectable()
export class EventsRepository {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDB) {}

  async create(data: NewEvent): Promise<Event> {
    const [event] = await this.db.insert(events).values(data).returning();
    return event;
  }

  async findById(id: string): Promise<Event | undefined> {
    const [event] = await this.db
      .select()
      .from(events)
      .where(eq(events.id, id))
      .limit(1);
    return event;
  }

  async findByUserId(userId: string): Promise<Event[]> {
    const participantEvents = await this.db
      .select({ event: events })
      .from(eventParticipants)
      .innerJoin(events, eq(eventParticipants.eventId, events.id))
      .where(eq(eventParticipants.userId, userId));

    return participantEvents.map((row: { event: Event }) => row.event);
  }

  async update(
    id: string,
    data: Partial<
      Pick<
        Event,
        | 'title'
        | 'description'
        | 'latitude'
        | 'longitude'
        | 'address'
        | 'startsAt'
        | 'status'
        | 'completedAt'
        | 'arrivalThresholdM'
      >
    >,
  ): Promise<Event | undefined> {
    const [event] = await this.db
      .update(events)
      .set(data)
      .where(eq(events.id, id))
      .returning();
    return event;
  }

  async delete(id: string): Promise<void> {
    await this.db.delete(events).where(eq(events.id, id));
  }

  async addParticipant(data: {
    eventId: string;
    userId: string;
    role: 'organizer' | 'participant';
    sharingLocation: boolean;
  }): Promise<EventParticipant> {
    const [participant] = await this.db
      .insert(eventParticipants)
      .values(data)
      .returning();
    return participant;
  }

  async findParticipant(
    eventId: string,
    userId: string,
  ): Promise<EventParticipant | undefined> {
    const [participant] = await this.db
      .select()
      .from(eventParticipants)
      .where(
        and(
          eq(eventParticipants.eventId, eventId),
          eq(eventParticipants.userId, userId),
        ),
      )
      .limit(1);
    return participant;
  }

  async findParticipants(eventId: string): Promise<EventParticipant[]> {
    return this.db
      .select()
      .from(eventParticipants)
      .where(eq(eventParticipants.eventId, eventId));
  }

  async updateParticipant(
    eventId: string,
    userId: string,
    data: Partial<
      Pick<
        EventParticipant,
        | 'sharingLocation'
        | 'arrived'
        | 'lastLatitude'
        | 'lastLongitude'
        | 'lastLocationAt'
        | 'arrivedAt'
      >
    >,
  ): Promise<EventParticipant | undefined> {
    const [participant] = await this.db
      .update(eventParticipants)
      .set(data)
      .where(
        and(
          eq(eventParticipants.eventId, eventId),
          eq(eventParticipants.userId, userId),
        ),
      )
      .returning();
    return participant;
  }

  async isParticipant(eventId: string, userId: string): Promise<boolean> {
    const participant = await this.findParticipant(eventId, userId);
    return !!participant;
  }
}
