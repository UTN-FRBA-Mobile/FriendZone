#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="${1:-$ANDROID_DIR/env/production.env}"
DIST_DIR="$ANDROID_DIR/dist"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

: "${FRIENDZONE_API_BASE_URL:=https://friendzone-api-zrvr.onrender.com/}"

if [[ "$FRIENDZONE_API_BASE_URL" != */ ]]; then
  FRIENDZONE_API_BASE_URL="$FRIENDZONE_API_BASE_URL/"
fi

export FRIENDZONE_API_BASE_URL

cd "$ANDROID_DIR"

./gradlew clean assembleRelease bundleRelease

mkdir -p "$DIST_DIR"

APK_SOURCE="$ANDROID_DIR/app/build/outputs/apk/release/app-release-unsigned.apk"
AAB_SOURCE="$ANDROID_DIR/app/build/outputs/bundle/release/app-release.aab"

if [[ -f "$APK_SOURCE" ]]; then
  cp "$APK_SOURCE" "$DIST_DIR/friendzone-release-unsigned.apk"
fi

if [[ -f "$AAB_SOURCE" ]]; then
  cp "$AAB_SOURCE" "$DIST_DIR/friendzone-release.aab"
fi

cat <<EOF
FriendZone distribution artifacts built with:
  FRIENDZONE_API_BASE_URL=$FRIENDZONE_API_BASE_URL

Artifacts:
  $DIST_DIR/friendzone-release-unsigned.apk
  $DIST_DIR/friendzone-release.aab
EOF
