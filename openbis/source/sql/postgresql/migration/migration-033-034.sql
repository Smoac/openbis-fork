------------------------------------------------------------------------------------
-- Create sequence for generating permanent identifiers starting with nextval of existing dataset sequence.
------------------------------------------------------------------------------------
CREATE FUNCTION CREATE_SEQUENCE(EXISTING_SEQUENCE VARCHAR, NEW_SEQUENCE VARCHAR) RETURNS VOID AS $$
DECLARE
  CURR_SEQ_VAL   INTEGER;
BEGIN
  SELECT INTO CURR_SEQ_VAL NEXTVAL(EXISTING_SEQUENCE);
  EXECUTE 'CREATE SEQUENCE ' || NEW_SEQUENCE || ' START WITH ' || CURR_SEQ_VAL;
  RETURN;
END;
$$ LANGUAGE 'plpgsql';

SELECT CREATE_SEQUENCE('DATA_ID_SEQ', 'PERM_ID_SEQ');
DROP FUNCTION CREATE_SEQUENCE(VARCHAR, VARCHAR);

------------------------------------------------------------------------------------
-- Add perm_id columns to samples and experiments.
------------------------------------------------------------------------------------
ALTER TABLE SAMPLES ADD COLUMN PERM_ID CODE;
ALTER TABLE EXPERIMENTS ADD COLUMN PERM_ID CODE;

UPDATE SAMPLES SET PERM_ID = to_char(registration_timestamp,'YYYYMMDDHHSSMS') || '-' || NEXTVAL('PERM_ID_SEQ');
UPDATE EXPERIMENTS SET PERM_ID = to_char(registration_timestamp,'YYYYMMDDHHSSMS') || '-' || NEXTVAL('PERM_ID_SEQ');
ALTER TABLE SAMPLES ALTER COLUMN PERM_ID SET NOT NULL;
ALTER TABLE EXPERIMENTS ALTER COLUMN PERM_ID SET NOT NULL;
ALTER TABLE SAMPLES ADD CONSTRAINT SAMP_PI_UK UNIQUE(PERM_ID);
ALTER TABLE EXPERIMENTS ADD CONSTRAINT EXPE_PI_UK UNIQUE(PERM_ID);


------------------------------------------------------------------------------------
-- Add column is_chosen_from_list to controlled_vocabularies.
------------------------------------------------------------------------------------
ALTER TABLE CONTROLLED_VOCABULARIES ADD COLUMN IS_CHOOSEN_FROM_LIST BOOLEAN_CHAR NOT NULL DEFAULT TRUE;

------------------------------------------------------------------------------------
-- Modify Data table - remove arc connection between Sample and Data table, use a flag instead
------------------------------------------------------------------------------------

-- add new columns

ALTER TABLE data
    ADD COLUMN is_derived boolean_char;
ALTER TABLE data
    ADD COLUMN samp_id tech_id;

-- update values in new columns

UPDATE data
    SET is_derived = (samp_id_derived_from IS NOT NULL); 
UPDATE data
    SET samp_id = samp_id_derived_from 
    WHERE is_derived = 'TRUE';
UPDATE data
    SET samp_id = samp_id_acquired_from 
    WHERE is_derived = 'FALSE';

-- add constraints and indexes to new columns

ALTER TABLE data
    ALTER COLUMN is_derived SET NOT NULL;
ALTER TABLE data
    ALTER COLUMN samp_id SET NOT NULL;   
ALTER TABLE data
    ADD CONSTRAINT data_samp_fk FOREIGN KEY (samp_id) REFERENCES samples (id);
CREATE INDEX data_samp_fk_i ON data USING btree (samp_id);

-- remove old columns

ALTER TABLE data
    DROP COLUMN samp_id_acquired_from;
ALTER TABLE data
    DROP COLUMN samp_id_derived_from;