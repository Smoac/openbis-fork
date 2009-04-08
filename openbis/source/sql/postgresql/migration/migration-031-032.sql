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

-- -------
-- Add CODE_SEQ
-- -------

CREATE SEQUENCE CODE_SEQ;

------------------------------------------------------------------------------------
--  Purpose:  Replace trigger SAMPLE_CODE_UNIQUENESS_CHECK 
------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION SAMPLE_CODE_UNIQUENESS_CHECK() RETURNS trigger AS $$
DECLARE
   counter  INTEGER;
BEGIN
    LOCK TABLE samples IN EXCLUSIVE MODE;
    IF (NEW.samp_id_part_of is NULL) THEN
        IF (NEW.dbin_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and dbin_id = NEW.dbin_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code already exists.', NEW.code;
            END IF;
        ELSIF (NEW.grou_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of is NULL and grou_id = NEW.grou_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code already exists.', NEW.code;
            END IF;
        END IF;
        ELSE
        IF (NEW.dbin_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and dbin_id = NEW.dbin_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because database instance sample with the same code and being the part of the same parent already exists.', NEW.code;
            END IF;
        ELSIF (NEW.grou_id is not NULL) THEN
            SELECT count(*) into counter FROM samples 
                where id != NEW.id and code = NEW.code and samp_id_part_of = NEW.samp_id_part_of and grou_id = NEW.grou_id;
            IF (counter > 0) THEN
                RAISE EXCEPTION 'Insert/Update of Sample (Code: %) failed because group sample with the same code and being the part of the same parent already exists.', NEW.code;
            END IF;
        END IF;
        END IF;   
   RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';



