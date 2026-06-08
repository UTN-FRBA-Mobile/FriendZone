import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { Request } from 'express';
import { SafeUser } from '../utils/user.mapper';

export interface AuthenticatedRequest extends Request {
  user: SafeUser;
}

export const CurrentUser = createParamDecorator(
  (_data: unknown, ctx: ExecutionContext) => {
    const request = ctx.switchToHttp().getRequest<AuthenticatedRequest>();
    return request.user;
  },
);
