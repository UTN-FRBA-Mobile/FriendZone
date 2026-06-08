import { ApiProperty } from '@nestjs/swagger';
import { IsEnum, IsString, MinLength } from 'class-validator';

export class CreateInvitationDto {
  @ApiProperty({ example: 'jane@university.edu' })
  @IsString()
  @MinLength(1)
  emailOrUsername!: string;
}

export class UpdateInvitationDto {
  @ApiProperty({ enum: ['accepted', 'rejected'] })
  @IsEnum(['accepted', 'rejected'] as const)
  status!: 'accepted' | 'rejected';
}
