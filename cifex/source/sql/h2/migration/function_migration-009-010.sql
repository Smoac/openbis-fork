-- Creating Functions

------------------------------------------------------------------------------------
--  Purpose: Create trigger for updating accounting information for the quota group when inserting a file
------------------------------------------------------------------------------------
CREATE TRIGGER UPDATE_ACCOUNTING_ON_INSERT_FILE AFTER INSERT ON FILES
    FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateAccountingOnInsertFileTrigger";

------------------------------------------------------------------------------------
--  Purpose: Create trigger for updating accounting information for the quota group when deleting a file
------------------------------------------------------------------------------------
CREATE TRIGGER UPDATE_ACCOUNTING_ON_DELETE_FILE AFTER DELETE ON FILES
    FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateAccountingOnDeleteFileTrigger";

-------------------------------------------------------------------------------------------
--  Purpose:  Create trigger for creating and setting a new quota_group if none is provided
-------------------------------------------------------------------------------------------
CREATE TRIGGER UPDATE_QUOTA_GROUP_ID_ON_INSERT BEFORE INSERT ON USERS
    FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateQuotaGroupOnInsertUserTrigger";
    
-------------------------------------------------------------------------------------------
--  Purpose:  Create trigger for deleting a quota group when its last user is deleted
-------------------------------------------------------------------------------------------
CREATE TRIGGER DELETE_QUOTA_GROUP_ON_DELETE AFTER DELETE ON USERS
    FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.DeleteQuotaGroupOnDeleteUserTrigger";
