-- Migration from 074 to 075

----------------------------------------------
-- Purpose: Rename invalidation to deletion --
----------------------------------------------
-- renamings:
-- table INVALIDATIONS -> DELETIONS  
ALTER TABLE invalidations RENAME TO deletions;
-- sequence INVALIDATION_ID_SEQ -> DELETION_ID_SEQ
SELECT RENAME_SEQUENCE('INVALIDATION_ID_SEQ', 'DELETION_ID_SEQ');
-- columns INVA_ID -> DEL_ID
ALTER TABLE data RENAME inva_id TO del_id;
ALTER TABLE samples RENAME inva_id TO del_id;
ALTER TABLE experiments RENAME inva_id TO del_id;
-- indexes
ALTER INDEX data_inva_fk_i RENAME TO data_del_fk_i;
ALTER INDEX expe_inva_fk_i RENAME TO expe_del_fk_i;
ALTER INDEX inva_pers_fk_i RENAME TO del_pers_fk_i;
ALTER INDEX samp_inva_fk_i RENAME TO samp_del_fk_i;
--
-- recreate constraints with new names (renaming is not possible)
--
ALTER TABLE data 
  DROP CONSTRAINT data_inva_fk;
ALTER TABLE experiments
  DROP CONSTRAINT expe_inva_fk;
ALTER TABLE samples
  DROP CONSTRAINT samp_inva_fk;
ALTER TABLE deletions 
  DROP CONSTRAINT inva_pk;
  
ALTER TABLE deletions 
  ADD CONSTRAINT del_pk PRIMARY KEY(id);
ALTER TABLE data 
  ADD CONSTRAINT data_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE experiments 
  ADD CONSTRAINT expe_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
ALTER TABLE samples 
  ADD CONSTRAINT samp_del_fk FOREIGN KEY (del_id) REFERENCES deletions(id);
  
ALTER TABLE deletions 
  DROP CONSTRAINT inva_pers_fk;
ALTER TABLE deletions 
  ADD CONSTRAINT del_pers_fk FOREIGN KEY (pers_id_registerer) REFERENCES persons(id);
--
-- remove 'INVALIDATION' from EVENT_TYPE domain (we don't store or handle such events)
--
DELETE FROM events WHERE event_type = 'INVALIDATION';
ALTER DOMAIN event_type DROP CONSTRAINT event_type_check;
ALTER DOMAIN event_type ADD CONSTRAINT event_type_check CHECK (VALUE IN ('DELETION', 'MOVEMENT'));

---------------------------------------------------------------------------------------------------
--  Purpose:  Create DEFERRED triggers for checking consistency of deletion state.
----------------------------------------------------------------------------------------------------
-- 1. forbid modifications of deleted entities 
-- NOTE: we don't check for creation of deleted entities as it is not possible from the client side
-- and would reduce performance of bulk insert. 

CREATE OR REPLACE FUNCTION entity_name(entity_table_name NAME) RETURNS varchar AS $$
BEGIN
	CASE entity_table_name
		WHEN 'data' THEN RETURN 'Data Set';
		WHEN 'samples' THEN RETURN 'Sample';
		WHEN 'experiments' THEN RETURN 'Experiment';
		WHEN 'materials' THEN RETURN 'Material';
		ELSE RAISE EXCEPTION '"%" is not an entity table', entity_table_name;
	END CASE;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION forbid_deleted_entity_modification() RETURNS trigger AS $$
BEGIN
  RAISE NOTICE 'Check % (Code: %) ', entity_name(TG_TABLE_NAME), NEW.code;
	IF (OLD.del_id IS NOT NULL AND NEW.del_id IS NOT NULL) THEN
		RAISE EXCEPTION 'Update of a deleted % (Code: %) failed because deleted entities can''t be modified.', entity_name(TG_TABLE_NAME), NEW.code;
	END IF;
  RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER forbid_deleted_entity_modification 
	BEFORE UPDATE ON data 
	FOR EACH ROW EXECUTE PROCEDURE forbid_deleted_entity_modification();

CREATE TRIGGER forbid_deleted_entity_modification 
	BEFORE UPDATE ON samples 
	FOR EACH ROW EXECUTE PROCEDURE forbid_deleted_entity_modification();

CREATE TRIGGER forbid_deleted_entity_modification 
	BEFORE UPDATE ON experiments 
	FOR EACH ROW EXECUTE PROCEDURE forbid_deleted_entity_modification();
	
----------------------------------------------------------------------------------------------------
-- 2. data set
--- on insert/update - experiment, sample can't be deleted unless the data set is delete
---                  - parents/children relationship stays unchanged 
CREATE OR REPLACE FUNCTION check_created_or_modified_data_set_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
  -- check sample
  IF (NEW.samp_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM samples 
  	  WHERE id = NEW.samp_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected to a deleted Sample (Code: %).', NEW.code, owner_code;
		END IF;
	END IF;
	-- check experiment
	SELECT del_id, code INTO owner_del_id, owner_code
    FROM experiments 
    WHERE id = NEW.expe_id;
  IF (owner_del_id IS NOT NULL) THEN 
		RAISE EXCEPTION 'Insert/Update of Data Set (Code: %) failed because it cannot be connected to a deleted Experiment (Code: %).', NEW.code, owner_code;
	END IF;	
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_created_or_modified_data_set_owner_is_alive 
	AFTER INSERT OR UPDATE ON data
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW WHEN (NEW.del_id IS NULL)
	EXECUTE PROCEDURE check_created_or_modified_data_set_owner_is_alive();
	
----------------------------------------------------------------------------------------------------
-- 3. sample
--- on insert/update -> experiment can't be deleted unless the sample is deleted
--- deletion 
----> all directly connected data sets need to be deleted
----> all components and children need to be deleted

CREATE OR REPLACE FUNCTION check_created_or_modified_sample_owner_is_alive() RETURNS trigger AS $$
DECLARE
	owner_code	CODE;
	owner_del_id	TECH_ID;
BEGIN
  -- check experiment (can't be deleted)
  IF (NEW.expe_id IS NOT NULL) THEN
  	SELECT del_id, code INTO owner_del_id, owner_code
  	  FROM experiments 
  	  WHERE id = NEW.samp_id;
  	IF (owner_del_id IS NOT NULL) THEN 
			RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because it cannot be connected to a deleted Experiment (Code: %).', NEW.code, owner_code;
		END IF;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_created_or_modified_sample_owner_is_alive 
  AFTER INSERT OR UPDATE ON samples
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW WHEN (NEW.del_id IS NULL)
	EXECUTE PROCEDURE check_created_or_modified_sample_owner_is_alive();
	
CREATE OR REPLACE FUNCTION check_deletion_consistency_on_sample_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
  -- all directly connected data sets need to be deleted
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.samp_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its data sets is not deleted.', NEW.code;
	END IF;
  -- all components need to be deleted
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.samp_id_part_of = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its component samples is not deleted.', NEW.code;
	END IF;
	-- all children need to be deleted
	SELECT count(*) INTO counter 
		FROM sample_relationships sr, samples sc
		WHERE sample_id_parent = NEW.id AND sc.id = sr.sample_id_child AND sc.del_id IS NULL;
	IF (counter > 0) THEN
		RAISE EXCEPTION 'Sample (Code: %) deletion failed because at least one of its child samples is not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_sample_deletion 
  AFTER UPDATE ON samples
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	WHEN (OLD.del_id IS NULL AND NEW.del_id IS NOT NULL)
	EXECUTE PROCEDURE check_deletion_consistency_on_sample_deletion();	
	
----------------------------------------------------------------------------------------------------
-- 4. experiment
--- deletion -> all directly connected samples and data sets need to be deleted

CREATE OR REPLACE FUNCTION check_deletion_consistency_on_experiment_deletion() RETURNS trigger AS $$
DECLARE
  counter  INTEGER;
BEGIN
  -- check datasets
	SELECT count(*) INTO counter 
	  FROM data
	  WHERE data.expe_id = NEW.id AND data.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its data sets is not deleted.', NEW.code;
	END IF;
	-- check samples
	SELECT count(*) INTO counter 
	  FROM samples 
	  WHERE samples.expe_id = NEW.id AND samples.del_id IS NULL;
	IF (counter > 0) THEN
	  RAISE EXCEPTION 'Experiment (Code: %) deletion failed because at least one of its samples is not deleted.', NEW.code;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
  
CREATE CONSTRAINT TRIGGER check_deletion_consistency_on_experiment_deletion 
  AFTER UPDATE ON experiments
	DEFERRABLE INITIALLY DEFERRED
	FOR EACH ROW 
	WHEN (OLD.del_id IS NULL AND NEW.del_id IS NOT NULL)
	EXECUTE PROCEDURE check_deletion_consistency_on_experiment_deletion();