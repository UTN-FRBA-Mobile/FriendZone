import {
  ConflictException,
  Injectable,
  Logger,
  UnauthorizedException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { UsersRepository } from '../users/users.repository';
import { AuthRepository } from './auth.repository';
import { AuthResponseDto, LoginDto, RegisterDto } from './dto/auth.dto';
import { JwtPayload } from './strategies/jwt.strategy';
import { toSafeUser } from '../common/utils/user.mapper';

const BCRYPT_ROUNDS = 12;

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    private readonly usersRepository: UsersRepository,
    private readonly authRepository: AuthRepository,
    private readonly jwtService: JwtService,
    private readonly configService: ConfigService,
  ) {}

  async register(dto: RegisterDto): Promise<AuthResponseDto> {
    const email = dto.email.toLowerCase();
    const username = dto.username.toLowerCase();

    const existingEmail = await this.usersRepository.findByEmail(email);
    if (existingEmail) {
      this.logger.warn('Register rejected — email already registered');
      throw new ConflictException('Email already registered');
    }

    const existingUsername =
      await this.usersRepository.findByUsername(username);
    if (existingUsername) {
      this.logger.warn('Register rejected — username already taken');
      throw new ConflictException('Username already taken');
    }

    const passwordHash = await bcrypt.hash(dto.password, BCRYPT_ROUNDS);

    const user = await this.usersRepository.create({
      email,
      username,
      passwordHash,
      displayName: dto.displayName,
    });

    this.logger.log(
      `User registered userId=${user.id} username=${user.username}`,
    );

    return this.issueTokens(
      user.id,
      user.email,
      user.displayName,
      user.username,
      user.locationSharingEnabled,
    );
  }

  async login(dto: LoginDto): Promise<AuthResponseDto> {
    const user = await this.usersRepository.findByEmailOrUsername(
      dto.emailOrUsername,
    );
    if (!user) {
      this.logger.warn(
        `Login failed for identifier=${dto.emailOrUsername.toLowerCase()}`,
      );
      throw new UnauthorizedException('Invalid credentials');
    }

    const valid = await bcrypt.compare(dto.password, user.passwordHash);
    if (!valid) {
      this.logger.warn(
        `Login failed for identifier=${dto.emailOrUsername.toLowerCase()}`,
      );
      throw new UnauthorizedException('Invalid credentials');
    }

    this.logger.log(`User logged in userId=${user.id}`);

    return this.issueTokens(
      user.id,
      user.email,
      user.displayName,
      user.username,
      user.locationSharingEnabled,
    );
  }

  async refresh(refreshToken: string): Promise<AuthResponseDto> {
    const tokenHash = this.authRepository.hashToken(refreshToken);
    const stored = await this.authRepository.findRefreshToken(tokenHash);

    if (!stored || stored.expiresAt < new Date()) {
      this.logger.warn('Token refresh failed');
      throw new UnauthorizedException('Invalid refresh token');
    }

    let payload: JwtPayload;
    try {
      payload = this.jwtService.verify<JwtPayload>(refreshToken, {
        secret: this.configService.getOrThrow<string>('JWT_REFRESH_SECRET'),
      });
    } catch {
      this.logger.warn('Token refresh failed');
      throw new UnauthorizedException('Invalid refresh token');
    }

    const user = await this.usersRepository.findById(payload.sub);
    if (!user) {
      this.logger.warn('Token refresh failed');
      throw new UnauthorizedException('Invalid refresh token');
    }

    await this.authRepository.deleteRefreshToken(tokenHash);

    this.logger.debug(`Token refreshed userId=${user.id}`);

    return this.issueTokens(
      user.id,
      user.email,
      user.displayName,
      user.username,
      user.locationSharingEnabled,
    );
  }

  async logout(refreshToken: string): Promise<void> {
    const tokenHash = this.authRepository.hashToken(refreshToken);
    await this.authRepository.deleteRefreshToken(tokenHash);
    this.logger.log('User logged out');
  }

  private async issueTokens(
    userId: string,
    email: string,
    displayName: string,
    username: string,
    locationSharingEnabled: boolean,
  ): Promise<AuthResponseDto> {
    const payload: JwtPayload = { sub: userId, email };

    const accessToken = this.jwtService.sign(payload, {
      secret: this.configService.getOrThrow<string>('JWT_SECRET'),
      expiresIn: this.configService.get<string>(
        'JWT_ACCESS_EXPIRES_IN',
        '15m',
      ) as `${number}${'s' | 'm' | 'h' | 'd'}`,
    });

    const refreshToken = this.jwtService.sign(payload, {
      secret: this.configService.getOrThrow<string>('JWT_REFRESH_SECRET'),
      expiresIn: this.configService.get<string>(
        'JWT_REFRESH_EXPIRES_IN',
        '7d',
      ) as `${number}${'s' | 'm' | 'h' | 'd'}`,
    });

    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 7);

    await this.authRepository.saveRefreshToken({
      userId,
      tokenHash: this.authRepository.hashToken(refreshToken),
      expiresAt,
    });

    return {
      accessToken,
      refreshToken,
      user: {
        id: userId,
        email,
        username,
        displayName,
        locationSharingEnabled,
      },
    };
  }
}
