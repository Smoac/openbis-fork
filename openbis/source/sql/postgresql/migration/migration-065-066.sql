-- Migration from 065 to 066

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
-- This is a screening specific migration. Nothing will be performed on openBIS databases 
-- which are not screening specific.
-- 
-- This migration for each existing connection between oligo well, oligo material and gene material
-- creates a direct connection between the well and the gene. 
--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
 
CREATE OR REPLACE FUNCTION INSERT_DATASET_TYPE_IF_NOT_PRESENT(new_code text, new_description text) RETURNS void AS $$
DECLARE
	exists bool;
BEGIN
	
	select true into exists from data_set_types	where code = new_code;
	
	if exists IS NOT NULL then 
		return;
	end if;   
	
	insert into data_set_types(
		id, 
		code, description, 
		dbin_id)
	values(
			nextval('data_set_type_id_seq'), 
			new_code,
			new_description,
			(select id from database_instances where is_original_source = 'T')
		);
		
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION CREATE_HCS_DATASET_TYPES() RETURNS void AS $$
DECLARE
	hcs_image_dataset_exists bool;
BEGIN
	
	select true 
	into hcs_image_dataset_exists
	from data_set_types 
	where code = 'HCS_IMAGE';
	
	if hcs_image_dataset_exists IS NULL then 
		-- skip migration if there is not HCS_IMAGE dataset type
		return;
	end if;   
	
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_IMAGE_RAW',	
			'Raw High Content Screening Images');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_IMAGE_OVERVIEW',
			'Overview High Content Screening Images. Generated from raw images.');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_IMAGE_SEGMENTATION',
			'HCS Segmentation Images (overlays)');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_ANALYSIS_WELL_FEATURES',
			'HCS image analysis well feature vectors.');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_ANALYSIS_CELL_SEGMENTATION',
			'HCS image analysis cell segmentation');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(	
			'HCS_ANALYSIS_CELL_FEATURES',
			'HCS image analysis cell feature vectors');
	PERFORM INSERT_DATASET_TYPE_IF_NOT_PRESENT(
			'HCS_ANALYSIS_CELL_CLASS',
			'HCS image analysis cell classification');

END;
$$ LANGUAGE 'plpgsql';

select CREATE_HCS_DATASET_TYPES();
drop function CREATE_HCS_DATASET_TYPES();
drop function INSERT_DATASET_TYPE_IF_NOT_PRESENT(text, text);

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------=======
