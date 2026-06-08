# FriendZone

FriendZone is a university project for organizing social events and tracking when invited participants arrive at the meeting point.

This repo is split into two parts:

| Directory | Description |
|-----------|-------------|
| [`android/`](android/) | Kotlin + Jetpack Compose mobile app |
| [`api/`](api/) | NestJS backend (REST, WebSocket, PostgreSQL) |

## Backend (API)

The backend handles authentication, events, invitations, location sharing, proximity detection, real-time sync over Socket.IO, and optional push notifications via Firebase.

**Quick start with Docker:**

```bash
cd api
cp .env.example .env
docker compose up --build
```

Once running:

- API: http://localhost:3000
- Swagger docs: http://localhost:3000/api/docs

Firebase is optional — the server runs without it; only push notifications are disabled.

For local dev without Docker, environment variables, WebSocket usage, deployment notes, and the full API reference, see **[api/README.md](api/README.md)**.

## Android App

The Android client lives in [`android/`](android/). Open that folder in Android Studio to build and run the app.
