# FriendZone SDK Distribution

Production backend:

```bash
FRIENDZONE_API_BASE_URL=https://friendzone-api-zrvr.onrender.com/
```

The Android app reads the backend URL from `FRIENDZONE_API_BASE_URL` first, then from the Gradle property `friendzoneApiBaseUrl`.

## Build Production Distribution Artifacts

From the repository root:

```bash
cd android
./scripts/build-sdk-distribution.sh
```

The script sources `android/env/production.env`, builds release APK and AAB artifacts, and copies them into:

```text
android/dist/friendzone-release-unsigned.apk
android/dist/friendzone-release.aab
```

## Override The Backend URL

Use a one-off environment variable:

```bash
cd android
FRIENDZONE_API_BASE_URL=https://example.com/ ./gradlew assembleRelease
```

Or use a Gradle property:

```bash
cd android
./gradlew assembleRelease -PfriendzoneApiBaseUrl=https://example.com/
```

Debug builds default to `http://10.0.2.2:3000/` when no URL is configured. Release builds default to `https://friendzone-api-zrvr.onrender.com/`.

## Notes

The generated release APK is unsigned unless a signing configuration is added to Gradle. Use the AAB for Play Console distribution, or add release signing before sharing an installable APK.

## Build A Signed Installable APK

Run:

```bash
cd android
./scripts/build-signed-release.sh
```

On the first run, the script asks for keystore passwords and creates:

```text
android/friendzone-release.keystore
android/local.properties
```

Both files are ignored by git. Keep `friendzone-release.keystore` and the passwords backed up; Android app updates must be signed with the same key.

The signed APK is copied to:

```text
android/dist/friendzone-release-signed.apk
```

Install it with:

```bash
adb install android/dist/friendzone-release-signed.apk
```

You can override defaults with environment variables before running the script:

```bash
RELEASE_STORE_FILE=friendzone-release.keystore \
RELEASE_KEY_ALIAS=friendzone \
FRIENDZONE_API_BASE_URL=https://friendzone-api-zrvr.onrender.com/ \
./scripts/build-signed-release.sh
```
