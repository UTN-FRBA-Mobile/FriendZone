import { Inject, Injectable } from '@nestjs/common';
import { eq, ilike, or } from 'drizzle-orm';
import { users, NewUser, User } from '../../drizzle/schema';
import { DRIZZLE } from '../drizzle/drizzle.module';
import type { DrizzleDB } from '../drizzle/drizzle.module';

@Injectable()
export class UsersRepository {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDB) {}

  async create(data: NewUser): Promise<User> {
    const [user] = await this.db.insert(users).values(data).returning();
    return user;
  }

  async findById(id: string): Promise<User | undefined> {
    const [user] = await this.db
      .select()
      .from(users)
      .where(eq(users.id, id))
      .limit(1);
    return user;
  }

  async findByEmail(email: string): Promise<User | undefined> {
    const [user] = await this.db
      .select()
      .from(users)
      .where(eq(users.email, email.toLowerCase()))
      .limit(1);
    return user;
  }

  async findByUsername(username: string): Promise<User | undefined> {
    const [user] = await this.db
      .select()
      .from(users)
      .where(eq(users.username, username.toLowerCase()))
      .limit(1);
    return user;
  }

  async findByEmailOrUsername(identifier: string): Promise<User | undefined> {
    const normalized = identifier.toLowerCase();
    const [user] = await this.db
      .select()
      .from(users)
      .where(or(eq(users.email, normalized), eq(users.username, normalized)))
      .limit(1);
    return user;
  }

  async search(query: string, limit = 20): Promise<User[]> {
    const pattern = `${query.toLowerCase()}%`;
    return this.db
      .select()
      .from(users)
      .where(
        or(ilike(users.email, pattern), ilike(users.username, pattern)),
      )
      .limit(limit);
  }

  async update(
    id: string,
    data: Partial<
      Pick<User, 'displayName' | 'fcmToken' | 'locationSharingEnabled'>
    >,
  ): Promise<User | undefined> {
    const [user] = await this.db
      .update(users)
      .set(data)
      .where(eq(users.id, id))
      .returning();
    return user;
  }
}
