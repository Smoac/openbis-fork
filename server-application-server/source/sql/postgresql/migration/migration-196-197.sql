-- Migration from 196 to 197

-- modify data_all table

ALTER TABLE data_all ADD COLUMN afs_data boolean_char NOT NULL DEFAULT 'F';
ALTER TABLE data_all ADD CONSTRAINT data_afs_data_dast_id_ck CHECK ((afs_data = 'F' AND dast_id IS NOT NULL) OR (afs_data = 'T' AND dast_id IS NULL));
ALTER TABLE data_all ALTER COLUMN dast_id DROP NOT NULL;
CREATE UNIQUE INDEX data_afs_data_expe_id_samp_id_uk ON data_all (expe_id, samp_id) WHERE (afs_data = 'T');

-- recreate data and data_deleted views

DROP VIEW data;
DROP VIEW data_deleted;

CREATE VIEW data AS
     SELECT id, code, dsty_id, dast_id, expe_id, expe_frozen, data_producer_code, production_timestamp, samp_id, samp_frozen,
            registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp,
            is_derived, del_id, orig_del, version, data_set_kind,
            frozen, frozen_for_children, frozen_for_parents, frozen_for_comps, frozen_for_conts, tsvector_document, meta_data
       FROM data_all
      WHERE del_id IS NULL AND afs_data = 'F';

CREATE VIEW data_deleted AS
     SELECT id, code, dsty_id, dast_id, expe_id, data_producer_code, production_timestamp, samp_id, registration_timestamp, access_timestamp, pers_id_registerer, pers_id_modifier, is_valid, modification_timestamp, is_derived, del_id, orig_del, version, data_set_kind
       FROM data_all
      WHERE del_id IS NOT NULL AND afs_data = 'F';
