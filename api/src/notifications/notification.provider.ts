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
  FRIEND_REQUEST = 'friend.request',
}

export const ACTIONABLE_NOTIFICATION_TYPES: NotificationType[] = [
  NotificationType.FRIEND_REQUEST,
  NotificationType.INVITATION_CREATED,
];

export function isActionableNotificationType(type: string): boolean {
  return (ACTIONABLE_NOTIFICATION_TYPES as string[]).includes(type);
}
