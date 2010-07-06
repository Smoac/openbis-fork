-- Create Functions 

-------------------------------------------------------------------------------------------
--  Purpose:  Re-calculate the accounting information for all quota groups
-------------------------------------------------------------------------------------------
CREATE FUNCTION CALC_ACCOUNTING_FOR_ALL_QUOTA_GROUPS() RETURNS void AS $$
	LOCK TABLE QUOTA_GROUPS IN ACCESS EXCLUSIVE MODE;
	UPDATE QUOTA_GROUPS Q SET FILE_COUNT = (
		SELECT COUNT(*) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID
			WHERE U.QUOTA_GROUP_ID = Q.ID
	  );
	UPDATE QUOTA_GROUPS Q SET FILE_SIZE = (
		SELECT COALESCE(SUM(F.COMPLETE_SIZE), 0) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID
			WHERE U.QUOTA_GROUP_ID = Q.ID
	  );
$$ LANGUAGE 'sql';

-------------------------------------------------------------------------------------------
--  Purpose:  Re-calculate the accounting information for given quota groups
--  Note: this function won't work with PostgreSQL before 8.4 and it won't be called
--  by CIFEX in normal operation. It is supposed to be used by the database admin 
--  in maintenance situations under PostgreSQL 8.4.
-------------------------------------------------------------------------------------------
CREATE FUNCTION CALC_ACCOUNTING_FOR_QUOTA_GROUPS(VARIADIC BIGINT[]) RETURNS void AS $$
  LOCK TABLE QUOTA_GROUPS IN ACCESS EXCLUSIVE MODE;
  UPDATE QUOTA_GROUPS Q SET FILE_COUNT = (
    SELECT COUNT(*) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID 
      WHERE U.QUOTA_GROUP_ID = Q.ID 
    ) 
    WHERE Q.ID = ANY($1);
  UPDATE QUOTA_GROUPS Q SET FILE_SIZE = (
    SELECT COALESCE(SUM(F.COMPLETE_SIZE), 0) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID
      WHERE U.QUOTA_GROUP_ID = Q.ID
    )
    WHERE Q.ID = ANY($1);
$$ LANGUAGE 'sql';

-------------------------------------------------------------------------------------------
--  Purpose:  Re-calculate the accounting information for given quota groups
-------------------------------------------------------------------------------------------
CREATE FUNCTION CALC_ACCOUNTING_FOR_QUOTA_GROUPS(BIGINT, BIGINT) RETURNS void AS $$
        LOCK TABLE QUOTA_GROUPS IN ACCESS EXCLUSIVE MODE;
        UPDATE QUOTA_GROUPS Q SET FILE_COUNT = (
                SELECT COUNT(*) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID 
                        WHERE U.QUOTA_GROUP_ID = Q.ID 
          ) 
          WHERE Q.ID = $1 OR Q.ID = $2;
        UPDATE QUOTA_GROUPS Q SET FILE_SIZE = (
                SELECT COALESCE(SUM(F.COMPLETE_SIZE), 0) FROM FILES F JOIN USERS U ON F.USER_ID = U.ID
                        WHERE U.QUOTA_GROUP_ID = Q.ID
          )
          WHERE Q.ID = $1 OR Q.ID = $2;
$$ LANGUAGE 'sql';
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

CREATE TRIGGER UPDATE_ACCOUNTING_ON_INSERT_FILE AFTER INSERT ON FILES
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

CREATE TRIGGER UPDATE_ACCOUNTING_ON_DELETE_FILE AFTER DELETE ON FILES
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_ACCOUNTING_ON_DELETE_FILE();

-------------------------------------------------------------------------------------------
--  Purpose:  Update accounting information for the quota group when updating a file
-------------------------------------------------------------------------------------------
CREATE FUNCTION UPDATE_ACCOUNTING_ON_UPDATE_FILE() RETURNS TRIGGER AS $$
DECLARE
	USER_CHANGED         BOOLEAN := (OLD.USER_ID <> NEW.USER_ID);
	SIZE_CHANGED         BOOLEAN := (OLD.COMPLETE_SIZE <> NEW.COMPLETE_SIZE);
	QUOTA_GROUP_CHANGED  BOOLEAN := FALSE;
	OLD_QUOTA_GROUP_ID   TECH_ID;
	NEW_QUOTA_GROUP_ID   TECH_ID;
BEGIN
  IF USER_CHANGED OR SIZE_CHANGED THEN
		SELECT Q.ID FROM QUOTA_GROUPS Q JOIN USERS U ON Q.ID = U.QUOTA_GROUP_ID WHERE U.ID = NEW.USER_ID
			INTO NEW_QUOTA_GROUP_ID;
	  IF USER_CHANGED THEN
			SELECT Q.ID FROM QUOTA_GROUPS Q JOIN USERS U ON Q.ID = U.QUOTA_GROUP_ID WHERE U.ID = OLD.USER_ID
				INTO OLD_QUOTA_GROUP_ID;
				QUOTA_GROUP_CHANGED := (OLD_QUOTA_GROUP_ID <> NEW_QUOTA_GROUP_ID);
	  END IF;
  END IF;
  IF QUOTA_GROUP_CHANGED THEN
		UPDATE QUOTA_GROUPS
			SET
				FILE_COUNT = FILE_COUNT - 1,
				FILE_SIZE = FILE_SIZE - OLD.COMPLETE_SIZE
			WHERE ID = OLD_QUOTA_GROUP_ID;
		UPDATE QUOTA_GROUPS
			SET
				FILE_COUNT = FILE_COUNT + 1,
				FILE_SIZE = FILE_SIZE + NEW.COMPLETE_SIZE
			WHERE ID = NEW_QUOTA_GROUP_ID;
  ELSIF SIZE_CHANGED THEN
		UPDATE QUOTA_GROUPS
			SET
				FILE_SIZE = FILE_SIZE - OLD.COMPLETE_SIZE + NEW.COMPLETE_SIZE
			WHERE ID = NEW_QUOTA_GROUP_ID;
  END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_ACCOUNTING_ON_UPDATE_FILE AFTER UPDATE ON FILES
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_ACCOUNTING_ON_UPDATE_FILE();

-------------------------------------------------------------------------------------------
--  Purpose:  Create and set a new quota_group if none is provided when updating or inserting a user
-------------------------------------------------------------------------------------------
CREATE FUNCTION UPDATE_QUOTA_GROUP_ID_ON_INSERT_UPDATE_USER() RETURNS TRIGGER AS $$
DECLARE
	REGISTRATOR_IS_ADMIN   BOOLEAN;
	TARGET_QUOTA_GROUP_ID  TECH_ID;
BEGIN
	IF NEW.QUOTA_GROUP_ID IS NULL THEN
		INSERT INTO QUOTA_GROUPS (ID) VALUES (NEXTVAL('QUOTA_GROUP_ID_SEQ')) 
			RETURNING ID INTO NEW.QUOTA_GROUP_ID;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_QUOTA_GROUP_ID_ON_INSERT_UPDATE_USER BEFORE INSERT OR UPDATE ON USERS
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_QUOTA_GROUP_ID_ON_INSERT_UPDATE_USER();

-------------------------------------------------------------------------------------------
--  Purpose:  Delete a quota group when its last user is deleted
-------------------------------------------------------------------------------------------
CREATE FUNCTION DELETE_QUOTA_GROUP_ON_DELETE_USER() RETURNS TRIGGER AS $$
DECLARE
	REMAINING_USERS_IN_GROUP  INTEGER;
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

CREATE TRIGGER DELETE_QUOTA_GROUP_ON_DELETE_USER AFTER DELETE ON USERS
    FOR EACH ROW EXECUTE PROCEDURE DELETE_QUOTA_GROUP_ON_DELETE_USER();

-------------------------------------------------------------------------------------------
--  Purpose:  Update the accounting information when a user changes the quota group
-------------------------------------------------------------------------------------------
CREATE FUNCTION UPDATE_ACCOUNTING_ON_UPDATE_USER() RETURNS TRIGGER AS $$
BEGIN
	-- Simulate NOT NULL constraint on USERS.QUOTA_GROUP_ID
	-- Implementation note: in PostgreSQL the NOT NULL column constraint could be set directly,
	--   however H2 checks constraints _before_ calling any BEFORE triggers and thus would throw a
	--   constraint violation when inserting a user without QUOTA_GROUP_ID set explicitly even when
	--   the UPDATE_QUOTA_GROUP_ID_ON_INSERT_USER trigger would rectify the situation. 
	IF NEW.QUOTA_GROUP_ID IS NULL THEN
		RAISE EXCEPTION 'Quota group of user % must not be set to NULL', NEW.USER_CODE;
	END IF;
	IF OLD.QUOTA_GROUP_ID <> NEW.QUOTA_GROUP_ID THEN
		PERFORM CALC_ACCOUNTING_FOR_QUOTA_GROUPS(OLD.QUOTA_GROUP_ID, NEW.QUOTA_GROUP_ID);
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_ACCOUNTING_ON_UPDATE_USER AFTER UPDATE ON USERS
    FOR EACH ROW EXECUTE PROCEDURE UPDATE_ACCOUNTING_ON_UPDATE_USER();
