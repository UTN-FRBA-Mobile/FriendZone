import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { randomUUID } from 'crypto';
import { mkdir, unlink, writeFile } from 'fs/promises';
import { join } from 'path';
import { UsersRepository } from './users.repository';
import { toSafeUser, SafeUser } from '../common/utils/user.mapper';

const MAX_PROFILE_PICTURE_BYTES = 20 * 1024 * 1024;
const ALLOWED_MIME_TYPES = new Set(['image/jpeg', 'image/png']);

@Injectable()
export class UsersService {
  constructor(
    private readonly usersRepository: UsersRepository,
    private readonly configService: ConfigService,
  ) {}

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

  async uploadProfilePicture(
    userId: string,
    file: Express.Multer.File,
  ): Promise<SafeUser> {
    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (!file) {
      throw new BadRequestException('Profile picture file is required');
    }

    if (!ALLOWED_MIME_TYPES.has(file.mimetype)) {
      throw new BadRequestException('Only JPEG and PNG images are allowed');
    }

    if (file.size > MAX_PROFILE_PICTURE_BYTES) {
      throw new BadRequestException('Profile picture must be 20 MB or smaller');
    }

    const uploadsDir = this.configService.get<string>('UPLOADS_DIR', 'uploads');
    const profileDir = join(uploadsDir, 'profile-pictures');
    await mkdir(profileDir, { recursive: true });

    const extension = file.mimetype === 'image/png' ? 'png' : 'jpg';
    const filename = `${userId}-${randomUUID()}.${extension}`;
    const absolutePath = join(profileDir, filename);
    await writeFile(absolutePath, file.buffer);

    const profilePictureUrl = `/uploads/profile-pictures/${filename}`;
    const updated = await this.usersRepository.update(userId, {
      profilePictureUrl,
    });

    if (!updated) {
      throw new NotFoundException('User not found');
    }

    await this.deleteProfilePictureFile(user.profilePictureUrl);

    return toSafeUser(updated);
  }

  async removeProfilePicture(userId: string): Promise<SafeUser> {
    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    await this.deleteProfilePictureFile(user.profilePictureUrl);

    const updated = await this.usersRepository.update(userId, {
      profilePictureUrl: null,
    });

    if (!updated) {
      throw new NotFoundException('User not found');
    }

    return toSafeUser(updated);
  }

  async deleteAccount(userId: string): Promise<SafeUser> {
    const user = await this.usersRepository.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    await this.deleteProfilePictureFile(user.profilePictureUrl);
    const deleted = await this.usersRepository.delete(userId);
    if (!deleted) {
      throw new NotFoundException('User not found during deletion');
    }
    return toSafeUser(deleted);
  }

  private async deleteProfilePictureFile(
    profilePictureUrl: string | null,
  ): Promise<void> {
    if (!profilePictureUrl) {
      return;
    }

    const uploadsDir = this.configService.get<string>('UPLOADS_DIR', 'uploads');
    const relativePath = profilePictureUrl.replace(/^\/uploads\//, '');
    const absolutePath = join(uploadsDir, relativePath);

    try {
      await unlink(absolutePath);
    } catch {
      // File may already be missing; ignore.
    }
  }
}
