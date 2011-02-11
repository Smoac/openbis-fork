-- Migration from 065 to 066

ALTER TABLE EXTERNAL_DATA ADD COLUMN SHARE_ID CODE;
ALTER TABLE EXTERNAL_DATA ADD COLUMN SIZE ORDINAL_INT;

----------------------------------------------------------------------------------------------------
-- Redo part of migration from 056 to 057
--
-- Introduction of a new data type - XML, was done in migration but data.sql wasn't updated.
-- Add the XML data type it if it doesn't exist.
----------------------------------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION insert_xml_data_type_if_not_exists() RETURNS void AS $$
DECLARE
	exists bool;
BEGIN
    SELECT true INTO exists
    FROM data_types WHERE code = 'XML';
    
    IF exists IS NULL then
       -- XML data type doesn't exist - insert it
       INSERT INTO data_types (id, code, description) 
              VALUES (nextval('data_type_id_seq'), 'XML', 'XML document');
    END IF;	
END;
$$ LANGUAGE 'plpgsql';


SELECT insert_xml_data_type_if_not_exists();
DROP FUNCTION insert_xml_data_type_if_not_exists();

----------------------------------------------------------------------------------------------------
-- Redo part of migration from 063 to 064
--
-- Domain SCRIPT_TYPE created in migration has different values than the one in domain.sql.
-- Alter the domain.
----------------------------------------------------------------------------------------------------

ALTER DOMAIN script_type DROP CONSTRAINT script_type_check;
ALTER DOMAIN script_type ADD CONSTRAINT script_type_check CHECK (VALUE IN ('DYNAMIC_PROPERTY', 'MANAGED_PROPERTY'));
