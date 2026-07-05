import {
  boolean,
  doublePrecision,
  index,
  integer,
  jsonb,
  pgEnum,
  pgTable,
  text,
  timestamp,
  uniqueIndex,
  uuid,
  varchar,
} from 'drizzle-orm/pg-core';

export const eventStatusEnum = pgEnum('event_status', [
  'scheduled',
  'active',
  'completed',
  'cancelled',
]);

export const invitationStatusEnum = pgEnum('invitation_status', [
  'pending',
  'accepted',
  'rejected',
]);

export const participantRoleEnum = pgEnum('participant_role', [
  'organizer',
  'participant',
]);

export const friendRequestStatusEnum = pgEnum('friend_request_status', [
  'pending',
  'accepted',
  'rejected',
]);

export const users = pgTable(
  'users',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    email: varchar('email', { length: 255 }).notNull(),
    username: varchar('username', { length: 50 }).notNull(),
    passwordHash: varchar('password_hash', { length: 255 }).notNull(),
    displayName: varchar('display_name', { length: 100 }).notNull(),
    fcmToken: text('fcm_token'),
    locationSharingEnabled: boolean('location_sharing_enabled')
      .notNull()
      .default(false),
    createdAt: timestamp('created_at', { withTimezone: true })
      .notNull()
      .defaultNow(),
  },
  (table) => [
    uniqueIndex('users_email_idx').on(table.email),
    uniqueIndex('users_username_idx').on(table.username),
  ],
);

export const events = pgTable('events', {
  id: uuid('id').primaryKey().defaultRandom(),
  organizerId: uuid('organizer_id')
    .notNull()
    .references(() => users.id, { onDelete: 'cascade' }),
  title: varchar('title', { length: 200 }).notNull(),
  description: text('description'),
  latitude: doublePrecision('latitude').notNull(),
  longitude: doublePrecision('longitude').notNull(),
  address: varchar('address', { length: 500 }),
  status: eventStatusEnum('status').notNull().default('scheduled'),
  arrivalThresholdM: integer('arrival_threshold_m').notNull().default(150),
  trackingLeadMinutes: integer('tracking_lead_minutes').notNull().default(30),
  coverImageUrl: text('cover_image_url'),
  startsAt: timestamp('starts_at', { withTimezone: true }).notNull(),
  completedAt: timestamp('completed_at', { withTimezone: true }),
  createdAt: timestamp('created_at', { withTimezone: true })
    .notNull()
    .defaultNow(),
});

export const invitations = pgTable(
  'invitations',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    eventId: uuid('event_id')
      .notNull()
      .references(() => events.id, { onDelete: 'cascade' }),
    inviteeId: uuid('invitee_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    invitedById: uuid('invited_by_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    status: invitationStatusEnum('status').notNull().default('pending'),
    createdAt: timestamp('created_at', { withTimezone: true })
      .notNull()
      .defaultNow(),
  },
  (table) => [
    uniqueIndex('invitations_event_invitee_idx').on(
      table.eventId,
      table.inviteeId,
    ),
  ],
);

export const eventParticipants = pgTable(
  'event_participants',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    eventId: uuid('event_id')
      .notNull()
      .references(() => events.id, { onDelete: 'cascade' }),
    userId: uuid('user_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    role: participantRoleEnum('role').notNull().default('participant'),
    sharingLocation: boolean('sharing_location').notNull().default(false),
    arrived: boolean('arrived').notNull().default(false),
    lastLatitude: doublePrecision('last_latitude'),
    lastLongitude: doublePrecision('last_longitude'),
    lastLocationAt: timestamp('last_location_at', { withTimezone: true }),
    arrivedAt: timestamp('arrived_at', { withTimezone: true }),
    createdAt: timestamp('created_at', { withTimezone: true })
      .notNull()
      .defaultNow(),
  },
  (table) => [
    uniqueIndex('event_participants_event_user_idx').on(
      table.eventId,
      table.userId,
    ),
  ],
);

export const friendRequests = pgTable(
  'friend_requests',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    requesterId: uuid('requester_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    addresseeId: uuid('addressee_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    status: friendRequestStatusEnum('status').notNull().default('pending'),
    createdAt: timestamp('created_at', { withTimezone: true })
      .notNull()
      .defaultNow(),
    respondedAt: timestamp('responded_at', { withTimezone: true }),
  },
  (table) => [
    uniqueIndex('friend_requests_requester_addressee_idx').on(
      table.requesterId,
      table.addresseeId,
    ),
  ],
);

export const friendships = pgTable(
  'friendships',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    userIdLow: uuid('user_id_low')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    userIdHigh: uuid('user_id_high')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    createdAt: timestamp('created_at', { withTimezone: true })
      .notNull()
      .defaultNow(),
  },
  (table) => [
    uniqueIndex('friendships_user_pair_idx').on(
      table.userIdLow,
      table.userIdHigh,
    ),
  ],
);

export const userNotifications = pgTable(
  'user_notifications',
  {
    id: uuid('id').primaryKey().defaultRandom(),
    userId: uuid('user_id')
      .notNull()
      .references(() => users.id, { onDelete: 'cascade' }),
    type: varchar('type', { length: 50 }).notNull(),
    title: varchar('title', { length: 255 }).notNull(),
    body: text('body').notNull(),
    data: jsonb('data').$type<Record<string, string>>(),
    readAt: timestamp('read_at', { withTimezone: true }),
    resolvedAt: timestamp('resolved_at', { withTimezone: true }),
    createdAt: timestamp('created_at', { withTimezone: true })
      .notNull()
      .defaultNow(),
  },
  (table) => [index('user_notifications_user_inbox_idx').on(table.userId)],
);

export const refreshTokens = pgTable('refresh_tokens', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id')
    .notNull()
    .references(() => users.id, { onDelete: 'cascade' }),
  tokenHash: varchar('token_hash', { length: 255 }).notNull(),
  expiresAt: timestamp('expires_at', { withTimezone: true }).notNull(),
  createdAt: timestamp('created_at', { withTimezone: true })
    .notNull()
    .defaultNow(),
});

export type User = typeof users.$inferSelect;
export type NewUser = typeof users.$inferInsert;
export type Event = typeof events.$inferSelect;
export type NewEvent = typeof events.$inferInsert;
export type Invitation = typeof invitations.$inferSelect;
export type EventParticipant = typeof eventParticipants.$inferSelect;
export type NewRefreshToken = typeof refreshTokens.$inferInsert;
export type RefreshToken = typeof refreshTokens.$inferSelect;
export type NewInvitation = typeof invitations.$inferInsert;
export type FriendRequest = typeof friendRequests.$inferSelect;
export type NewFriendRequest = typeof friendRequests.$inferInsert;
export type Friendship = typeof friendships.$inferSelect;
export type NewFriendship = typeof friendships.$inferInsert;
export type UserNotification = typeof userNotifications.$inferSelect;
export type NewUserNotification = typeof userNotifications.$inferInsert;
