# FriendZone Backend

NestJS REST + WebSocket API for the FriendZone university project.

## Features

- JWT authentication with refresh tokens
- Event creation and management
- Invitations by email or username
- Location sharing with global + per-event consent
- Haversine proximity detection (configurable threshold)
- Real-time updates via Socket.IO
- Push notifications via Firebase Cloud Messaging (optional)

## Prerequisites

- Node.js 20+
- Docker & Docker Compose (recommended)
- PostgreSQL 16 (if running without Docker)

## Quick Start (Docker)

```bash
cd api
cp .env.example .env
docker compose up --build
```

API: http://localhost:3000  
Swagger: http://localhost:3000/api/docs

## Local Development (without Docker)

1. Start PostgreSQL and create a database.

2. Copy environment file:

```bash
cp .env.example .env
```

3. Install dependencies:

```bash
npm install
```

4. Run migrations:

```bash
npm run db:migrate
```

5. Start dev server:

```bash
npm run start:dev
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | — |
| `JWT_SECRET` | Access token secret | — |
| `JWT_REFRESH_SECRET` | Refresh token secret | — |
| `JWT_ACCESS_EXPIRES_IN` | Access token TTL | `15m` |
| `JWT_REFRESH_EXPIRES_IN` | Refresh token TTL | `7d` |
| `ARRIVAL_THRESHOLD` | Arrival distance in meters | `500` |

Events include `trackingLeadMinutes` (default **30**): location sharing for an event is rejected until `startsAt - trackingLeadMinutes`. Set on create/update via `trackingLeadMinutes` (1–1440).
| `FIREBASE_PROJECT_ID` | Firebase project ID | — |
| `FIREBASE_CLIENT_EMAIL` | Firebase service account email | — |
| `FIREBASE_PRIVATE_KEY` | Firebase private key (use `\n` for newlines) | — |
| `PORT` | HTTP port | `3000` |

Firebase credentials are optional. Without them, push notifications are skipped in development.

## Logging

All logs go to stdout (visible in Docker).

**View API logs:**

```bash
docker compose logs -f backend
```

| Source | What it logs |
|--------|----------------|
| `HTTP` (morgan) | Every API request — method, URL, status, response time |
| `HttpExceptionFilter` | Client/server errors — `4xx` as warn, `5xx` as error, with validation messages |
| `AuthService` | Register, login, refresh, logout events |

Swagger UI static assets (`/api/docs/*`) are excluded from morgan to reduce noise.

**Examples after a failed signup:**

```
[HttpExceptionFilter] POST /auth/register 400 — password must be longer than or equal to 8 characters
[HTTP] ::ffff:172.18.0.1 - - [...] "POST /auth/register HTTP/1.1" 400 94 "-" "okhttp/4.12.0"
```

## API Overview

### Auth (public)
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

### Users
- `GET /users/me`
- `GET /users/me/friends` (confirmed friends)
- `GET /users/lookup?q=` (exact email or username)
- `PATCH /users/me`
- `PATCH /users/me/location-sharing`
- `PUT /users/me/fcm-token`
- `GET /users/search?q=`

### Friends
- `GET /friends`
- `POST /friends/requests` body `{ emailOrUsername }`
- `GET /friends/requests` (incoming pending)
- `GET /friends/requests/count`
- `PATCH /friends/requests/:id` body `{ status: accepted | rejected }`

Event invitations require an accepted friendship between organizer and invitee.

### Events
- `POST /events`
- `GET /events`
- `GET /events/:id`
- `PATCH /events/:id`
- `DELETE /events/:id`
- `POST /events/:id/leave`

### Invitations
- `POST /events/:eventId/invitations`
- `GET /events/:eventId/invitations`
- `PATCH /invitations/:id`

### Notifications (in-app inbox)
- `GET /notifications/inbox`
- `GET /notifications/badge-count`
- `PATCH /notifications/:id/read`

Every FCM push is persisted in `user_notifications` before send. Accept/reject friend requests or invitations resolves the linked inbox row.

### Locations
- `PATCH /events/:eventId/sharing`
- `POST /events/:eventId/location`
- `GET /events/:eventId/participants`

## WebSocket

Connect to the same host/port. Authenticate with JWT via:

- `auth.token` in handshake, or
- `?token=` query param, or
- `Authorization: Bearer <token>` header

Subscribe to an event room:

```json
{ "event": "join", "data": { "eventId": "<uuid>" } }
```

Events emitted on room `event:{eventId}`:

- `location.updated`
- `participant.joined`
- `participant.arrived`
- `event.completed`

## Location Sharing Rules

1. Global sharing must be enabled via `PATCH /users/me/location-sharing`.
2. Per-event sharing can be toggled via `PATCH /events/:id/sharing`.
3. Per-event off does not disable global setting.
4. Global off blocks all location sharing.
5. Only users actively sharing count toward arrival detection.

## Deployment (Render / Railway)

**Demo deployment (free tier):** see **[docs/DEPLOY_RENDER_NEON.md](docs/DEPLOY_RENDER_NEON.md)** for Neon Postgres + Render + Android APK steps and [`scripts/deploy-render-neon.sh`](scripts/deploy-render-neon.sh).

Legacy notes (any host):

1. Create a PostgreSQL instance.
2. Set all environment variables.
3. Build command: `npm run build`
4. Start command: `npm run start:render` (runs migrations first) or `npm run start:prod` if migrations run separately
5. For Firebase on Render/Railway, paste the private key with `\n` escaped newlines.

## Scripts

```bash
npm run start:dev      # Development with hot reload
npm run build          # Production build
npm run start:prod     # Run production build
npm run db:generate    # Generate Drizzle migrations (after schema edits)
npm run db:migrate     # Apply pending migrations
```

### Migration workflow

1. Edit `drizzle/schema/index.ts`
2. Run `npm run db:generate` — creates a new `000N_*.sql` only when the schema differs from the latest snapshot in `drizzle/migrations/meta/`
3. Run `npm run db:migrate`

Do **not** run `db:generate` to “catch up” after hand-written SQL migrations unless `meta/NNNN_snapshot.json` matches the latest journal entry. Otherwise Drizzle diffs against an old snapshot and regenerates already-applied tables (e.g. `friend_request_status already exists`).

If migrate fails with “already exists” but objects are in the DB, check what Drizzle has recorded:

```bash
docker compose exec postgres psql -U friendzone -d friendzone \
  -c "SELECT * FROM drizzle.__drizzle_migrations ORDER BY id;"
```

Remove any duplicate `0004_*.sql` files and extra journal entries; keep migrations `0000`–`0003` only.

### Firebase warning (unrelated to migrate)

`Firebase credentials not configured — push notifications disabled` is expected in local dev without `FIREBASE_*` env vars. In-app notifications still work; only device push is skipped.

## Project Structure

```
src/
├── auth/           # Registration, login, JWT
├── users/          # Profiles, search, FCM tokens
├── events/         # Event CRUD
├── invitations/    # Invite flow
├── locations/      # Location updates, proximity
├── notifications/  # FCM abstraction
├── websocket/      # Socket.IO gateway
├── common/         # Guards, filters, utils
├── config/         # Env validation
├── drizzle/        # DB module
└── main.ts
```
