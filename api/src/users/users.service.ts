import { Injectable, NotFoundException } from '@nestjs/common';
import { UsersRepository } from './users.repository';
import { toSafeUser, SafeUser } from '../common/utils/user.mapper';

@Injectable()
export class UsersService {
  constructor(private readonly usersRepository: UsersRepository) {}

  async getProfile(userId: string): Promise<SafeUser> {
    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return toSafeUser(user);
  }

  async updateProfile(
    userId: string,
    displayName: string,
  ): Promise<SafeUser> {
    const user = await this.usersRepository.update(userId, { displayName });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return toSafeUser(user);
  }

  async updateLocationSharing(
    userId: string,
    enabled: boolean,
  ): Promise<SafeUser> {
    const user = await this.usersRepository.update(userId, {
      locationSharingEnabled: enabled,
    });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return toSafeUser(user);
  }

  async updateFcmToken(userId: string, token: string): Promise<SafeUser> {
    const user = await this.usersRepository.update(userId, { fcmToken: token });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return toSafeUser(user);
  }

  async getFriends(userId: string): Promise<SafeUser[]> {
    const friends = await this.usersRepository.findFriends(userId);
    return friends.map(toSafeUser);
  }

  async lookup(userId: string, query: string): Promise<SafeUser> {
    const user = await this.usersRepository.findByEmailOrUsername(query);
    if (!user) {
      throw new NotFoundException('User not found');
    }
    if (user.id === userId) {
      throw new NotFoundException('User not found');
    }
    return toSafeUser(user);
  }

  async search(query: string): Promise<SafeUser[]> {
    const users = await this.usersRepository.search(query);
    return users.map(toSafeUser);
  }

  async findById(userId: string): Promise<SafeUser | undefined> {
    const user = await this.usersRepository.findById(userId);
    return user ? toSafeUser(user) : undefined;
  }
}
