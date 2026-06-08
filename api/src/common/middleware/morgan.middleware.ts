import { INestApplication, Logger } from '@nestjs/common';
import morgan from 'morgan';
import type { Request } from 'express';

const httpLogger = new Logger('HTTP');

function shouldSkipRequest(req: Request): boolean {
  const url = req.originalUrl ?? req.url ?? '';
  return (
    url.startsWith('/api/docs') ||
    url === '/api/docs-json' ||
    url === '/favicon.ico'
  );
}

export function configureMorgan(app: INestApplication): void {
  const isProduction = process.env.NODE_ENV === 'production';
  const format = isProduction
    ? 'combined'
    : ':method :url :status :res[content-length] - :response-time ms';

  app.use(
    morgan(format, {
      skip: shouldSkipRequest,
      stream: {
        write: (message: string) => {
          httpLogger.log(message.trim());
        },
      },
    }),
  );
}
