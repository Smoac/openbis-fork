
function SchwankLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(SchwankLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		
		this.storagesConfiguration = {
			"isEnabled" : true,
			/*
			* Should be the same across all storages, if not correct behaviour is not guaranteed.
			*/
			"STORAGE_PROPERTIES": [{
				"STORAGE_PROPERTY_GROUP" : "Physical Storage", //Where the storage will be painted.
				"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage", //Storage Group Name
				"NAME_PROPERTY" : 		"STORAGE_NAMES", //Should be a Vocabulary.
				"ROW_PROPERTY" : 		"STORAGE_ROW", //Should be an integer.
				"COLUMN_PROPERTY" : 	"STORAGE_COLUMN",  //Should be an integer.
				"BOX_PROPERTY" : 		"STORAGE_BOX_NAME", //Should be text.
				"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE", //Should be Vocabulary.
				"USER_PROPERTY" : 		"STORAGE_USER", //Should be text.
				"POSITION_PROPERTY" : 	"STORAGE_POSITION" //Should be text.
			}],
			/*
			* Storages map, can hold configurations for several storages.
			*/
			"STORAGE_CONFIGS": {
					"FRIDGE_MAIN_SMALL_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"FRIDGE_MAIN_SMALL_2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"FRIDGE_MAIN_SMALL_3" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FRIDGE_MAIN_SMALL_4" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FRIDGE_MAIN_SMALL_5" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_SMALL_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_SMALL_2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_SMALL_3" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_SMALL_4" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_SMALL_5" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_SMALL_6" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MAIN_LARGE_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 7, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_CELL_LAB_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 7, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 20 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FRIDGE_CELL_LAB_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MINUS80_UPSTAIRS_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 16 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"FREEZER_MINUS80_D-FLOOR_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 16 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},								
					"LIQUID_NITROGEN_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},				
					"CHEMICAL_CABINET" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 6, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 10 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},												
					"CUPBOARD" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},												
					"BENCH" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},												
			
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
					"SAMPLE_LINKS_HINT" : [
												{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false },{"TYPE" : "CONTAINED", "MANDATORY" : false }]
												}
										]
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
													}																						
												],
				},
				"CELLCULTURE_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Parental cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																							
												],
				},
				"HISTOCHEMISTRY_PROTOCOLS" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																							
												],
				},
				"EXPERIMENTAL_STEP" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [ {"TYPE" : "COMMENTS", "MANDATORY" : false }]
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
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
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
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
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
													},
					                             	{
														"LABEL" : "Cell culture protocol",
														"TYPE": "CELLCULTURE_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Histochemistry protocol",
														"TYPE": "HISTOCHEMISTRY_PROTOCOLS",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																																																													
												],
				}
		

		}
}
});
