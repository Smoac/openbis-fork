-- Call trigger for insert and update

DROP TRIGGER UPDATE_QUOTA_GROUP_ID_ON_INSERT;

CREATE TRIGGER UPDATE_QUOTA_GROUP_ID_ON_INSERT_UPDATE_USER BEFORE INSERT, UPDATE ON USERS
	FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateQuotaGroupIdOnInsertUpdateUserTrigger";
    