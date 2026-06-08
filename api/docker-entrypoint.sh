#!/bin/sh
set -e

echo "Running database migrations..."
node dist/src/drizzle/migrate.js

echo "Starting application..."
exec node dist/src/main.js
