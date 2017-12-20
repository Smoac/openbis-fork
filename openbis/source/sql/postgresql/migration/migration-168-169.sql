-- Migration from 168 to 169

------------------------------------------------------------------------------------
--  Purpose:  allow longer codes 
------------------------------------------------------------------------------------

-- drop rules depending on CODE
drop rule material_properties_update on material_properties;
drop rule material_properties_delete on material_properties;
drop rule sample_properties_update on sample_properties;
drop rule sample_properties_delete on sample_properties;
drop rule experiment_properties_update on experiment_properties;
drop rule experiment_properties_delete on experiment_properties;
drop rule data_set_properties_update on data_set_properties;
drop rule data_set_properties_delete on data_set_properties;
drop rule sample_experiment_update on samples_all;
drop rule sample_experiment_remove_update on samples_all;
drop rule sample_experiment_insert on samples_all;
drop rule sample_experiment_delete on samples_all;
drop rule sample_container_update on samples_all;
drop rule sample_container_remove_update on samples_all;
drop rule sample_container_insert on samples_all;
drop rule sample_container_delete on samples_all;
drop rule dataset_experiment_update on data_all;
drop rule dataset_experiment_remove_update on data_all;
drop rule dataset_experiment_insert on data_all;
drop rule dataset_experiment_delete on data_all;
drop rule dataset_sample_update on data_all;
drop rule dataset_sample_remove_update on data_all;
drop rule dataset_sample_insert on data_all;
drop rule dataset_sample_delete on data_all;
drop rule data_relationship_insert on data_set_relationships_all;
drop rule data_relationship_delete on data_set_relationships_all;
drop rule data_relationship_update on data_set_relationships_all;
drop rule data_relationship_trash_update on data_set_relationships_all;
drop rule data_relationship_trash_revert_update on data_set_relationships_all;
drop rule sample_parent_child_insert on sample_relationships_all;
drop rule sample_parent_child_delete on sample_relationships_all;
drop rule sample_parent_child_update on sample_relationships_all;
drop rule sample_parent_child_revert_update on sample_relationships_all;
drop rule experiment_project_update on experiments_all;
drop rule experiment_project_remove_update on experiments_all;
drop rule experiment_project_insert on experiments_all;
drop rule experiment_project_delete on experiments_all;
drop rule project_space_update on projects;
drop rule project_space_remove_update on projects;
drop rule project_space_insert on projects;
drop rule sample_project_update on samples_all;
drop rule sample_project_remove_update on samples_all;
drop rule sample_project_insert on samples_all;
drop rule sample_space_update on samples_all;
drop rule sample_space_remove_update on samples_all;
drop rule sample_space_insert on samples_all;

-- drop views depending on CODE
drop view data;
drop view data_deleted;
drop view experiments;
drop view experiments_deleted;
drop view samples;
drop view samples_deleted;

-- Switch all uses of the domain code (there are a lot of them!) to use varchar(60)
alter table AUTHORIZATION_GROUPS alter column CODE type varchar(60);
alter table CONTROLLED_VOCABULARIES alter column CODE type varchar(60);
alter table DATA_ALL alter column CODE type varchar(60), alter column DATA_PRODUCER_CODE type varchar(60);
alter table DATA_SET_TYPES alter column CODE type varchar(60);
alter table DATA_STORES alter column CODE type varchar(60), alter column UUID type varchar(60);
alter table DATA_TYPES alter column CODE type varchar(60);
alter table EXPERIMENT_TYPES alter column CODE type varchar(60);
alter table EXPERIMENTS_ALL alter column PERM_ID type varchar(60), alter column CODE type varchar(60);
alter table EXTERNAL_DATA alter column SHARE_ID type varchar(60);
alter table EXTERNAL_DATA_MANAGEMENT_SYSTEMS alter column CODE type varchar(60);
alter table FILE_FORMAT_TYPES alter column CODE type varchar(60);
alter table LOCATOR_TYPES alter column CODE type varchar(60);
alter table MATERIAL_TYPES alter column CODE type varchar(60);
alter table MATERIALS alter column CODE type varchar(60);
alter table METAPROJECTS alter column NAME type varchar(60);
alter table OPERATION_EXECUTIONS alter column CODE type varchar(60);
alter table PROJECTS alter column CODE type varchar(60), alter column PERM_ID type varchar(60);
alter table PROPERTY_TYPES alter column CODE type varchar(60);
alter table RELATIONSHIP_TYPES alter column CODE type varchar(60);
alter table QUERIES alter column DB_KEY type varchar(60), alter column ENTITY_TYPE_CODE type varchar(60);
alter table SAMPLE_TYPES alter column CODE type varchar(60), alter column generated_code_prefix type varchar(60);
alter table SAMPLES_ALL alter column PERM_ID type varchar(60), alter column CODE type varchar(60);
alter table SEMANTIC_ANNOTATIONS alter column PERM_ID type varchar(60);
alter table SPACES alter column CODE type varchar(60);

-- Convert CODE to VARCHAR(100)
drop DOMAIN CODE;
create DOMAIN CODE as varchar(100);

-- Switch all columns back to using the domain code
alter table AUTHORIZATION_GROUPS alter column CODE type CODE;
alter table CONTROLLED_VOCABULARIES alter column CODE type CODE;
alter table DATA_ALL alter column CODE type CODE, alter column DATA_PRODUCER_CODE type CODE;
alter table DATA_SET_TYPES alter column CODE type CODE;
alter table DATA_STORES alter column CODE type CODE, alter column UUID type CODE;
alter table DATA_TYPES alter column CODE type CODE;
alter table EXPERIMENT_TYPES alter column CODE type CODE;
alter table EXPERIMENTS_ALL alter column PERM_ID type CODE, alter column CODE type CODE;
alter table EXTERNAL_DATA alter column SHARE_ID type CODE;
alter table EXTERNAL_DATA_MANAGEMENT_SYSTEMS alter column CODE type CODE;
alter table FILE_FORMAT_TYPES alter column CODE type CODE;
alter table LOCATOR_TYPES alter column CODE type CODE;
alter table MATERIAL_TYPES alter column CODE type CODE;
alter table MATERIALS alter column CODE type CODE;
alter table METAPROJECTS alter column NAME type CODE;
alter table OPERATION_EXECUTIONS alter column CODE type CODE;
alter table PROJECTS alter column CODE type CODE, alter column PERM_ID type CODE;
alter table PROPERTY_TYPES alter column CODE type CODE;
alter table RELATIONSHIP_TYPES alter column CODE type CODE;
alter table QUERIES alter column DB_KEY type CODE, alter column ENTITY_TYPE_CODE type CODE;
alter table SAMPLE_TYPES alter column CODE type CODE, alter column generated_code_prefix type CODE;
alter table SAMPLES_ALL alter column PERM_ID type CODE, alter column CODE type CODE;
alter table SEMANTIC_ANNOTATIONS alter column PERM_ID type CODE;
alter table SPACES alter column CODE type CODE;

-- Recreate views
CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind 
       FROM data_all 
      WHERE del_id IS NULL;

CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind 
       FROM data_all 
      WHERE del_id IS NOT NULL;

CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NULL;

CREATE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, orig_del, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;

CREATE VIEW samples AS
     SELECT id, perm_id, code, proj_id, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NULL;

CREATE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, proj_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NOT NULL;

-- Recreate rules

CREATE OR REPLACE RULE material_properties_update AS
    ON UPDATE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
       
CREATE OR REPLACE RULE material_properties_delete AS
    ON DELETE TO material_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO 
       INSERT INTO material_properties_history (
         ID, 
         MATE_ID, 
         MTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('MATERIAL_PROPERTY_ID_SEQ'), 
         OLD.MATE_ID, 
         OLD.MTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Experiment Properties --

CREATE OR REPLACE RULE experiment_properties_update AS
    ON UPDATE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
       
CREATE OR REPLACE RULE experiment_properties_delete AS
    ON DELETE TO experiment_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL
    DO ALSO 
       INSERT INTO experiment_properties_history (
         ID, 
         EXPE_ID,
         ETPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('EXPERIMENT_PROPERTY_ID_SEQ'), 
         OLD.EXPE_ID, 
         OLD.ETPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Sample Properties --

CREATE OR REPLACE RULE sample_properties_update AS
    ON UPDATE TO sample_properties
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );
              
CREATE OR REPLACE RULE sample_properties_delete AS
    ON DELETE TO sample_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
     AND (SELECT DEL_ID FROM SAMPLES_ALL WHERE ID = OLD.SAMP_ID) IS NULL
     DO ALSO
       INSERT INTO sample_properties_history (
         ID, 
         SAMP_ID,
         STPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('SAMPLE_PROPERTY_ID_SEQ'), 
         OLD.SAMP_ID, 
         OLD.STPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- Data Set Properties --

CREATE OR REPLACE RULE data_set_properties_update AS
    ON UPDATE TO data_set_properties 
    WHERE (OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd' AND OLD.VALUE != NEW.VALUE) 
        OR (OLD.CVTE_ID IS NOT NULL AND OLD.CVTE_ID != NEW.CVTE_ID) 
        OR (OLD.MATE_PROP_ID IS NOT NULL AND OLD.MATE_PROP_ID != NEW.MATE_PROP_ID)
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         NEW.MODIFICATION_TIMESTAMP
       );

CREATE OR REPLACE RULE data_set_properties_delete AS
    ON DELETE TO data_set_properties 
    WHERE ((OLD.VALUE IS NOT NULL AND decode(replace(substring(OLD.value from 1 for 1), '\', '\\'), 'escape') != E'\\xefbfbd')
        OR OLD.CVTE_ID IS NOT NULL 
        OR OLD.MATE_PROP_ID IS NOT NULL)
     AND (SELECT DEL_ID FROM DATA_ALL WHERE ID = OLD.DS_ID) IS NULL
    DO ALSO
       INSERT INTO data_set_properties_history (
         ID, 
         DS_ID,
         DSTPT_ID, 
         VALUE, 
         VOCABULARY_TERM,
         MATERIAL, 
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP,
         VALID_UNTIL_TIMESTAMP 
       ) VALUES (
         nextval('DATA_SET_PROPERTY_ID_SEQ'), 
         OLD.DS_ID, 
         OLD.DSTPT_ID, 
         OLD.VALUE, 
         (select (t.code || ' [' || v.code || ']') from controlled_vocabulary_terms as t join controlled_vocabularies as v on t.covo_id = v.id where t.id = OLD.CVTE_ID),
         (select (m.code || ' [' || mt.code || ']') from materials as m join material_types as mt on m.maty_id = mt.id where m.id = OLD.MATE_PROP_ID),
         OLD.PERS_ID_AUTHOR,
         OLD.MODIFICATION_TIMESTAMP,
         current_timestamp
       );
       
-- sample -> experiment

CREATE OR REPLACE RULE sample_experiment_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.EXPE_ID != NEW.EXPE_ID OR OLD.EXPE_ID IS NULL) AND NEW.EXPE_ID IS NOT NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE sample_experiment_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.EXPE_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE sample_experiment_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NOT NULL
       DO ALSO (
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE sample_experiment_delete AS
    ON DELETE TO samples_all 
    WHERE OLD.EXPE_ID IS NOT NULL
       DO ALSO 
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
     
-- container samples
       
CREATE OR REPLACE RULE sample_container_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.SAMP_ID_PART_OF != NEW.SAMP_ID_PART_OF OR OLD.SAMP_ID_PART_OF IS NULL) AND NEW.SAMP_ID_PART_OF IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER')
           OR (MAIN_SAMP_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID_PART_OF AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINED');
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID_PART_OF, 
         'CONTAINER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'CONTAINED', 
         NEW.SAMP_ID_PART_OF, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID_PART_OF),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE sample_container_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.SAMP_ID_PART_OF IS NOT NULL AND NEW.SAMP_ID_PART_OF IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER')
           OR (MAIN_SAMP_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID_PART_OF AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINED');
    );
    
CREATE OR REPLACE RULE sample_container_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.SAMP_ID_PART_OF IS NOT NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID_PART_OF, 
         'CONTAINER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'CONTAINED', 
         NEW.SAMP_ID_PART_OF, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID_PART_OF),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE sample_container_delete AS
    ON DELETE TO samples_all 
    WHERE OLD.SAMP_ID_PART_OF IS NOT NULL
       DO ALSO 
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID_PART_OF AND SAMP_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL AND RELATION_TYPE = 'CONTAINER';

-- dataset -> eperiment

CREATE OR REPLACE RULE dataset_experiment_update AS
    ON UPDATE TO data_all 
    WHERE (OLD.EXPE_ID != NEW.EXPE_ID OR OLD.SAMP_ID IS NOT NULL) AND NEW.SAMP_ID IS NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_DATA_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL);
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE dataset_experiment_remove_update AS
    ON UPDATE TO data_all 
    WHERE OLD.SAMP_ID IS NULL AND NEW.SAMP_ID IS NOT NULL
    DO ALSO (
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_DATA_ID = OLD.ID AND EXPE_ID = OLD.EXPE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE dataset_experiment_insert AS
    ON INSERT TO data_all 
    WHERE NEW.EXPE_ID IS NOT NULL AND NEW.SAMP_ID IS NULL
       DO ALSO (
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.EXPE_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.EXPE_ID, 
         (SELECT PERM_ID FROM EXPERIMENTS_ALL WHERE ID = NEW.EXPE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE dataset_experiment_delete AS
    ON DELETE TO data_all 
    WHERE OLD.EXPE_ID IS NOT NULL AND OLD.SAMP_ID IS NULL
       DO ALSO 
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_EXPE_ID = OLD.EXPE_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

-- dataset -> sample

CREATE OR REPLACE RULE dataset_sample_update AS
    ON UPDATE TO data_all 
    WHERE (OLD.SAMP_ID != NEW.SAMP_ID OR OLD.SAMP_ID IS NULL) AND NEW.SAMP_ID IS NOT NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE (MAIN_DATA_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID AND VALID_UNTIL_TIMESTAMP IS NULL);
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SAMP_ID, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE dataset_sample_remove_update AS
    ON UPDATE TO data_all 
    WHERE OLD.SAMP_ID IS NOT NULL AND NEW.SAMP_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_DATA_ID = OLD.ID AND SAMP_ID = OLD.SAMP_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE dataset_sample_insert AS
    ON INSERT TO data_all 
    WHERE NEW.SAMP_ID IS NOT NULL
       DO ALSO (
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         DATA_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.SAMP_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.CODE,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_DATA_ID,
         RELATION_TYPE, 
         SAMP_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SAMP_ID, 
         (SELECT PERM_ID FROM SAMPLES_ALL WHERE ID = NEW.SAMP_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE dataset_sample_delete AS
    ON DELETE TO data_all 
    WHERE OLD.SAMP_ID IS NOT NULL
       DO ALSO 
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_SAMP_ID = OLD.SAMP_ID AND DATA_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

-- data set relationship

CREATE OR REPLACE RULE data_relationship_insert AS
    ON INSERT TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );

CREATE OR REPLACE RULE data_relationship_delete AS
    ON DELETE TO data_set_relationships_all
    WHERE OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
       );
       
CREATE OR REPLACE RULE data_relationship_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );
       
CREATE OR REPLACE RULE data_relationship_trash_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE NEW.DEL_ID IS NOT NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE DATA_SET_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_DATA_ID = OLD.DATA_ID_PARENT 
                  AND DATA_ID = OLD.DATA_ID_CHILD
                  AND RELATION_TYPE = (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                  AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_DATA_ID = OLD.DATA_ID_CHILD 
                 AND DATA_ID = OLD.DATA_ID_PARENT 
                 AND RELATION_TYPE = (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = OLD.RELATIONSHIP_ID)
                 AND VALID_UNTIL_TIMESTAMP IS NULL);
       );
       
CREATE OR REPLACE RULE data_relationship_trash_revert_update AS
    ON UPDATE TO data_set_relationships_all
    WHERE OLD.DEL_ID IS NOT NULL AND NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_PARENT, 
           (SELECT UPPER(PARENT_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_CHILD, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
         INSERT INTO DATA_SET_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_DATA_ID,
           RELATION_TYPE, 
           DATA_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP,
           ORDINAL
         ) VALUES (
           nextval('DATA_SET_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.DATA_ID_CHILD, 
           (SELECT UPPER(CHILD_LABEL) FROM RELATIONSHIP_TYPES WHERE ID = NEW.RELATIONSHIP_ID), 
           NEW.DATA_ID_PARENT, 
           (SELECT CODE FROM data_all WHERE ID = NEW.DATA_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP,
           NEW.ORDINAL
         );
       );

CREATE OR REPLACE RULE sample_parent_child_insert AS
    ON INSERT TO sample_relationships_all
    WHERE NEW.DEL_ID IS NULL
       DO ALSO (
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );
       
CREATE OR REPLACE RULE sample_parent_child_delete AS
    ON DELETE TO sample_relationships_all
    WHERE OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_SAMP_ID = OLD.SAMPLE_ID_PARENT AND SAMP_ID = OLD.SAMPLE_ID_CHILD AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_SAMP_ID = OLD.SAMPLE_ID_CHILD AND SAMP_ID = OLD.SAMPLE_ID_PARENT AND VALID_UNTIL_TIMESTAMP IS NULL);
       );

CREATE OR REPLACE RULE sample_parent_child_update AS
    ON UPDATE TO sample_relationships_all
    WHERE NEW.DEL_ID IS NOT NULL AND OLD.DEL_ID IS NULL
       DO ALSO (
         UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp
           WHERE (MAIN_SAMP_ID = OLD.SAMPLE_ID_PARENT AND SAMP_ID = OLD.SAMPLE_ID_CHILD AND VALID_UNTIL_TIMESTAMP IS NULL)
             OR (MAIN_SAMP_ID = OLD.SAMPLE_ID_CHILD AND SAMP_ID = OLD.SAMPLE_ID_PARENT AND VALID_UNTIL_TIMESTAMP IS NULL);
       );

CREATE OR REPLACE RULE sample_parent_child_revert_update AS
    ON UPDATE TO sample_relationships_all
    WHERE NEW.DEL_ID IS NULL AND OLD.DEL_ID IS NOT NULL
       DO ALSO (
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_PARENT, 
           'PARENT', 
           NEW.SAMPLE_ID_CHILD, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_CHILD),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
         INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
           ID, 
           MAIN_SAMP_ID,
           RELATION_TYPE, 
           SAMP_ID, 
           ENTITY_PERM_ID,
           PERS_ID_AUTHOR,
           VALID_FROM_TIMESTAMP
         ) VALUES (
           nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
           NEW.SAMPLE_ID_CHILD, 
           'CHILD', 
           NEW.SAMPLE_ID_PARENT, 
           (SELECT PERM_ID FROM samples_all WHERE ID = NEW.SAMPLE_ID_PARENT),
           NEW.PERS_ID_AUTHOR,
           NEW.MODIFICATION_TIMESTAMP
         );
       );

-- experiment -> project

CREATE OR REPLACE RULE experiment_project_update AS
    ON UPDATE TO experiments_all 
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL) AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.PROJ_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_EXPE_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT perm_id FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE experiment_project_remove_update AS
    ON UPDATE TO experiments_all 
    WHERE OLD.PROJ_ID IS NOT NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       UPDATE EXPERIMENT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_EXPE_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE experiment_project_insert AS
    ON INSERT TO experiments_all 
    WHERE NEW.PROJ_ID IS NOT NULL
       DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         EXPE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.PROJ_ID, 
         'OWNER', 
         NEW.ID, 
         NEW.PERM_ID,
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
       INSERT INTO EXPERIMENT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_EXPE_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('EXPERIMENT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT perm_id FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );
   
CREATE OR REPLACE RULE experiment_project_delete AS
    ON DELETE TO experiments_all 
    WHERE OLD.PROJ_ID IS NOT NULL
       DO ALSO 
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = current_timestamp 
         WHERE MAIN_PROJ_ID = OLD.PROJ_ID AND EXPE_ID = OLD.ID AND VALID_UNTIL_TIMESTAMP IS NULL;

-- project -> space

CREATE OR REPLACE RULE project_space_update AS
    ON UPDATE TO projects 
    WHERE (OLD.SPACE_ID != NEW.SPACE_ID OR OLD.SPACE_ID IS NULL) AND NEW.SPACE_ID IS NOT NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );
    
CREATE OR REPLACE RULE project_space_remove_update AS
    ON UPDATE TO projects 
    WHERE OLD.SPACE_ID IS NOT NULL AND NEW.SPACE_ID IS NULL
    DO ALSO (
       UPDATE PROJECT_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP
         WHERE MAIN_PROJ_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );
    
CREATE OR REPLACE RULE project_space_insert AS
    ON INSERT TO projects 
    WHERE NEW.SPACE_ID IS NOT NULL
       DO ALSO (
       INSERT INTO PROJECT_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_PROJ_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('PROJECT_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

-- sample -> project

CREATE OR REPLACE RULE sample_project_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.PROJ_ID != NEW.PROJ_ID OR OLD.PROJ_ID IS NULL OR OLD.EXPE_ID IS NOT NULL) AND NEW.PROJ_ID IS NOT NULL AND NEW.EXPE_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_project_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.PROJ_ID IS NOT NULL AND (NEW.PROJ_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL))
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND PROJ_ID = OLD.PROJ_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_project_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NOT NULL
    DO ALSO (
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         PROJ_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.PROJ_ID, 
         (SELECT PERM_ID FROM PROJECTS WHERE ID = NEW.PROJ_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

-- sample -> project

CREATE OR REPLACE RULE sample_space_update AS
    ON UPDATE TO samples_all 
    WHERE (OLD.SPACE_ID != NEW.SPACE_ID OR OLD.SPACE_ID IS NULL OR OLD.EXPE_ID IS NOT NULL OR OLD.PROJ_ID IS NOT NULL) AND NEW.SPACE_ID IS NOT NULL AND NEW.EXPE_ID IS NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
       INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
    );

CREATE OR REPLACE RULE sample_space_remove_update AS
    ON UPDATE TO samples_all 
    WHERE OLD.SPACE_ID IS NOT NULL AND (NEW.SPACE_ID IS NULL OR (OLD.EXPE_ID IS NULL AND NEW.EXPE_ID IS NOT NULL) OR (OLD.PROJ_ID IS NULL AND NEW.PROJ_ID IS NOT NULL))
    DO ALSO (
       UPDATE SAMPLE_RELATIONSHIPS_HISTORY SET VALID_UNTIL_TIMESTAMP = NEW.MODIFICATION_TIMESTAMP 
         WHERE MAIN_SAMP_ID = OLD.ID AND SPACE_ID = OLD.SPACE_ID AND VALID_UNTIL_TIMESTAMP IS NULL;
    );

CREATE OR REPLACE RULE sample_space_insert AS
    ON INSERT TO samples_all 
    WHERE NEW.EXPE_ID IS NULL AND NEW.SPACE_ID IS NOT NULL AND NEW.PROJ_ID IS NULL
    DO ALSO (
      INSERT INTO SAMPLE_RELATIONSHIPS_HISTORY (
         ID, 
         MAIN_SAMP_ID,
         RELATION_TYPE, 
         SPACE_ID, 
         ENTITY_PERM_ID,
         PERS_ID_AUTHOR,
         VALID_FROM_TIMESTAMP
       ) VALUES (
         nextval('SAMPLE_RELATIONSHIPS_HISTORY_ID_SEQ'), 
         NEW.ID, 
         'OWNED', 
         NEW.SPACE_ID, 
         (SELECT CODE FROM SPACES WHERE ID = NEW.SPACE_ID),
         NEW.PERS_ID_MODIFIER,
         NEW.MODIFICATION_TIMESTAMP
       );
   );

