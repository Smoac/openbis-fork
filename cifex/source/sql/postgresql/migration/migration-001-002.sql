----------------------------------------------------------------------
--
--  Migration script from version 1 to 2 of the database
--
----------------------------------------------------------------------

-- Make the path name as long as the maximum length of the email address plus the maximum length of the name of a file

CREATE DOMAIN PATH_NAME AS VARCHAR(300);

ALTER TABLE FILES ALTER COLUMN PATH TYPE PATH_NAME;