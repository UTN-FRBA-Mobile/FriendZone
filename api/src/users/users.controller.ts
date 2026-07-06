import {
  Body,
  Controller,
  Delete,
  Get,
  Patch,
  Post,
  Put,
  Query,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { ApiBearerAuth, ApiConsumes, ApiOperation, ApiTags } from '@nestjs/swagger';
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

  @Delete('me')
  @ApiOperation({ summary: 'Delete current user account' })
  async deleteMe(@CurrentUser() user: SafeUser): Promise<SafeUser> {
    return this.usersService.deleteAccount(user.id);
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

  @Post('me/profile-picture')
  @ApiOperation({ summary: 'Upload profile picture' })
  @ApiConsumes('multipart/form-data')
  @UseInterceptors(FileInterceptor('picture'))
  uploadProfilePicture(
    @CurrentUser() user: SafeUser,
    @UploadedFile() file: Express.Multer.File,
  ): Promise<SafeUser> {
    return this.usersService.uploadProfilePicture(user.id, file);
  }

  @Delete('me/profile-picture')
  @ApiOperation({ summary: 'Remove profile picture' })
  removeProfilePicture(@CurrentUser() user: SafeUser): Promise<SafeUser> {
    return this.usersService.removeProfilePicture(user.id);
  }

  @Get('me/friends')
  @ApiOperation({ summary: 'List confirmed friends' })
  getMyFriends(@CurrentUser() user: SafeUser): Promise<SafeUser[]> {
    return this.usersService.getFriends(user.id);
  }

  @Get('lookup')
  @ApiOperation({ summary: 'Look up a user by exact email or username' })
  lookup(
    @CurrentUser() user: SafeUser,
    @Query() query: SearchUsersQueryDto,
  ): Promise<SafeUser> {
    return this.usersService.lookup(user.id, query.q ?? '');
  }

  @Get('search')
  @ApiOperation({ summary: 'Search users by email or username prefix' })
  search(@Query() query: SearchUsersQueryDto): Promise<SafeUser[]> {
    return this.usersService.search(query.q ?? '');
  }
}
