import { ApiProperty } from '@nestjs/swagger';
import { NotificationType } from '../notification.provider';

export class InboxNotificationDto {
  @ApiProperty()
  id!: string;

  @ApiProperty({ enum: NotificationType })
  type!: NotificationType;

  @ApiProperty()
  title!: string;

  @ApiProperty()
  body!: string;

  @ApiProperty()
  createdAt!: string;

  @ApiProperty()
  actionable!: boolean;

  @ApiProperty()
  read!: boolean;

  @ApiProperty({ type: 'object', additionalProperties: { type: 'string' } })
  data!: Record<string, string>;
}
