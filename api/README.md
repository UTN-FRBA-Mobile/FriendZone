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
| `FIREBASE_PROJECT_ID` | Firebase project ID | — |
| `FIREBASE_CLIENT_EMAIL` | Firebase service account email | — |
| `FIREBASE_PRIVATE_KEY` | Firebase private key (use `\n` for newlines) | — |
| `PORT` | HTTP port | `3000` |

Firebase credentials are optional. Without them, push notifications are skipped in development.

## API Overview

### Auth (public)
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

### Users
- `GET /users/me`
- `PATCH /users/me`
- `PATCH /users/me/location-sharing`
- `PUT /users/me/fcm-token`
- `GET /users/search?q=`

### Events
- `POST /events`
- `GET /events`
- `GET /events/:id`
- `PATCH /events/:id`
- `DELETE /events/:id`

### Invitations
- `POST /events/:eventId/invitations`
- `GET /events/:eventId/invitations`
- `PATCH /invitations/:id`

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

1. Create a PostgreSQL instance.
2. Set all environment variables.
3. Build command: `npm run build`
4. Start command: `npm run start:prod` (run migrations first or use entrypoint script)
5. For Firebase on Render/Railway, paste the private key with `\n` escaped newlines.

## Scripts

```bash
npm run start:dev      # Development with hot reload
npm run build          # Production build
npm run start:prod     # Run production build
npm run db:generate    # Generate Drizzle migrations
npm run db:migrate     # Apply migrations
```

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
