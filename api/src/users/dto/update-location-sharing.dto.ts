import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean } from 'class-validator';

export class UpdateLocationSharingDto {
  @ApiProperty({ example: true })
  @IsBoolean()
  enabled!: boolean;
}
