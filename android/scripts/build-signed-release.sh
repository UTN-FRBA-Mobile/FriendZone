#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="${1:-$ANDROID_DIR/env/production.env}"
LOCAL_PROPERTIES="$ANDROID_DIR/local.properties"
DIST_DIR="$ANDROID_DIR/dist"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

read_property() {
  local key="$1"
  if [[ -f "$LOCAL_PROPERTIES" ]]; then
    awk -F= -v key="$key" '$1 == key { print substr($0, index($0, "=") + 1); exit }' "$LOCAL_PROPERTIES"
  fi
}

prompt_secret() {
  local label="$1"
  local value
  read -r -s -p "$label: " value
  printf '\n' >&2
  printf '%s' "$value"
}

upsert_property() {
  local key="$1"
  local value="$2"
  local temp_file
  temp_file="$(mktemp)"

  if [[ -f "$LOCAL_PROPERTIES" ]]; then
    awk -F= -v key="$key" '$1 != key { print }' "$LOCAL_PROPERTIES" > "$temp_file"
  fi

  printf '%s=%s\n' "$key" "$value" >> "$temp_file"
  mv "$temp_file" "$LOCAL_PROPERTIES"
}

: "${FRIENDZONE_API_BASE_URL:=https://friendzone-api-zrvr.onrender.com/}"
: "${RELEASE_STORE_FILE:=$(read_property RELEASE_STORE_FILE)}"
: "${RELEASE_KEY_ALIAS:=$(read_property RELEASE_KEY_ALIAS)}"
: "${RELEASE_STORE_PASSWORD:=$(read_property RELEASE_STORE_PASSWORD)}"
: "${RELEASE_KEY_PASSWORD:=$(read_property RELEASE_KEY_PASSWORD)}"

RELEASE_STORE_FILE="${RELEASE_STORE_FILE:-friendzone-release.keystore}"
RELEASE_KEY_ALIAS="${RELEASE_KEY_ALIAS:-friendzone}"

if [[ "$FRIENDZONE_API_BASE_URL" != */ ]]; then
  FRIENDZONE_API_BASE_URL="$FRIENDZONE_API_BASE_URL/"
fi

if [[ -z "${RELEASE_STORE_PASSWORD:-}" ]]; then
  RELEASE_STORE_PASSWORD="$(prompt_secret "Release keystore password")"
fi

if [[ -z "${RELEASE_KEY_PASSWORD:-}" ]]; then
  RELEASE_KEY_PASSWORD="$(prompt_secret "Release key password")"
fi

STORE_FILE_ABS="$RELEASE_STORE_FILE"
if [[ "$STORE_FILE_ABS" != /* ]]; then
  STORE_FILE_ABS="$ANDROID_DIR/$STORE_FILE_ABS"
fi

if [[ ! -f "$STORE_FILE_ABS" ]]; then
  keytool -genkeypair \
    -v \
    -keystore "$STORE_FILE_ABS" \
    -storepass "$RELEASE_STORE_PASSWORD" \
    -alias "$RELEASE_KEY_ALIAS" \
    -keypass "$RELEASE_KEY_PASSWORD" \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=FriendZone, OU=FriendZone, O=FriendZone, L=Buenos Aires, ST=Buenos Aires, C=AR"
fi

upsert_property RELEASE_STORE_FILE "$RELEASE_STORE_FILE"
upsert_property RELEASE_STORE_PASSWORD "$RELEASE_STORE_PASSWORD"
upsert_property RELEASE_KEY_ALIAS "$RELEASE_KEY_ALIAS"
upsert_property RELEASE_KEY_PASSWORD "$RELEASE_KEY_PASSWORD"

export FRIENDZONE_API_BASE_URL
export RELEASE_STORE_FILE
export RELEASE_STORE_PASSWORD
export RELEASE_KEY_ALIAS
export RELEASE_KEY_PASSWORD

cd "$ANDROID_DIR"

./gradlew clean assembleRelease

mkdir -p "$DIST_DIR"

SIGNED_APK_SOURCE="$ANDROID_DIR/app/build/outputs/apk/release/app-release.apk"
if [[ ! -f "$SIGNED_APK_SOURCE" ]]; then
  echo "Signed APK was not created at $SIGNED_APK_SOURCE" >&2
  exit 1
fi

cp "$SIGNED_APK_SOURCE" "$DIST_DIR/friendzone-release-signed.apk"

cat <<EOF
FriendZone signed release APK built with:
  FRIENDZONE_API_BASE_URL=$FRIENDZONE_API_BASE_URL
  RELEASE_STORE_FILE=$RELEASE_STORE_FILE
  RELEASE_KEY_ALIAS=$RELEASE_KEY_ALIAS

Artifact:
  $DIST_DIR/friendzone-release-signed.apk
EOF
