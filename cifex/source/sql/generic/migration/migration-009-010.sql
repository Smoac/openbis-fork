-- JAVA ch.systemsx.cisd.cifex.server.business.dataaccess.db.migration.MigrationStepFrom009To010

----------------------------------------------------------------------
--
--  Migration script from version 9 to 10 of the database
--
----------------------------------------------------------------------

-- Add NOT NULL constraint for FILES.USER_ID

-- Note: This change has been revoked. It is commented out to avoid assigning orphaned files to USER_ID 1 
-- UPDATE FILES SET USER_ID = 1 WHERE USER_ID IS NULL;
-- ALTER TABLE FILES ALTER COLUMN USER_ID SET NOT NULL;

-- Clean up old domain USER_NAME that has been forgotten in the migration from 1 to 2

DROP DOMAIN IF EXISTS USER_NAME;

-- New table QUOTA_GROUPS

CREATE TABLE QUOTA_GROUPS (
  ID TECH_ID NOT NULL,
	FILE_COUNT INTEGER NOT NULL DEFAULT 0,
  FILE_SIZE SIZE NOT NULL DEFAULT 0,
  QUOTA_FILE_COUNT INTEGER,
  QUOTA_FILE_SIZE SIZE,
  FILE_RETENTION DURATION,
  USER_RETENTION DURATION
);

-- Creating primary key constraint for table QUOTA_GROUPS

ALTER TABLE QUOTA_GROUPS ADD CONSTRAINT QUOTA_GROUP_PK PRIMARY KEY(ID);

-- New sequence QUOTA_GROUP_ID_SEQ for QUOTA_GROUPS.ID

CREATE SEQUENCE QUOTA_GROUP_ID_SEQ;

-- New column USERS.QUOTA_GROUP_ID

ALTER TABLE USERS ADD QUOTA_GROUP_ID TECH_ID;

-- Foreign key constraint for USERS.QUOTA_GROUP_ID

ALTER TABLE USERS ADD CONSTRAINT USER_QUOTA_GROUP_FK FOREIGN KEY (QUOTA_GROUP_ID) REFERENCES QUOTA_GROUPS(ID);

-- Drop column USERS.MAX_UPLOAD_SIZE

ALTER TABLE USERS DROP MAX_UPLOAD_SIZE;