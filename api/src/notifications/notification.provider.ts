export interface NotificationPayload {
  title: string;
  body: string;
  data?: Record<string, string>;
}

export interface NotificationProvider {
  send(token: string, payload: NotificationPayload): Promise<void>;
}

export const NOTIFICATION_PROVIDER = Symbol('NOTIFICATION_PROVIDER');

export enum NotificationType {
  INVITATION_CREATED = 'invitation.created',
  PARTICIPANT_ARRIVED = 'participant.arrived',
  EVENT_COMPLETED = 'event.completed',
}
