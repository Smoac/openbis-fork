ALTER TABLE external_data_management_systems ADD COLUMN type TEXT_VALUE;
UPDATE external_data_management_systems SET type='OPENBIS' WHERE is_openbis = true;
UPDATE external_data_management_systems SET TYPE='UNDEFINED' WHERE is_openbis = false;
ALTER TABLE external_data_management_systems DROP COLUMN is_openbis, ALTER COLUMN type SET NOT NULL;
