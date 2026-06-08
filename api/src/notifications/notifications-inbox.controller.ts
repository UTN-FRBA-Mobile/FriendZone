import { Controller, Get, Param, Patch, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import type { SafeUser } from '../common/utils/user.mapper';
import { InboxNotificationDto } from './dto/inbox.dto';
import { NotificationsInboxService } from './notifications-inbox.service';

@ApiTags('notifications')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('notifications')
export class NotificationsInboxController {
  constructor(
    private readonly notificationsInboxService: NotificationsInboxService,
  ) {}

  @Get('inbox')
  @ApiOperation({ summary: 'List inbox notifications for current user' })
  getInbox(@CurrentUser() user: SafeUser): Promise<InboxNotificationDto[]> {
    return this.notificationsInboxService.getInbox(user.id);
  }

  @Get('badge-count')
  @ApiOperation({ summary: 'Count of inbox-visible notifications' })
  getBadgeCount(@CurrentUser() user: SafeUser): Promise<{ count: number }> {
    return this.notificationsInboxService
      .getBadgeCount(user.id)
      .then((count) => ({ count }));
  }

  @Patch(':id/read')
  @ApiOperation({ summary: 'Mark an informational notification as read' })
  markRead(
    @CurrentUser() user: SafeUser,
    @Param('id') id: string,
  ): Promise<InboxNotificationDto> {
    return this.notificationsInboxService.markRead(user.id, id);
  }
}
