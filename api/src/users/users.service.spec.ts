import { BadRequestException, NotFoundException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { mkdir, unlink, writeFile } from 'fs/promises';
import { UsersRepository } from './users.repository';
import { UsersService } from './users.service';

jest.mock('fs/promises', () => ({
  mkdir: jest.fn().mockResolvedValue(undefined),
  writeFile: jest.fn().mockResolvedValue(undefined),
  unlink: jest.fn().mockResolvedValue(undefined),
}));

describe('UsersService profile picture', () => {
  const userId = '11111111-1111-1111-1111-111111111111';
  const baseUser = {
    id: userId,
    email: 'user@example.com',
    username: 'user',
    passwordHash: 'hash',
    displayName: 'User',
    fcmToken: null,
    locationSharingEnabled: false,
    profilePictureUrl: null,
    createdAt: new Date(),
  };

  let usersRepository: jest.Mocked<UsersRepository>;
  let configService: jest.Mocked<ConfigService>;
  let service: UsersService;

  beforeEach(() => {
    usersRepository = {
      findById: jest.fn(),
      update: jest.fn(),
    } as unknown as jest.Mocked<UsersRepository>;

    configService = {
      get: jest.fn().mockReturnValue('uploads'),
    } as unknown as jest.Mocked<ConfigService>;

    service = new UsersService(usersRepository, configService);
    jest.clearAllMocks();
  });

  it('uploadProfilePicture stores file and updates user', async () => {
    usersRepository.findById.mockResolvedValue(baseUser);
    usersRepository.update.mockResolvedValue({
      ...baseUser,
      profilePictureUrl: '/uploads/profile-pictures/test.jpg',
    });

    const file = {
      mimetype: 'image/jpeg',
      size: 1024,
      buffer: Buffer.from('jpeg'),
    } as Express.Multer.File;

    const result = await service.uploadProfilePicture(userId, file);

    expect(mkdir).toHaveBeenCalled();
    expect(writeFile).toHaveBeenCalled();
    expect(usersRepository.update).toHaveBeenCalledWith(
      userId,
      expect.objectContaining({
        profilePictureUrl: expect.stringMatching(
          /^\/uploads\/profile-pictures\//,
        ),
      }),
    );
    expect(result.profilePictureUrl).toMatch(/^\/uploads\/profile-pictures\//);
  });

  it('uploadProfilePicture rejects unsupported mime type', async () => {
    usersRepository.findById.mockResolvedValue(baseUser);

    await expect(
      service.uploadProfilePicture(userId, {
        mimetype: 'image/gif',
        size: 100,
        buffer: Buffer.from('gif'),
      } as Express.Multer.File),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('uploadProfilePicture rejects missing file', async () => {
    usersRepository.findById.mockResolvedValue(baseUser);

    await expect(
      service.uploadProfilePicture(userId, undefined as unknown as Express.Multer.File),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('uploadProfilePicture throws when user missing', async () => {
    usersRepository.findById.mockResolvedValue(undefined);

    await expect(
      service.uploadProfilePicture(userId, {
        mimetype: 'image/png',
        size: 100,
        buffer: Buffer.from('png'),
      } as Express.Multer.File),
    ).rejects.toBeInstanceOf(NotFoundException);
  });

  it('removeProfilePicture clears url and deletes old file', async () => {
    const existingUrl = '/uploads/profile-pictures/old.jpg';
    usersRepository.findById.mockResolvedValue({
      ...baseUser,
      profilePictureUrl: existingUrl,
    });
    usersRepository.update.mockResolvedValue({
      ...baseUser,
      profilePictureUrl: null,
    });

    const result = await service.removeProfilePicture(userId);

    expect(unlink).toHaveBeenCalled();
    expect(usersRepository.update).toHaveBeenCalledWith(userId, {
      profilePictureUrl: null,
    });
    expect(result.profilePictureUrl).toBeNull();
  });
});
