// screening dictionary
var screening = {
    
// If a dictionary contains 'is_default_dictionary' key and the value is 'true', 
// that dictionary will be treated as a 'default' one. This means that values 
// from that dictionary will override entries with the same keys defined in other dictionaries. 
// There should be at most one default dictionary, otherwise the behavior is undefined.  
is_default_dictionary: "true",


GENE_LIBRARY_URL: "http://www.genecards.org/cgi-bin/carddisp.pl?gene={0}",
GENE_LIBRARY_SEARCH_URL: "http://www.genecards.org//index.php?path=/Search/keyword/{0}",

// 
// General
// 
PLATE: "Plate",
WELL_ROW: "Well Row",
WELL_COLUMN: "Well Column",
WELL: "Well",
WELL_IMAGES: "Well Images",
PREVIEW: "Preview",
PLATE_VIEWER_TITLE: "Plate {0}",
ANALYSIS_PROCEDURE: "Analysis Procedure",

//
// Menu items
//
WELLS_SEARCH_menu_item: "Wells Search",  
WELLS_SEARCH_tab_label: "Wells Search",  

//
// Experiment Viewer
//
experiment_samples_selction_title: "Plates",
no_samples_found: "There are no plates in this experiment",

//
// Sample Viewer
//

sample_properties_panel_sample_identifier: "ID",  
sample: "Sample",
sample_type: "Sample Type",
generated_samples: "Children Samples",
openbis_plate_metadata_browser_CODE: "Code",
openbis_plate_metadata_browser_TYPE: "Type",
openbis_plate_metadata_browser_THUMBNAIL: "Thumbnail",


sample_properties_heading: "Properties",
part_of_heading: "Components",
derived_samples_heading: "Derived Samples",
parent_samples_heading: "Parent Samples",
derived_sample: "Derived Sample",
external_data_heading: "Data Sets",
show_only_directly_connected: "directly connected",

//
// Sample import
//
import_scheduled: "Import has started successfully. Notification will be sent to '{0}' upon completion.",
register: "Register",
separator: "Separator",

//
// Material Viewer
//
plate_locations: "Plate Locations",
MATERIAL_MERGED_SUMMARY_SECTION_TITLE: "Summary",

//
// Plate Material Reviewer 
//    
SCREENING_MODULE_TITLE: "Screening",

WELL_CONTENT_MATERIAL: "Content",
WELL_CONTENT_MATERIAL_TYPE: "Content Type",
WELL_CONTENT_PROPERTIES: "Content Properties",
WELL_CONTENT_FEATURE_VECTORS: "Feature Vector",
IMAGE_ANALYSIS_DATA_SET: "Image Analysis Dataset",
IMAGE_DATA_SET: "Image Dataset",

PLATE_MATERIAL_REVIEWER_TITLE: "Wells Search Results",
PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS: "E.g. gene symbols, gene ids, gene descriptions, control names or compound names. Separate items with commas (\",\") or specify one item per line.",

EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION: "Wells Search",
EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION: "Library Index",
EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION: "Analysis Summary",
experiment_feature_vector_summary_section_RANK: "Rank",
experiment_feature_vector_summary_section_ID: "Id",
experiment_feature_vector_summary_section_DETAILS: "Details",

material_replica_feature_summary_FEATURE: "Feature",
material_replica_feature_summary_RANK: "Rank",
material_replica_feature_summary_MEDIAN: "Median",
material_replica_feature_summary_DEVIATION: "Deviation",

material_features_from_all_experiments_ASSAY: "Experiment",

EXACT_MATCH_ONLY: "Exact Matches Only",
WELLS_SEARCH_SHOW_COMBINED_RESULTS: "Show all matching results combined",
WELL_SEARCH_NO_RESULTS_IN_ANY_EXP_FOUND: "All experiments have been searched and no results match the query. Please check the spelling.",
WELL_SEARCH_NO_RESULTS_IN_SELECTED_EXP_FOUND: "No results match the query in the selected experiment.",
WELL_SEARCH_PERFORM_IN_ALL_EXP: "Search in all experiments",

MATERIAL_DISAMBIGUATION_TITLE: "Material Disambiguation",
MATERIAL_DISAMBIGUATION_GRID_EXPLANATION: "More than one result has been found. Click on it to see the details.",

image_viewer_button: "Adjust Colors",

ASSAY_HEADER: "Experiment {0}",
FIND_IN_ALL_ASSAYS: "Find {0} in all experiments",
MATERIAL_IN_ASSAY: "{0} in experiment {1}",
SHOW_ASSAY: "Show experiment {0}",
MATERIAL_IN_ALL_ASSAYS: "{0} in all experiments",

// LAST LINE: KEEP IT AT THE END
lastline: "" // we need a line without a comma
};