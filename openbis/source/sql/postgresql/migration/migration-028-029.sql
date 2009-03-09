-- -------
-- add modification_timestamp field to allow edition with Hibernate
-- -------

ALTER TABLE samples	ADD COLUMN modification_timestamp time_stamp_dfl;

ALTER TABLE experiments	ADD COLUMN modification_timestamp time_stamp_dfl;
ALTER TABLE experiment_properties	ADD COLUMN modification_timestamp time_stamp_dfl;ALTER TABLE material_properties	ADD COLUMN modification_timestamp time_stamp_dfl;

ALTER TABLE sample_properties	ADD COLUMN modification_timestamp time_stamp_dfl;

-- -------
-- add property data type which references materials
-- -------
ALTER TABLE property_types	ADD COLUMN maty_prop_id tech_id;

ALTER TABLE property_types	ADD CONSTRAINT prty_maty_fk FOREIGN KEY (maty_prop_id) REFERENCES material_types(id);
ALTER TABLE experiment_properties	ADD COLUMN mate_prop_id tech_id;ALTER TABLE material_properties	ADD COLUMN mate_prop_id tech_id;

ALTER TABLE sample_properties	ADD COLUMN mate_prop_id tech_id;ALTER TABLE experiment_properties	ADD CONSTRAINT expr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);ALTER TABLE material_properties	ADD CONSTRAINT mapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);ALTER TABLE sample_properties	ADD CONSTRAINT sapr_mapr_fk FOREIGN KEY (mate_prop_id) REFERENCES materials(id);

-- -------
-- remove the reminescents of the OBSERVABLE_TYPE
-- -------
ALTER TABLE data	DROP CONSTRAINT data_obty_fk;
ALTER TABLE data_set_types	DROP CONSTRAINT obty_pk;ALTER TABLE data_set_types	ADD CONSTRAINT dsty_pk PRIMARY KEY (id);

ALTER TABLE data	ADD CONSTRAINT data_dsty_fk FOREIGN KEY (dsty_id) REFERENCES data_set_types(id);
ALTER TABLE data_set_types	DROP CONSTRAINT obty_bk_uk;ALTER TABLE data_set_types	DROP CONSTRAINT obty_dbin_fk;ALTER TABLE data_set_types	ADD CONSTRAINT dsty_bk_uk UNIQUE (code, dbin_id);ALTER TABLE data_set_types	ADD CONSTRAINT dsty_dbin_fk FOREIGN KEY (dbin_id) REFERENCES database_instances(id);DROP INDEX data_obty_fk_i;CREATE INDEX data_dsty_fk_i ON data(dsty_id);