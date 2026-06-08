import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as admin from 'firebase-admin';
import {
  NotificationPayload,
  NotificationProvider,
} from './notification.provider';

@Injectable()
export class FirebaseNotificationProvider
  implements NotificationProvider, OnModuleInit
{
  private readonly logger = new Logger(FirebaseNotificationProvider.name);
  private initialized = false;

  constructor(private readonly configService: ConfigService) {}

  onModuleInit() {
    const projectId = this.configService.get<string>('FIREBASE_PROJECT_ID');
    const clientEmail = this.configService.get<string>('FIREBASE_CLIENT_EMAIL');
    const privateKey = this.configService
      .get<string>('FIREBASE_PRIVATE_KEY')
      ?.replace(/\\n/g, '\n');

    if (!projectId || !clientEmail || !privateKey) {
      this.logger.warn(
        'Firebase credentials not configured — push notifications disabled',
      );
      return;
    }

    try {
      admin.initializeApp({
        credential: admin.credential.cert({
          projectId,
          clientEmail,
          privateKey,
        }),
      });
      this.initialized = true;
      this.logger.log('Firebase Admin initialized');
    } catch (error) {
      this.logger.error('Failed to initialize Firebase Admin', error);
    }
  }

  async send(token: string, payload: NotificationPayload): Promise<void> {
    if (!this.initialized) {
      this.logger.debug(
        `Skipping FCM send (not configured): ${payload.title}`,
      );
      return;
    }

    try {
      await admin.messaging().send({
        token,
        notification: {
          title: payload.title,
          body: payload.body,
        },
        data: payload.data,
      });
    } catch (error) {
      this.logger.error(`FCM send failed for token ${token}`, error);
    }
  }
}
