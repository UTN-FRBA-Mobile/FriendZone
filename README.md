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

### Testing

Tests cover critical app logic only (no API/backend tests). Each test targets one behavior class rather than repeating the same rule with different inputs.

**Run from `android/`:**

```bash
./gradlew testDebugUnitTest              # JVM unit tests (no device)
./gradlew connectedDebugAndroidTest      # Instrumented tests (emulator or device)
```

**Unit tests** (`app/src/test/`) — domain utils, error messages, UI mappers, and ViewModels for auth, events, and friends. Stack: JUnit 4, Mockito, `kotlinx-coroutines-test`. Shared helpers live in `app/src/test/java/com/example/friendzone/testutil/`.

**Instrumented tests** (`app/src/androidTest/`) — Compose smoke checks (login screen, bottom nav, events shell) and deep-link parsing. Uses `mockito-android` (required on device/emulator). No network or real API calls.

Reports: `app/build/reports/tests/`.
W