-- Creating Functions

-------------------------------------------------------------------------------------------
--  Purpose:  Re-calculate the accounting information for all quota groups
-------------------------------------------------------------------------------------------
CREATE ALIAS CALC_ACCOUNTING_FOR_ALL_QUOTA_GROUPS FOR 
	"ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.AccountingStoredProcedures.calcAccountingForAllQuotaGroups";

-------------------------------------------------------------------------------------------
--  Purpose:  Re-calculate the accounting information for given quota groups
-------------------------------------------------------------------------------------------
CREATE ALIAS CALC_ACCOUNTING_FOR_QUOTA_GROUPS FOR 
	"ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.AccountingStoredProcedures.calcAccountingForQuotaGroups";

-- Creating Triggers

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
--  Purpose:  Update accounting information for the quota group when updating a file
-------------------------------------------------------------------------------------------
CREATE TRIGGER UPDATE_ACCOUNTING_ON_UPDATE_FILE AFTER UPDATE ON FILES
    FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateAccountingOnUpdateFileTrigger";

-------------------------------------------------------------------------------------------
--  Purpose:  Create trigger for creating and setting a new quota_group if none is provided
-------------------------------------------------------------------------------------------
CREATE TRIGGER UPDATE_QUOTA_GROUP_ID_ON_INSERT_UPDATE_USER BEFORE INSERT, UPDATE ON USERS
	FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateQuotaGroupIdOnInsertUpdateUserTrigger";
    
-------------------------------------------------------------------------------------------
--  Purpose:  Create trigger for deleting a quota group when its last user is deleted
-------------------------------------------------------------------------------------------
CREATE TRIGGER DELETE_QUOTA_GROUP_ON_DELETE_USER AFTER DELETE ON USERS
	FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.DeleteQuotaGroupOnDeleteUserTrigger";

-------------------------------------------------------------------------------------------
--  Purpose:  Update the accounting information when a user changes the quota group
-------------------------------------------------------------------------------------------
CREATE TRIGGER UPDATE_ACCOUNTING_ON_UPDATE_USER AFTER UPDATE ON USERS
    FOR EACH ROW CALL "ch.systemsx.cisd.cifex.server.business.dataaccess.db.h2.UpdateAccountingOnUpdateUserTrigger";
