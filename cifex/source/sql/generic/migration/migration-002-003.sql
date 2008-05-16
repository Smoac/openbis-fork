----------------------------------------------------------------------
--
--  Migration script from version 2 to 3 of the database
--
----------------------------------------------------------------------

CREATE DOMAIN COMMENT AS VARCHAR(1000);

-- New column COMMENT

ALTER TABLE FILES ADD COMMENT COMMENT;
