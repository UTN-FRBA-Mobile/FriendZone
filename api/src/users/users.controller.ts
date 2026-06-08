import {
  Body,
  Controller,
  Get,
  Patch,
  Put,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import type { SafeUser } from '../common/utils/user.mapper';
import { SearchUsersQueryDto } from './dto/search-users.dto';
import { UpdateLocationSharingDto } from './dto/update-location-sharing.dto';
import {
  UpdateFcmTokenDto,
  UpdateProfileDto,
} from './dto/update-profile.dto';
import { UsersService } from './users.service';

@ApiTags('users')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get('me')
  @ApiOperation({ summary: 'Get current user profile' })
  getMe(@CurrentUser() user: SafeUser): SafeUser {
    return user;
  }

  @Patch('me')
  @ApiOperation({ summary: 'Update current user profile' })
  updateMe(
    @CurrentUser() user: SafeUser,
    @Body() dto: UpdateProfileDto,
  ): Promise<SafeUser> {
    return this.usersService.updateProfile(user.id, dto.displayName);
  }

  @Patch('me/location-sharing')
  @ApiOperation({ summary: 'Toggle global location sharing' })
  updateLocationSharing(
    @CurrentUser() user: SafeUser,
    @Body() dto: UpdateLocationSharingDto,
  ): Promise<SafeUser> {
    return this.usersService.updateLocationSharing(user.id, dto.enabled);
  }

  @Put('me/fcm-token')
  @ApiOperation({ summary: 'Update FCM device token' })
  updateFcmToken(
    @CurrentUser() user: SafeUser,
    @Body() dto: UpdateFcmTokenDto,
  ): Promise<SafeUser> {
    return this.usersService.updateFcmToken(user.id, dto.token);
  }

  @Get('search')
  @ApiOperation({ summary: 'Search users by email or username prefix' })
  search(@Query() query: SearchUsersQueryDto): Promise<SafeUser[]> {
    return this.usersService.search(query.q ?? '');
  }
}
