-------------------------------------------------------
-- rename HCS_ANALYSIS_WELL_FEATURES_CONTAINER to HCS_ANALYSIS_CONTAINER_WELL_FEATURES
-------------------------------------------------------

update data_set_types set code = 'HCS_ANALYSIS_CONTAINER_WELL_FEATURES' where code = 'HCS_ANALYSIS_WELL_FEATURES_CONTAINER'
