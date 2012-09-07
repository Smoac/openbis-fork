-- Migration from 119 to 120

-- drop old functions and triggers that checked sample code and subcode uniqueness
DROP TRIGGER sample_code_uniqueness_check ON samples_all;
DROP TRIGGER sample_subcode_uniqueness_check ON samples_all;

DROP FUNCTION sample_code_uniqueness_check();
DROP FUNCTION sample_subcode_uniqueness_check();


-- add columns that will store information necessary for efficient sample code and subcode uniqueness checks
ALTER TABLE samples_All ADD COLUMN code_unique_check character varying(300);
ALTER TABLE samples_All ADD COLUMN subcode_unique_check character varying(300);


-- add unique constraints on the new columns
ALTER TABLE samples_all
  ADD CONSTRAINT samp_code_unique_check_uk UNIQUE(code_unique_check);
ALTER TABLE samples_all
  ADD CONSTRAINT samp_subcode_unique_check_uk UNIQUE(subcode_unique_check);

  
-- function that fills in samples_all.code_unique_check column
CREATE OR REPLACE FUNCTION sample_fill_code_unique_check()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.code_unique_check = NEW.code || '_' || coalesce(NEW.samp_id_part_of, -1) || '_' || coalesce(NEW.dbin_id, -1) || '_' || coalesce(NEW.space_id, -1);
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
-- function that fills in samples_all.subcode_unique_check column
CREATE OR REPLACE FUNCTION sample_fill_subcode_unique_check()
  RETURNS trigger AS
$BODY$
DECLARE
    unique_subcode  BOOLEAN_CHAR;
BEGIN
    SELECT is_subcode_unique into unique_subcode FROM sample_types WHERE id = NEW.saty_id;
    
    IF (unique_subcode) THEN
    NEW.subcode_unique_check = NEW.code || '_' || coalesce(NEW.dbin_id, -1) || '_' || coalesce(NEW.space_id, -1);
    ELSE
    NEW.subcode_unique_check = NULL;
  END IF;
  
  RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
-- function that triggers recalculation of samples_all.subcode_unique_check column on sample_types.is_subcode_unique change
CREATE OR REPLACE FUNCTION sample_type_fill_subcode_unique_check()
  RETURNS trigger AS
$BODY$
BEGIN
    IF (NEW.is_subcode_unique::boolean <> OLD.is_subcode_unique::boolean) THEN
      UPDATE samples_all SET subcode_unique_check = subcode_unique_check WHERE saty_id = NEW.id;
  END IF;
    RETURN NEW;
END;
$BODY$
  LANGUAGE 'plpgsql';

  
-- triggers for the above functions  
CREATE TRIGGER sample_fill_code_unique_check
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE sample_fill_code_unique_check();

CREATE TRIGGER sample_fill_subcode_unique_check
  BEFORE INSERT OR UPDATE
  ON samples_all
  FOR EACH ROW
  EXECUTE PROCEDURE sample_fill_subcode_unique_check();
  
CREATE TRIGGER sample_type_fill_subcode_unique_check
  AFTER UPDATE
  ON sample_types
  FOR EACH ROW
  EXECUTE PROCEDURE sample_type_fill_subcode_unique_check();
  

-- fill in samples_all.code_unique_check and samples_all.subcode_unique_check columns for existing samples
UPDATE samples_all SET code_unique_check = code_unique_check;
