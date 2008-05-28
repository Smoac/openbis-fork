----------------------------------------------------------------------
--
--  Migration script from version 3 to 4 of the database
--
----------------------------------------------------------------------

-- Create DOMAIN HASH 
CREATE DOMAIN HASH AS VARCHAR(32);

-- Rename USERS.ENCRYPTED_PASSWORD to USERS.PASSWORD_HASH
-- Rename DOMAIN MD5_SUM to HASH 

ALTER TABLE USERS RENAME COLUMN ENCRYPTED_PASSWORD TO PASSWORD_HASH;
ALTER TABLE USERS ALTER COLUMN PASSWORD_HASH TYPE HASH;

-- Drop DOMAIN MD5_SUM 
DROP DOMAIN MD5_SUM;
