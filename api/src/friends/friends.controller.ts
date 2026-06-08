import {
  Body,
  Controller,
  Get,
  Param,
  Patch,
  Post,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import type { SafeUser } from '../common/utils/user.mapper';
import {
  CreateFriendRequestDto,
  RespondFriendRequestDto,
} from './dto/friend.dto';
import { FriendsService } from './friends.service';

@ApiTags('friends')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('friends')
export class FriendsController {
  constructor(private readonly friendsService: FriendsService) {}

  @Get()
  @ApiOperation({ summary: 'List confirmed friends' })
  listFriends(@CurrentUser() user: SafeUser): Promise<SafeUser[]> {
    return this.friendsService.listFriends(user.id);
  }

  @Get('requests')
  @ApiOperation({ summary: 'List incoming pending friend requests' })
  listIncomingRequests(@CurrentUser() user: SafeUser) {
    return this.friendsService.listIncomingRequests(user.id);
  }

  @Get('requests/count')
  @ApiOperation({ summary: 'Count incoming pending friend requests' })
  countIncomingRequests(@CurrentUser() user: SafeUser): Promise<{ count: number }> {
    return this.friendsService
      .countIncomingRequests(user.id)
      .then((count) => ({ count }));
  }

  @Post('requests')
  @ApiOperation({ summary: 'Send a friend request' })
  createRequest(
    @CurrentUser() user: SafeUser,
    @Body() dto: CreateFriendRequestDto,
  ) {
    return this.friendsService.createRequest(user.id, dto);
  }

  @Patch('requests/:id')
  @ApiOperation({ summary: 'Accept or reject a friend request' })
  respondToRequest(
    @CurrentUser() user: SafeUser,
    @Param('id') requestId: string,
    @Body() dto: RespondFriendRequestDto,
  ): Promise<{ success: boolean }> {
    return this.friendsService
      .respondToRequest(user.id, requestId, dto)
      .then(() => ({ success: true }));
  }
}
