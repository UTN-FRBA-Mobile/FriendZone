#!/bin/sh
set -e

echo "Running database migrations..."
node dist/drizzle/migrate.js

echo "Starting application..."
exec node dist/main.js
