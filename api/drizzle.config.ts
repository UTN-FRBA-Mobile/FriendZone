import { defineConfig } from 'drizzle-kit';

export default defineConfig({
  schema: './drizzle/schema/index.ts',
  out: './drizzle/migrations',
  dialect: 'postgresql',
  dbCredentials: {
    url: process.env.DATABASE_URL ?? 'postgresql://friendzone:friendzone@localhost:5432/friendzone',
  },
});
