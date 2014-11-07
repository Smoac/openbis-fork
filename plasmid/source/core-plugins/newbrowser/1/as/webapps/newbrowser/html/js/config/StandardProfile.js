
function StandardProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(StandardProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.inventorySpaces = ["INVENTORY"];
		
		this.storagesConfiguration = {
				"isEnabled" : true,
				/*
				 * Should be the same across all storages, if not correct behaviour is not guaranteed.
				*/
				"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Storage Utility", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Utility", //Storage Group Name
					"NAME_PROPERTY" : "FREEZER_NAME", //Should be a Vocabulary.
					"ROW_PROPERTY" : "ROW", //Should be an integer.
					"COLUMN_PROPERTY" : "COLUMN",  //Should be an integer.
					"BOX_PROPERTY" : "BOX_NUMBER", //Should be text.
					"USER_PROPERTY" : "USER_PROPERTY" //Should be text.
				}],
				/*
				 * Storages map, can hold configurations for several storages.
				*/
				"STORAGE_CONFIGS": {
					"MINUS80_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"MINUS80_2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},	
					"MINUS820_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},																
					"BENCH" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								}
				}
			};
	
		/* New Sample definition tests*/
		this.sampleTypeDefinitionsExtension = {
				"MEDIA" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																								
												],
				},

				"SOLUTION_BUFFER" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																								
												],
				},

				"GENERAL_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "General protocol",
														"TYPE": "GENERAL_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																												
												],
				},

				"PCR_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},																								
												],
				},

				"WESTERN_BLOTTING_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},																								
												],
				},

				"PLASMID" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																		
												],
				},

				"BACTERIA" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Bacteria parents",
														"TYPE": "BACTERIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																				
												],
				},

				"YEAST" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Yeast parents",
														"TYPE": "YEAST",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																					
												],
				},

				"CELL_LINE" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Parental cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},		
					                             	{
														"LABEL" : "Parental fly",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																									
												],
				},

				"FLY" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Fly parents",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																					
												],
				},

				"EXPERIMENTAL_STEP" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Bacteria",
														"TYPE": "BACTERIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Fly",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Oligo",
														"TYPE": "OLIGO",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "RNA",
														"TYPE": "RNA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Yeast",
														"TYPE": "YEAST",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "General protocol",
														"TYPE": "GENERAL_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "PCR protocol",
														"TYPE": "PCR_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Western blotting protocol",
														"TYPE": "WESTERN_BLOTTING_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																																			
												],
				}

		} 

		
		//The properties you want to appear on the tables, if you don«t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
		
		//The colors for the notes, if you don«t specify the color, light yellow will be used by default.
		this.colorForInspectors = {};


		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "EXPERIMENTAL_STEP") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				var freeFormTableController = new FreeFormTableController(sample, isEnabled);
				freeFormTableController.init($("#" + containerId));
			}

		}
}
});