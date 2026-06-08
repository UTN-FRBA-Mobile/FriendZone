import { ApiProperty } from '@nestjs/swagger';
import { IsIn, IsString, MinLength } from 'class-validator';

export class CreateFriendRequestDto {
  @ApiProperty({ example: 'alex@mail.com' })
  @IsString()
  @MinLength(1)
  emailOrUsername!: string;
}

export class RespondFriendRequestDto {
  @ApiProperty({ enum: ['accepted', 'rejected'] })
  @IsIn(['accepted', 'rejected'])
  status!: 'accepted' | 'rejected';
}
