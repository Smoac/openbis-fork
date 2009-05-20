-- JAVA ch.systemsx.cisd.openbis.generic.server.dataaccess.migration.MigrationStepFrom033To034

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

------------------------------------------------------------------------------------
-- Add column is_chosen_from_list to controlled_vocabularies.
------------------------------------------------------------------------------------
ALTER TABLE CONTROLLED_VOCABULARIES ADD COLUMN IS_CHOOSEN_FROM_LIST BOOLEAN_CHAR NOT NULL DEFAULT TRUE;

