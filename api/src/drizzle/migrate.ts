import { drizzle } from 'drizzle-orm/postgres-js';
import { migrate } from 'drizzle-orm/postgres-js/migrator';
import postgres from 'postgres';

async function runMigrations() {
  const connectionString =
    process.env.DATABASE_URL ??
    'postgresql://friendzone:friendzone@localhost:5432/friendzone';

  const client = postgres(connectionString, { max: 1 });
  const db = drizzle(client);

  await migrate(db, { migrationsFolder: './drizzle/migrations' });
  await client.end();

  console.log('Migrations applied successfully');
}

runMigrations().catch((err) => {
  console.error('Migration failed:', err);
  process.exit(1);
});
