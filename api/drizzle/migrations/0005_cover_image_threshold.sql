ALTER TABLE "events" ADD COLUMN IF NOT EXISTS "cover_image_url" text;
--> statement-breakpoint
ALTER TABLE "events" ALTER COLUMN "arrival_threshold_m" SET DEFAULT 150;
