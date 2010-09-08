// screening dictionary
var screening = {

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
  
  
  //
  // Sample Viewer
  //
  
  sample: "Sample",
  sample_type: "Sample Type",
  generated_samples: "Children Samples",
  openbis_plate_metadata_browser_CODE: "Code",
  openbis_plate_metadata_browser_TYPE: "Type",
  openbis_plate_metadata_browser_THUMBNAIL: "Thumbnail",

	//
	// Sample import
	//
  import_scheduled: "Import has started successfully. Notification will be sent to '{0}' upon completion.",
	register: "Register",
	
	//
	// Gene Viewer
	//
	plate_locations: "Plate Locations",
  
	//
	// Plate Material Reviewer 
	//    
    SCREENING_MODULE_TITLE: "Screening",
    
    WELL_CONTENT_MATERIAL: "Content",
    WELL_CONTENT_MATERIAL_TYPE: "Content Type",
    WELL_CONTENT_PROPERTIES: "Content Properties",
    PLATE_MATERIAL_REVIEWER_TITLE: "Wells Reviewing Panel",
    PLATE_MATERIAL_REVIEWER_HELP_INFO: "Specify a list of materials contained in the wells. You can use material codes or properties. The content of all wells in this experiment will be checked and wells containing specified materials will be shown.",
    PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS: "List of material codes or properties (e.g. gene ids, gene symbols, gene descriptions, control names, compound names) separated by commas (\",\") or one item per line.",

    EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION: "Wells Reviewing Panel",
    EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION: "Well Materials",
		EXACT_MATCH_ONLY: "Exact Matches Only",
	
  
  // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};