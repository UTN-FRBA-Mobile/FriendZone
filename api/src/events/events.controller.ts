import {
  Body,
  Controller,
  Delete,
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
import { CreateEventDto, UpdateEventDto } from './dto/event.dto';
import { EventsService } from './events.service';

@ApiTags('events')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('events')
export class EventsController {
  constructor(private readonly eventsService: EventsService) {}

  @Post()
  @ApiOperation({ summary: 'Create a new event' })
  create(@CurrentUser() user: SafeUser, @Body() dto: CreateEventDto) {
    return this.eventsService.create(user.id, dto);
  }

  @Get()
  @ApiOperation({ summary: 'List events for current user' })
  findMine(@CurrentUser() user: SafeUser) {
    return this.eventsService.findMine(user.id);
  }

  @Get(':id')
  @ApiOperation({ summary: 'Get event by ID' })
  findOne(@CurrentUser() user: SafeUser, @Param('id') id: string) {
    return this.eventsService.findById(id, user.id);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update event (organizer only)' })
  update(
    @CurrentUser() user: SafeUser,
    @Param('id') id: string,
    @Body() dto: UpdateEventDto,
  ) {
    return this.eventsService.update(id, user.id, dto);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Delete event (organizer only)' })
  async remove(@CurrentUser() user: SafeUser, @Param('id') id: string) {
    await this.eventsService.delete(id, user.id);
    return { success: true };
  }
}
