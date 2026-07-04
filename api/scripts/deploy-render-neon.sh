#!/usr/bin/env bash
set -euo pipefail

# FriendZone — Render + Neon deployment helper
# Usage: ./scripts/deploy-render-neon.sh <command>
# Docs:  api/docs/DEPLOY_RENDER_NEON.md

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${API_DIR}/.." && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { printf '%b\n' "${GREEN}==>${NC} $*"; }
warn()  { printf '%b\n' "${YELLOW}warn:${NC} $*"; }
error() { printf '%b\n' "${RED}error:${NC} $*" >&2; }

usage() {
  cat <<'EOF'
FriendZone Render + Neon deployment helper

Commands:
  check           Verify local tools (node, npm, optional render CLI)
  generate-env    Print a Render environment block with fresh JWT secrets
  validate-url    Validate DATABASE_URL format (and SSL for Neon)
  migrate         Run DB migrations locally against DATABASE_URL
  build           Production build (same as Render build step)
  smoke           Build, migrate, and print smoke-test curl commands
  help            Show this message

Examples:
  ./scripts/deploy-render-neon.sh check
  ./scripts/deploy-render-neon.sh generate-env > render.env
  DATABASE_URL='postgresql://...' ./scripts/deploy-render-neon.sh migrate
  API_URL='https://friendzone-api.onrender.com' ./scripts/deploy-render-neon.sh smoke

Set DATABASE_URL in the shell or in api/.env before migrate/smoke.
EOF
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    error "Missing required command: $1"
    return 1
  fi
}

load_database_url() {
  if [[ -n "${DATABASE_URL:-}" ]]; then
    return 0
  fi
  if [[ -f "${API_DIR}/.env" ]]; then
    # shellcheck disable=SC1091
    set -a
    source "${API_DIR}/.env"
    set +a
  fi
  if [[ -z "${DATABASE_URL:-}" ]]; then
    error "DATABASE_URL is not set. Export it or add it to api/.env"
    exit 1
  fi
}

ensure_neon_ssl() {
  local url="$1"
  if [[ "$url" != *"sslmode="* ]]; then
    if [[ "$url" == *"?"* ]]; then
      printf '%s&sslmode=require' "$url"
    else
      printf '%s?sslmode=require' "$url"
    fi
  else
    printf '%s' "$url"
  fi
}

cmd_check() {
  info "Checking prerequisites..."
  require_command node
  require_command npm
  node --version
  npm --version

  if command -v render >/dev/null 2>&1; then
    info "Render CLI found: $(render --version 2>/dev/null || render version 2>/dev/null || echo 'installed')"
  else
    warn "Render CLI not installed (optional). Install: https://render.com/docs/cli"
  fi

  if [[ -f "${REPO_ROOT}/render.yaml" ]]; then
    info "Found ${REPO_ROOT}/render.yaml"
  else
    warn "Missing ${REPO_ROOT}/render.yaml"
  fi

  info "Done."
}

random_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 48 | tr -d '\n'
  else
    node -e "console.log(require('crypto').randomBytes(48).toString('base64'))"
  fi
}

cmd_generate_env() {
  local access_secret refresh_secret
  access_secret="$(random_secret)"
  refresh_secret="$(random_secret)"

  cat <<EOF
# Paste these into Render → friendzone-api → Environment
# Replace DATABASE_URL with your Neon connection string (see docs).

NODE_ENV=production
DATABASE_URL=postgresql://USER:PASSWORD@HOST/DBNAME?sslmode=require
JWT_SECRET=${access_secret}
JWT_REFRESH_SECRET=${refresh_secret}
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d
ARRIVAL_THRESHOLD=500
FIREBASE_PROJECT_ID=
FIREBASE_CLIENT_EMAIL=
FIREBASE_PRIVATE_KEY=
PORT=3000
EOF
}

cmd_validate_url() {
  load_database_url
  local normalized
  normalized="$(ensure_neon_ssl "$DATABASE_URL")"

  if [[ "$normalized" != postgresql://* && "$normalized" != postgres://* ]]; then
    error "DATABASE_URL must start with postgresql:// or postgres://"
    exit 1
  fi

  if [[ "$normalized" != *neon.tech* && "$normalized" != *sslmode=require* ]]; then
    warn "URL does not look like Neon and has no sslmode=require — remote Postgres usually needs SSL"
  fi

  info "DATABASE_URL format looks valid."
  if [[ "$normalized" != "$DATABASE_URL" ]]; then
    warn "Suggested Neon URL with SSL:"
    printf '%s\n' "$normalized"
  fi
}

cmd_migrate() {
  load_database_url
  export DATABASE_URL="$(ensure_neon_ssl "$DATABASE_URL")"

  info "Installing dependencies..."
  (cd "$API_DIR" && npm ci)

  info "Building..."
  (cd "$API_DIR" && npm run build)

  info "Running migrations against remote database..."
  (cd "$API_DIR" && node dist/src/drizzle/migrate.js)

  info "Migrations complete."
}

cmd_build() {
  info "Running production build..."
  (cd "$API_DIR" && npm ci && npm run build)
  info "Build complete."
}

cmd_smoke() {
  local api_url="${API_URL:-}"
  if [[ -z "$api_url" ]]; then
    error "Set API_URL to your Render service URL, e.g. https://friendzone-api.onrender.com"
    exit 1
  fi
  api_url="${api_url%/}"

  cmd_build
  if [[ -n "${DATABASE_URL:-}" || -f "${API_DIR}/.env" ]]; then
    cmd_migrate || warn "Migration step failed — deploy may still run migrations on start"
  else
    warn "Skipping migrate — set DATABASE_URL to test migrations locally"
  fi

  cat <<EOF

Smoke tests (run after Render deploy is live):

  # Wake the service (free tier cold start may take 30–60s)
  curl -sS -o /dev/null -w "HTTP %{http_code}\\n" "${api_url}/api/docs"

  # Register a test user
  curl -sS -X POST "${api_url}/auth/register" \\
    -H 'Content-Type: application/json' \\
    -d '{"email":"demo@example.com","username":"demo","password":"password123","displayName":"Demo User"}'

Swagger UI: ${api_url}/api/docs
EOF
}

main() {
  local command="${1:-help}"
  case "$command" in
    check) cmd_check ;;
    generate-env) cmd_generate_env ;;
    validate-url) cmd_validate_url ;;
    migrate) cmd_migrate ;;
    build) cmd_build ;;
    smoke) cmd_smoke ;;
    help|-h|--help) usage ;;
    *)
      error "Unknown command: $command"
      usage
      exit 1
      ;;
  esac
}

main "$@"
