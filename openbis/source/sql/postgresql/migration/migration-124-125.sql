
ALTER TABLE authorization_groups ADD COLUMN version integer DEFAULT 0;
ALTER TABLE controlled_vocabularies ADD COLUMN version integer DEFAULT 0;
ALTER TABLE data_all ADD COLUMN version integer DEFAULT 0;
ALTER TABLE data_set_properties ADD COLUMN version integer DEFAULT 0;
ALTER TABLE data_set_relationships_all ADD COLUMN version integer DEFAULT 0;
ALTER TABLE data_stores ADD COLUMN version integer DEFAULT 0;
ALTER TABLE experiment_properties ADD COLUMN version integer DEFAULT 0;
ALTER TABLE experiments_all ADD COLUMN version integer DEFAULT 0;
ALTER TABLE filters ADD COLUMN version integer DEFAULT 0;
ALTER TABLE grid_custom_columns ADD COLUMN version integer DEFAULT 0;
ALTER TABLE material_properties ADD COLUMN version integer DEFAULT 0;
ALTER TABLE materials ADD COLUMN version integer DEFAULT 0;
ALTER TABLE projects ADD COLUMN version integer DEFAULT 0;
ALTER TABLE queries ADD COLUMN version integer DEFAULT 0;
ALTER TABLE sample_properties ADD COLUMN version integer DEFAULT 0;
ALTER TABLE sample_relationships_all ADD COLUMN version integer DEFAULT 0;
ALTER TABLE samples_all	ADD COLUMN version integer DEFAULT 0;

DROP VIEW data;
CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id, version 
       FROM data_all 
      WHERE del_id IS NULL;
      
DROP VIEW data_deleted;
CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, pers_id_registerer, pers_id_modifier, is_placeholder, is_valid, modification_timestamp, is_derived, ctnr_order, ctnr_id, del_id, version 
       FROM data_all 
      WHERE del_id IS NOT NULL;

DROP VIEW experiments;
CREATE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NULL;

DROP VIEW experiments_deleted;
CREATE VIEW experiments_deleted AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp, proj_id, del_id, is_public, version 
       FROM experiments_all 
      WHERE del_id IS NOT NULL;
      
DROP VIEW samples;
CREATE VIEW samples AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, dbin_id, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NULL;
      
DROP VIEW samples_deleted;
CREATE VIEW samples_deleted AS
     SELECT id, perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, dbin_id, space_id, samp_id_part_of, version 
       FROM samples_all 
      WHERE del_id IS NOT NULL;

DROP VIEW data_set_relationships;
CREATE VIEW data_set_relationships AS
   SELECT data_id_parent, data_id_child, del_id, pers_id_author, registration_timestamp, modification_timestamp, version
   FROM data_set_relationships_all 
   WHERE del_id IS NULL;
   
DROP VIEW sample_relationships;
CREATE VIEW sample_relationships AS
   SELECT id, sample_id_parent, relationship_id, sample_id_child, del_id, pers_id_author, registration_timestamp, modification_timestamp, version
   FROM sample_relationships_all
   WHERE del_id IS NULL;
      
