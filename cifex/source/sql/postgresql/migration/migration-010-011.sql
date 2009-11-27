-- Remove NOT NULL constraint from FILES.USER_ID that has been added in old version of 010 by mistake

ALTER TABLE FILES ALTER COLUMN USER_ID DROP NOT NULL;