-- Migration from 053 to 054


-- Add RELATIONSHIP_TYPES table
CREATE TABLE relationship_types (id TECH_ID NOT NULL, code CODE NOT NULL, label COLUMN_LABEL, parent_label COLUMN_LABEL, child_label COLUMN_LABEL, description DESCRIPTION_2000, registration_timestamp TIME_STAMP_DFL NOT NULL DEFAULT CURRENT_TIMESTAMP, pers_id_registerer TECH_ID NOT NULL, is_managed_internally BOOLEAN_CHAR NOT NULL DEFAULT 'F', is_internal_namespace BOOLEAN_CHAR NOT NULL DEFAULT 'F', dbin_id TECH_ID NOT NULL);

-- Add SAMPLE_RELATIONSHIPS table
CREATE TABLE sample_relationships (id TECH_ID NOT NULL, sample_id_parent TECH_ID NOT NULL, relationship_id TECH_ID NOT NULL, sample_id_child TECH_ID NOT NULL);

-- Add/update constraints
ALTER TABLE relationship_types ADD CONSTRAINT rety_pk PRIMARY KEY (id);
ALTER TABLE relationship_types ADD CONSTRAINT rety_uk UNIQUE(code,dbin_id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_pk PRIMARY KEY (id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_bk_uk UNIQUE(sample_id_child,sample_id_parent,relationship_id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_data_fk_child FOREIGN KEY (sample_id_child) REFERENCES samples(id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_data_fk_parent FOREIGN KEY (sample_id_parent) REFERENCES samples(id);
ALTER TABLE sample_relationships ADD CONSTRAINT sare_data_fk_relationship FOREIGN KEY (relationship_id) REFERENCES relationship_types(id);

-- Create index
CREATE INDEX sare_data_fk_i_child ON sample_relationships (sample_id_child);
CREATE INDEX sare_data_fk_i_parent ON sample_relationships (sample_id_parent);
CREATE INDEX sare_data_fk_i_relationship ON sample_relationships (relationship_id);

-- Create sequence for RELATIONSHIP_TYPES
CREATE SEQUENCE RELATIONSHIP_TYPE_ID_SEQ;
CREATE SEQUENCE SAMPLE_RELATIONSHIPS_ID_SEQ;

-- Create initial relationships
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
'PARENT_CHILD',
'Parent - Child', 
'Parent', 
'Child', 
'Parent - Child relationship', 
(select id from persons where user_id ='system'), 
'T', 
'T', 
(select id from database_instances where is_original_source = 'T')
);

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
'PLATE_CONTROL_LAYOUT',
'Plate - Control Layout', 
'Plate', 
'Control Layout', 
'Plate - Control Layout relationship', 
(select id from persons where user_id ='system'), 
'T', 
'T', 
(select id from database_instances where is_original_source = 'T')
); 


-- Migrate sample relationships to new schema
INSERT INTO sample_relationships (id, sample_id_parent,sample_id_child,relationship_id) (select distinct nextval('SAMPLE_RELATIONSHIPS_ID_SEQ') as id, s.SAMP_ID_GENERATED_FROM as parent_id, s.ID as child_id, rt.id as relationship_id from samples s, relationship_types rt  WHERE rt.code = 'PARENT_CHILD' and s.SAMP_ID_GENERATED_FROM is not null); 
INSERT INTO sample_relationships (id, sample_id_parent,sample_id_child,relationship_id) (select distinct nextval('SAMPLE_RELATIONSHIPS_ID_SEQ') as id, s.SAMP_ID_CONTROL_LAYOUT as parent_id, s.ID as child_id, rt.id as relationship_id from samples s, relationship_types rt  WHERE rt.code = 'PLATE_CONTROL_LAYOUT' and s.SAMP_ID_CONTROL_LAYOUT is not null);

-- Drop old sample relations
ALTER TABLE SAMPLES DROP COLUMN SAMP_ID_TOP;
ALTER TABLE SAMPLES DROP COLUMN SAMP_ID_GENERATED_FROM;
ALTER TABLE SAMPLES DROP COLUMN SAMP_ID_CONTROL_LAYOUT;










