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
  CreateInvitationDto,
  UpdateInvitationDto,
} from './dto/invitation.dto';
import { InvitationsService } from './invitations.service';

@ApiTags('invitations')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller()
export class InvitationsController {
  constructor(private readonly invitationsService: InvitationsService) {}

  @Post('events/:eventId/invitations')
  @ApiOperation({ summary: 'Invite a user to an event (organizer only)' })
  create(
    @CurrentUser() user: SafeUser,
    @Param('eventId') eventId: string,
    @Body() dto: CreateInvitationDto,
  ) {
    return this.invitationsService.create(eventId, user.id, dto);
  }

  @Get('events/:eventId/invitations')
  @ApiOperation({ summary: 'List invitations for an event' })
  findByEvent(
    @CurrentUser() user: SafeUser,
    @Param('eventId') eventId: string,
  ) {
    return this.invitationsService.findByEvent(eventId, user.id);
  }

  @Patch('invitations/:id')
  @ApiOperation({ summary: 'Accept or reject an invitation' })
  respond(
    @CurrentUser() user: SafeUser,
    @Param('id') id: string,
    @Body() dto: UpdateInvitationDto,
  ) {
    return this.invitationsService.respond(id, user.id, dto);
  }
}
