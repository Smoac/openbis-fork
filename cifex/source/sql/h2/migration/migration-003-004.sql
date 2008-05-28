----------------------------------------------------------------------
--
--  Migration script from version 3 to 4 of the database
--
----------------------------------------------------------------------

-- Rename USERS.ENCRYPTED_PASSWORD to USERS.PASSWORD_HASH
-- No need to change the type because H2 doesn't save the name of domains anyway. 

ALTER TABLE USERS ALTER COLUMN ENCRYPTED_PASSWORD RENAME TO PASSWORD_HASH;
