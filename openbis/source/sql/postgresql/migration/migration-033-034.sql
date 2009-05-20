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
    
------------------------------------------------------------------------------------
-- Granting SELECT privilege to group openbis_readonly
------------------------------------------------------------------------------------

GRANT SELECT ON SEQUENCE attachment_content_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE attachment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE code_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE controlled_vocabulary_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE cvte_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_relationship_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_set_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_store_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE data_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE database_instance_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE dstpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE etpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE event_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE experiment_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE file_format_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE group_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE invalidation_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE locator_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_batch_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE material_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE mtpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE perm_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE person_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE project_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE property_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE role_assignment_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_property_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE sample_type_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON SEQUENCE stpt_id_seq TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE attachment_contents TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE attachments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE controlled_vocabularies TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE controlled_vocabulary_terms TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_relationships TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_set_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_stores TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE data_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE database_instances TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE database_version_logs TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE events TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiment_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE experiments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE external_data TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE file_format_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE groups TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE invalidations TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE locator_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_batches TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE material_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE materials TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE persons TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE projects TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE role_assignments TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_material_batches TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_properties TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_type_property_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE sample_types TO GROUP OPENBIS_READONLY;
GRANT SELECT ON TABLE samples TO GROUP OPENBIS_READONLY;
