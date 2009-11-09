----------------------------------------------------------------------
--
--  Migration script from version 8 to 9 of the database
--
----------------------------------------------------------------------

-- New column IS_COMPLETE

ALTER TABLE FILES ADD IS_COMPLETE BOOLEAN_CHAR NOT NULL DEFAULT 'TRUE';
