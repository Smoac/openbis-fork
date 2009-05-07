----------------------------------------------------------------------
--
--  Migration script from version 005 to 006 of the database
--
----------------------------------------------------------------------

CREATE DOMAIN DURATION AS BIGINT;

ALTER TABLE USERS ADD COLUMN MAX_UPLOAD_SIZE SIZE;
ALTER TABLE USERS ADD COLUMN FILE_RETENTION DURATION;
