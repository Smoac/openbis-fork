----------------------------------------------------------------------
--
--  Migration script from version 8 to 9 of the database
--
----------------------------------------------------------------------

-- New column IS_COMPLETE

ALTER TABLE FILES ADD COMPLETE_SIZE SIZE NOT NULL;

-- Add index for querying candidates of files that are incomplete and can be resumed 

CREATE INDEX FILE_USER_NAME_SIZE_I ON FILES (USER_ID, NAME, COMPLETE_SIZE);
