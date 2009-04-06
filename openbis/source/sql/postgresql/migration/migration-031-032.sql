-- -------
-- Modify MATERIAL_BATCHES
-- -------

ALTER TABLE material_batches
    DROP COLUMN proc_id;

-- -------
-- Modify SAMPLES
-- -------

ALTER TABLE samples
    ADD COLUMN expe_id tech_id;

ALTER TABLE samples
    ADD CONSTRAINT samp_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);

CREATE INDEX samp_expe_fk_i ON samples USING btree (expe_id);

-- relink samples directly to experiments

UPDATE samples
SET expe_id = (SELECT e.id FROM experiments e, procedures p, sample_inputs si 
               WHERE si.samp_id = samples.id AND si.proc_id = p.id AND p.expe_id = e.id AND e.inva_id IS NULL);


-- -------
-- Modify DATA
-- -------

ALTER TABLE data
    ADD COLUMN expe_id tech_id;

ALTER TABLE data
    ADD CONSTRAINT data_expe_fk FOREIGN KEY (expe_id) REFERENCES experiments(id);

CREATE INDEX data_expe_fk_i ON data USING btree (expe_id);

UPDATE data
SET expe_id = (SELECT e.id FROM experiments e, procedures p 
               WHERE data.proc_id_produced_by = p.id AND p.expe_id = e.id);

ALTER TABLE data
    ALTER COLUMN expe_id SET NOT NULL;

ALTER TABLE data
    DROP COLUMN proc_id_produced_by;

-- -------
-- Drop PROCEDURES, PROCEDURE_TYPES, and SAMPLE_INPUTS
-- -------

DROP TABLE sample_inputs;
DROP TABLE procedures;
DROP TABLE procedure_types;

DROP SEQUENCE procedure_id_seq;
DROP SEQUENCE procedure_type_id_seq;



