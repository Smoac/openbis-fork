----------------------------------------------------------------------
--
--  Migration script from version 4 to 5 of the database
--
----------------------------------------------------------------------

CREATE DOMAIN CONTENT_TYPE as VARCHAR(120);
ALTER TABLE FILES ALTER COLUMN CONTENT_TYPE TYPE CONTENT_TYPE;
DROP DOMAIN VARCHAR_50;