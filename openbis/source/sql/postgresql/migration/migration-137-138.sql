-- Migration from 137 to 138

-- Migration to support data sets to be contained in more than one container.

--
-- New relation ship type
--
insert into relationship_types
(id, 
code, 
label, 
parent_label, 
child_label, 
description, 
pers_id_registerer, 
is_managed_internally, 
is_internal_namespace, 
dbin_id) 
values
(
nextval('RELATIONSHIP_TYPE_ID_SEQ'),
'CONTAINER_COMPONENT',
'Container - Component', 
'Container', 
'Component', 
'Container - Component relationship', 
(select id from persons where user_id ='system'), 
'T', 
'T', 
(select id from database_instances where is_original_source = 'T')
);

--
-- Extending data set relationships table with column for type and order
--
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN RELATIONSHIP_ID TECH_ID;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD COLUMN ORDINAL INTEGER; 
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DSRE_DATA_FK_RELATIONSHIP FOREIGN KEY (RELATIONSHIP_ID) REFERENCES RELATIONSHIP_TYPES(ID);

DROP VIEW data_set_relationships;
CREATE VIEW data_set_relationships AS
   SELECT data_id_parent, data_id_child, relationship_id, ordinal, del_id, pers_id_author, registration_timestamp, modification_timestamp
   FROM data_set_relationships_all 
   WHERE del_id IS NULL;
   
UPDATE DATA_SET_RELATIONSHIPS_ALL SET RELATIONSHIP_ID = (select id from relationship_types where code = 'PARENT_CHILD');
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ALTER COLUMN RELATIONSHIP_ID SET NOT NULL;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL DROP CONSTRAINT DSRE_BK_UK;
ALTER TABLE DATA_SET_RELATIONSHIPS_ALL ADD CONSTRAINT DSRE_BK_UK UNIQUE(DATA_ID_CHILD,DATA_ID_PARENT,RELATIONSHIP_ID);


   
--
-- Migrate column CTNR_ID and CTNR_ORDER of DATA_ALL to DATA_SET_RELATIONSHIPS_ALL
--

INSERT INTO DATA_SET_RELATIONSHIPS_ALL 
(data_id_parent, data_id_child, relationship_id, ordinal, pers_id_author, registration_timestamp, modification_timestamp)
SELECT ctnr_id, d.id, t.id, ctnr_order, p.id, d.registration_timestamp, d.modification_timestamp 
FROM DATA_ALL as d, RELATIONSHIP_TYPES as t, PERSONS as p 
WHERE p.user_id = 'system' and ctnr_id IS NOT NULL and t.code = 'CONTAINER_COMPONENT'; 

UPDATE DATA_ALL SET ctnr_id = NULL, ctnr_order = NULL WHERE ctnr_id IS NOT NULL; 

--
-- Drop columns CTNR_ID and CTNR_ORDER in DATA
--

DROP VIEW data;
DROP VIEW data_deleted;
DROP RULE dataset_container_update ON data_all;
DROP RULE data_container_remove_update ON data_all;
DROP RULE data_container_insert ON data_all;
DROP RULE data_container_delete ON data_all;
DROP RULE data_parent_child_insert ON data_set_relationships_all;
DROP RULE data_parent_child_delete ON data_set_relationships_all;
DROP RULE data_parent_child_update ON data_set_relationships_all;
DROP RULE data_parent_child_revert_update ON data_set_relationships_all;

ALTER TABLE DATA_ALL DROP COLUMN CTNR_ID;
ALTER TABLE DATA_ALL DROP COLUMN CTNR_ORDER;

CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, del_id, orig_del, version 
       FROM data_all 
      WHERE del_id IS NULL;

CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, del_id, orig_del, version 
       FROM data_all 
      WHERE del_id IS NOT NULL;
      
--
-- Add column ORDINAL to DATA_SET_RELATIONSHIPS_HISTORY
--
ALTER TABLE DATA_SET_RELATIONSHIPS_HISTORY ADD COLUMN ORDINAL INTEGER;

--
-- Add rules for DATA_SET_RELATIONSHIPS_HISTORY
--

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

