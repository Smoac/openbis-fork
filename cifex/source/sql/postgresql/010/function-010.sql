-- Creating Triggers

-------------------------------------------------------------------------------------------
--  Purpose:  Update accounting information for the quota group when inserting a file
-------------------------------------------------------------------------------------------
CREATE FUNCTION UPDATE_ACCOUNTING_ON_INSERT_FILE() RETURNS TRIGGER AS $$
BEGIN
  UPDATE QUOTA_GROUPS 
  	SET 
  		FILE_COUNT = FILE_COUNT + 1, 
  		FILE_SIZE = FILE_SIZE + NEW.COMPLETE_SIZE 
  	WHERE ID = (SELECT QUOTA_GROUP_ID FROM USERS WHERE ID = NEW.USER_ID); 
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_ACCOUNTING_ON_INSERT AFTER INSERT ON FILES
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_ACCOUNTING_ON_INSERT_FILE();


-------------------------------------------------------------------------------------------
--  Purpose:  Update accounting information for the quota group when deleting a file
-------------------------------------------------------------------------------------------
CREATE FUNCTION UPDATE_ACCOUNTING_ON_DELETE_FILE() RETURNS TRIGGER AS $$
BEGIN
  UPDATE QUOTA_GROUPS 
  	SET 
  		FILE_COUNT = FILE_COUNT - 1, 
  		FILE_SIZE = FILE_SIZE - OLD.COMPLETE_SIZE 
  	WHERE ID = (SELECT QUOTA_GROUP_ID FROM USERS WHERE ID = OLD.USER_ID); 
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_ACCOUNTING_ON_DELETE AFTER DELETE ON FILES
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_ACCOUNTING_ON_DELETE_FILE();

-------------------------------------------------------------------------------------------
--  Purpose:  Create and set a new quota_group if none is provided
-------------------------------------------------------------------------------------------
CREATE FUNCTION UPDATE_QUOTA_GROUP_ID_ON_INSERT_USER() RETURNS TRIGGER AS $$
DECLARE
	REGISTRATOR_IS_ADMIN  BOOLEAN;
	TARGET_QUOTA_GROUP_ID        BIGINT;
BEGIN
	IF NEW.QUOTA_GROUP_ID IS NULL THEN
		INSERT INTO QUOTA_GROUPS (ID) VALUES (NEXTVAL('QUOTA_GROUP_ID_SEQ')) 
			RETURNING ID INTO NEW.QUOTA_GROUP_ID;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_QUOTA_GROUP_ID_ON_INSERT BEFORE INSERT ON USERS
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_QUOTA_GROUP_ID_ON_INSERT_USER();

-------------------------------------------------------------------------------------------
--  Purpose:  Delete a quota group when its last user is deleted
-------------------------------------------------------------------------------------------
CREATE FUNCTION DELETE_QUOTA_GROUP_ON_DELETE_USER() RETURNS TRIGGER AS $$
DECLARE
	REMAINING_USERS_IN_GROUP INTEGER;
BEGIN
	-- Find out how many users still are in the quota group of the deleted user
	SELECT COUNT(*) FROM USERS WHERE QUOTA_GROUP_ID = OLD.QUOTA_GROUP_ID INTO REMAINING_USERS_IN_GROUP;
	-- If there is no user left, delete the quota group
	IF REMAINING_USERS_IN_GROUP = 0 THEN
		DELETE FROM QUOTA_GROUPS WHERE ID = OLD.QUOTA_GROUP_ID;
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER DELETE_QUOTA_GROUP_ON_DELETE AFTER DELETE ON USERS
    FOR EACH ROW EXECUTE PROCEDURE DELETE_QUOTA_GROUP_ON_DELETE_USER();
