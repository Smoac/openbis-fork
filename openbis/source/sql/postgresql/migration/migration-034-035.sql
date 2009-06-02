------------------------------------------------------------------------------------
-- Extend events table
------------------------------------------------------------------------------------

-- TODO sprawdz czy dobrze zmigruje dane

ALTER TABLE events
		ADD COLUMN entity_type VARCHAR(80);
ALTER TABLE events
		ADD COLUMN identifier VARCHAR(250);
		
-- Creating check constraints

ALTER TABLE events ADD CONSTRAINT evet_enum_ck CHECK 
		(entity_type IN ('ATTACHMENT', 'DATASET', 'EXPERIMENT', 'MATERIAL', 'PROJECT', 'PROPERTY_TYPE', 'SAMPLE', 'VOCABULARY')); 

-- simple migration of events (all old rows contain data set deletion information)

UPDATE events SET 
		entity_type = 'DATASET',
		identifier = data_id,
		description = data_id;

-- remove old columns

ALTER TABLE events
    DROP COLUMN data_id;
ALTER TABLE data
		DROP COLUMN is_deleted; 
    
-- add constraints

ALTER TABLE events
    ALTER COLUMN entity_type SET NOT NULL;
ALTER TABLE events
    ALTER COLUMN identifier SET NOT NULL;