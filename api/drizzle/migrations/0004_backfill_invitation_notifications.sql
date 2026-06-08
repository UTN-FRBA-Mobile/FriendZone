INSERT INTO "user_notifications" ("user_id", "type", "title", "body", "data", "created_at")
SELECT
  i."invitee_id",
  'invitation.created',
  'New Event Invitation',
  'You have been invited to "' || e."title" || '"',
  jsonb_build_object(
    'eventId', i."event_id"::text,
    'invitationId', i."id"::text,
    'eventTitle', e."title",
    'eventStartsAt', to_char(e."starts_at" AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"'),
    'organizerDisplayName', COALESCE(u."display_name", 'Organizer')
  ),
  i."created_at"
FROM "invitations" i
INNER JOIN "events" e ON e."id" = i."event_id"
INNER JOIN "users" u ON u."id" = i."invited_by_id"
WHERE i."status" = 'pending'
  AND NOT EXISTS (
    SELECT 1
    FROM "user_notifications" n
    WHERE n."user_id" = i."invitee_id"
      AND n."type" = 'invitation.created'
      AND n."data"->>'invitationId' = i."id"::text
  );
