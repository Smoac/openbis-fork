-- Migration from 051 to 052

-- add DB_KEY column to QUERIES

ALTER TABLE queries ADD COLUMN db_key code NOT NULL DEFAULT '1';
