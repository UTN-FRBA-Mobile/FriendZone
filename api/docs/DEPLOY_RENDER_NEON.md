# Deploy FriendZone (Render + Neon)

Free-tier demo deployment for the FriendZone backend and Android APK.

| Component | Service | Cost |
|-----------|---------|------|
| PostgreSQL | [Neon](https://neon.tech) | $0 (permanent free tier) |
| NestJS API + Socket.IO | [Render](https://render.com) free web service | $0 |
| Android APK | GitHub Releases / Drive | $0 |

**Good for:** low-traffic demos (~3 users), sessions of a few hours.

**Tradeoffs:** Render sleeps after ~15 minutes without traffic (cold start ~30–60s). The Android app re-joins Socket.IO rooms after reconnect (see `EventSocketManager`).

---

## Prerequisites

- GitHub repo with this project pushed
- [Neon](https://neon.tech) account (no credit card)
- [Render](https://render.com) account (no credit card)
- Node.js 20+ locally (for helper script / optional local migrate)
- Android Studio or JDK 11+ to build the APK

---

## 1. Create the Neon database

1. Sign in at [console.neon.tech](https://console.neon.tech).
2. **New Project** → pick a region close to your Render region.
3. Open **Dashboard → Connection details**.
4. Copy the **connection string** (direct connection is fine for Render).
5. Ensure the URL includes SSL, e.g.:

   ```text
   postgresql://USER:PASSWORD@ep-xxx.region.aws.neon.tech/neondb?sslmode=require
   ```

6. Save it — you will paste it into Render as `DATABASE_URL`.

Neon free tier scales Postgres to zero after ~5 minutes idle. The first query after idle may take 1–2 seconds. That is normal for demos.

---

## 2. Deploy the API on Render

### Option A — Blueprint (recommended)

1. Push this repo to GitHub.
2. In Render: **New → Blueprint**.
3. Connect the repository.
4. Render reads [`render.yaml`](../../render.yaml) at the repo root.
5. When prompted, set **`DATABASE_URL`** to your Neon connection string.
6. Render auto-generates `JWT_SECRET` and `JWT_REFRESH_SECRET` (or set your own).
7. Apply the Blueprint and wait for the first deploy.

### Option B — Manual web service

1. **New → Web Service** → connect GitHub repo.
2. Settings:

   | Field | Value |
   |-------|-------|
   | Root Directory | `api` |
   | Runtime | Node |
   | Build Command | `npm ci && npm run build` |
   | Start Command | `npm run start:render` |
   | Plan | Free |

3. **Environment** — add variables from [`api/.env.example`](../.env.example). Minimum:

   ```env
   NODE_ENV=production
   DATABASE_URL=postgresql://...neon.tech/...?sslmode=require
   JWT_SECRET=<long-random-string>
   JWT_REFRESH_SECRET=<another-long-random-string>
   JWT_ACCESS_EXPIRES_IN=15m
   JWT_REFRESH_EXPIRES_IN=7d
   ARRIVAL_THRESHOLD=500
   PORT=3000
   ```

   Firebase vars are optional (push notifications). In-app notifications work without them.

4. Deploy. Note your URL, e.g. `https://friendzone-api.onrender.com`.

### Migrations

`npm run start:render` runs Drizzle migrations before starting the server (same as Docker entrypoint). Every Render deploy applies pending migrations automatically.

To run migrations locally against Neon:

```bash
cd api
chmod +x scripts/deploy-render-neon.sh
DATABASE_URL='postgresql://...?sslmode=require' ./scripts/deploy-render-neon.sh migrate
```

---

## 3. Verify the backend

```bash
API_URL='https://YOUR-SERVICE.onrender.com' ./scripts/deploy-render-neon.sh smoke
```

Or manually:

1. Open `https://YOUR-SERVICE.onrender.com/api/docs` (first load may take up to ~60s on cold start).
2. Try **POST /auth/register** in Swagger.

---

## 4. Build the Android APK

The release build must point at your **HTTPS** Render URL.

Edit `android/app/build.gradle.kts` — `release` build type:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://YOUR-SERVICE.onrender.com/\"")
```

Build:

```bash
cd android
./gradlew assembleRelease
```

Output: `android/app/build/outputs/apk/release/app-release-unsigned.apk`

Sign the APK for installation on real devices (debug keystore is acceptable for a classroom demo):

```bash
# One-time debug signing example (from android/ directory)
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore ~/.android/debug.keystore -storepass android \
  app/build/outputs/apk/release/app-release-unsigned.apk androiddebugkey

zipalign -f 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/friendzone-demo.apk
```

Distribute `friendzone-demo.apk` via GitHub Releases, Google Drive, or email.

---

## 5. Demo-day checklist

1. Confirm Render service is deployed and env vars are set.
2. Open Swagger or the app **1–2 minutes before** the demo to wake Render.
3. Install the APK on test phones (enable “Install unknown apps” if sideloading).
4. Register 3 accounts (or pre-register via Swagger).
5. Keep the event detail screen open during the demo so Socket.IO heartbeats prevent idle sleep.
6. If the map stops updating, wait for reconnect or background and reopen the event (app re-joins rooms automatically).

---

## Helper script

[`api/scripts/deploy-render-neon.sh`](../scripts/deploy-render-neon.sh)

```bash
cd api
chmod +x scripts/deploy-render-neon.sh

./scripts/deploy-render-neon.sh check
./scripts/deploy-render-neon.sh generate-env          # print env block for Render
./scripts/deploy-render-neon.sh validate-url          # needs DATABASE_URL or api/.env
DATABASE_URL='...' ./scripts/deploy-render-neon.sh migrate
API_URL='https://...' ./scripts/deploy-render-neon.sh smoke
```

---

## Environment reference

| Variable | Required | Notes |
|----------|----------|-------|
| `DATABASE_URL` | Yes | Neon URL with `?sslmode=require` |
| `JWT_SECRET` | Yes | Long random string |
| `JWT_REFRESH_SECRET` | Yes | Different long random string |
| `JWT_ACCESS_EXPIRES_IN` | No | Default `15m` |
| `JWT_REFRESH_EXPIRES_IN` | No | Default `7d` |
| `ARRIVAL_THRESHOLD` | No | Meters; default `500` |
| `FIREBASE_*` | No | Push notifications only |
| `PORT` | No | Render sets this automatically |

---

## Troubleshooting

### Deploy fails on Render

- Check **Logs** in the Render dashboard.
- Confirm `DATABASE_URL` is correct and Neon project is active.
- Run `./scripts/deploy-render-neon.sh migrate` locally to surface migration errors early.

### `401` / WebSocket disconnects

- Access token may have expired — log in again.
- After Render cold start, the app reconnects and re-joins event rooms; wait a few seconds.

### Android cannot reach API

- `API_BASE_URL` must be **HTTPS** (cleartext is only allowed for localhost in the app).
- Trailing slash is fine; the app trims it.

### Slow first request

- Render free tier cold start. Wake the service before the demo.

### Do not use Render free Postgres

- Render’s free PostgreSQL databases **expire after 30 days**. Use Neon for a durable free database.

---

## Files

| File | Purpose |
|------|---------|
| [`render.yaml`](../../render.yaml) | Render Blueprint |
| [`api/scripts/deploy-render-neon.sh`](../scripts/deploy-render-neon.sh) | Local deploy helper |
| [`api/package.json`](../package.json) | `start:render` = migrate + start |
| [`api/.env.example`](../.env.example) | Local env template |
