----------------------------------------------------------------------
--
--  Migration script from version 6 to 7 of the database
--
----------------------------------------------------------------------

-- New column IS_ACTIVE

ALTER TABLE USERS ADD COLUMN IS_ACTIVE BOOLEAN_CHAR DEFAULT 'TRUE';

-- Set all currently existing users to IS_ACTIVE='TRUE'

UPDATE USERS SET IS_ACTIVE='TRUE';

-- Make IS_ACTIVE constrained to NOT NULL

ALTER TABLE USERS ALTER COLUMN IS_ACTIVE SET NOT NULL;