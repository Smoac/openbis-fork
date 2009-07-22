----------------------------------------------------------------------
--
--  Migration script from version 007 to 008 of the database
--
----------------------------------------------------------------------

CREATE DOMAIN CHECKSUM32 AS INTEGER;

ALTER TABLE FILES ADD COLUMN CRC32_CHECKSUM CHECKSUM32;
