-- Migration from 197 to 198

ALTER TABLE samples_all ADD COLUMN IMMUTABLE_DATA BOOLEAN_CHAR NOT NULL DEFAULT 'F';
ALTER TABLE EXPERIMENTS_ALL ADD COLUMN IMMUTABLE_DATA BOOLEAN_CHAR NOT NULL DEFAULT 'F';


CREATE OR REPLACE VIEW samples AS
     SELECT id, perm_id, code, proj_id, proj_frozen, expe_id, expe_frozen, saty_id, registration_timestamp,
            modification_timestamp, pers_id_registerer, pers_id_modifier, del_id, orig_del, space_id, space_frozen,
            samp_id_part_of, cont_frozen, version, frozen, frozen_for_comp, frozen_for_children, frozen_for_parents, frozen_for_data, tsvector_document, sample_identifier, meta_data, immutable_data
       FROM samples_all
      WHERE del_id IS NULL;

CREATE OR REPLACE VIEW experiments AS
     SELECT id, perm_id, code, exty_id, pers_id_registerer, pers_id_modifier, registration_timestamp, modification_timestamp,
            proj_id, proj_frozen, del_id, orig_del, is_public, version, frozen, frozen_for_samp, frozen_for_data, tsvector_document, meta_data, immutable_data
       FROM experiments_all
      WHERE del_id IS NULL;