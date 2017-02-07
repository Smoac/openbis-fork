CREATE DOMAIN EDMS_ADDRESS_TYPE AS TEXT CHECK (VALUE IN ('OPENBIS', 'URL', 'FILE_SYSTEM'));
ALTER TABLE external_data_management_systems ADD COLUMN address_type EDMS_ADDRESS_TYPE;
UPDATE external_data_management_systems SET address_type='OPENBIS' WHERE is_openbis = true;
UPDATE external_data_management_systems SET address_type='URL' WHERE is_openbis = false;
ALTER TABLE external_data_management_systems DROP COLUMN is_openbis;
ALTER TABLE external_data_management_systems RENAME COLUMN url_template TO address;