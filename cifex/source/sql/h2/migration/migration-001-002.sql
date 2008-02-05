----------------------------------------------------------------------
--
--  Migration script from version 1 to 2 of the database
--
----------------------------------------------------------------------


CREATE DOMAIN PATH_NAME AS VARCHAR(300);
CREATE DOMAIN USER_ID AS VARCHAR(50);
CREATE DOMAIN OBJECT_NAME AS VARCHAR(50);

-- Name changes

ALTER TABLE USERS ALTER COLUMN USER_NAME RENAME TO FULL_NAME;

-- Type changes

-- Make the path name as long as the maximum length of the email address plus the maximum length of the name of a file
ALTER TABLE FILES ALTER COLUMN PATH PATH_NAME NOT NULL;

-- Let FULL_NAME, EMAIL and USER_ID all have the same length 
ALTER TABLE USERS ALTER COLUMN FULL_NAME OBJECT_NAME;
ALTER TABLE USERS ALTER COLUMN EMAIL OBJECT_NAME;

-- New column USER_ID

ALTER TABLE USERS ADD USER_ID USER_ID NOT NULL;

-- Drop unique constraint on USERS.EMAIL and add unique constraint on USERS.USER_ID

ALTER TABLE USERS DROP CONSTRAINT USER_BK_UK;
ALTER TABLE USERS ADD CONSTRAINT USER_BK_UK UNIQUE(USER_ID);
