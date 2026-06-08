import { Inject, Injectable } from '@nestjs/common';
import { eq, lt } from 'drizzle-orm';
import { createHash } from 'crypto';
import { refreshTokens, NewRefreshToken } from '../../drizzle/schema';
import { DRIZZLE } from '../drizzle/drizzle.module';
import type { DrizzleDB } from '../drizzle/drizzle.module';

@Injectable()
export class AuthRepository {
  constructor(@Inject(DRIZZLE) private readonly db: DrizzleDB) {}

  hashToken(token: string): string {
    return createHash('sha256').update(token).digest('hex');
  }

  async saveRefreshToken(data: NewRefreshToken) {
    await this.db.insert(refreshTokens).values(data);
  }

  async findRefreshToken(tokenHash: string) {
    const [record] = await this.db
      .select()
      .from(refreshTokens)
      .where(eq(refreshTokens.tokenHash, tokenHash))
      .limit(1);
    return record;
  }

  async deleteRefreshToken(tokenHash: string) {
    await this.db
      .delete(refreshTokens)
      .where(eq(refreshTokens.tokenHash, tokenHash));
  }

  async deleteUserRefreshTokens(userId: string) {
    await this.db
      .delete(refreshTokens)
      .where(eq(refreshTokens.userId, userId));
  }

  async deleteExpiredTokens() {
    await this.db
      .delete(refreshTokens)
      .where(lt(refreshTokens.expiresAt, new Date()));
  }
}
