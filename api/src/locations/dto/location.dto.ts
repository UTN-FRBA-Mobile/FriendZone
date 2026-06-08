import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean, IsNumber, Max, Min } from 'class-validator';

export class UpdateLocationDto {
  @ApiProperty({ example: 40.7128 })
  @IsNumber()
  @Min(-90)
  @Max(90)
  latitude!: number;

  @ApiProperty({ example: -74.006 })
  @IsNumber()
  @Min(-180)
  @Max(180)
  longitude!: number;
}

export class UpdateSharingDto {
  @ApiProperty({ example: true })
  @IsBoolean()
  enabled!: boolean;
}
