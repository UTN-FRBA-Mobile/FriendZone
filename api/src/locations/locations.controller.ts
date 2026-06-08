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
import { UpdateLocationDto, UpdateSharingDto } from './dto/location.dto';
import { LocationsService } from './locations.service';

@ApiTags('locations')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('events/:eventId')
export class LocationsController {
  constructor(private readonly locationsService: LocationsService) {}

  @Patch('sharing')
  @ApiOperation({ summary: 'Toggle location sharing for this event' })
  updateSharing(
    @CurrentUser() user: SafeUser,
    @Param('eventId') eventId: string,
    @Body() dto: UpdateSharingDto,
  ) {
    return this.locationsService.updateSharing(eventId, user.id, dto);
  }

  @Post('location')
  @ApiOperation({ summary: 'Share current location for an event' })
  updateLocation(
    @CurrentUser() user: SafeUser,
    @Param('eventId') eventId: string,
    @Body() dto: UpdateLocationDto,
  ) {
    return this.locationsService.updateLocation(eventId, user.id, dto);
  }

  @Get('participants')
  @ApiOperation({ summary: 'List event participants with locations' })
  getParticipants(
    @CurrentUser() user: SafeUser,
    @Param('eventId') eventId: string,
  ) {
    return this.locationsService.getParticipants(eventId, user.id);
  }
}
