import { ApiProperty } from '@nestjs/swagger';
import { IsString, MaxLength, MinLength } from 'class-validator';

export class UpdateProfileDto {
  @ApiProperty({ example: 'John Doe' })
  @IsString()
  @MinLength(1)
  @MaxLength(100)
  displayName!: string;
}

export class UpdateFcmTokenDto {
  @ApiProperty({ example: 'fcm-device-token' })
  @IsString()
  @MinLength(1)
  token!: string;
}
