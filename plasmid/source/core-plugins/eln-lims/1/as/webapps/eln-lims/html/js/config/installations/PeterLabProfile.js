
function PeterLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(PeterLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.storagesConfiguration = {
			"isEnabled" : true,
			"STORAGE_PROPERTIES": [],
			/*
			* Storages map, can hold configurations for several storages.
			*/
			"STORAGE_CONFIGS": {
				"G9_FRIDGE-1" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G9_FRIDGE-2" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 5, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},	
				"G9_FREEZER-A" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},	
				"G9_FREEZER-B" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},	
				"G9_FREEZER-C" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 3, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FREEZER-D" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 7, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FREEZER-E" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 14, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FREEZER-F" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 3, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FREEZER-G" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FREEZER-H" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FREEZER-I" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 9, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FRIDGE-3" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 5, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FRIDGE-4" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FRIDGE-5" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 3, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FRIDGE-6" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G10_FRIDGE-7" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G11_FRIDGE-8" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G14_FREEZER-J" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"G14_FREEZER-K" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 8, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},																
				"G14_FREEZER-L" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},								
				"G14_FREEZER-M" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},								
				"G14_FRIDGE-9" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},										
				"G14_FRIDGE-10" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},									
				"G17_FRIDGE-11" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 6, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},										
				"G17_FRIDGE-12" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 3, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},													
				"G17_FREEZER-N" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 4, //Number of rows
								"COLUMN_NUM" : 4, //Number of columns
								"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},
				"BENCH" : 	{ //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 1, //Number of rows
								"COLUMN_NUM" : 1, //Number of columns
								"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},																				
				"USER_BENCH" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 1, //Number of rows
								"COLUMN_NUM" : 1, //Number of columns
								"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},												
				"USER_BENCH-80" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 1, //Number of rows
								"COLUMN_NUM" : 1, //Number of columns
								"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},												
				"USER_BENCH-20" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 1, //Number of rows
								"COLUMN_NUM" : 1, //Number of columns
								"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							},												
				"USER_BENCH-RT" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 1, //Number of rows
								"COLUMN_NUM" : 1, //Number of columns
								"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							}
				}
		};
		
		var getStoragGroupFromTemplate = function(groupNumber) {
			return {
				"STORAGE_PROPERTY_GROUP" : "Physical Storage " + groupNumber, //Where the storage will be painted.
				"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage " + groupNumber, //Storage Group Name
				"NAME_PROPERTY" : 		"STORAGE_NAME_" + groupNumber, //Should be a Vocabulary.
				"ROW_PROPERTY" : 		"STORAGE_ROW_" + groupNumber, //Should be an integer.
				"COLUMN_PROPERTY" : 	"STORAGE_COLUMN_" + groupNumber,  //Should be an integer.
				"BOX_PROPERTY" : 		"STORAGE_BOX_NAME_" + groupNumber, //Should be text.
				"USER_PROPERTY" : 		"STORAGE_USER_" + groupNumber, //Should be text.
				"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE_" + groupNumber, //Should be Vocabulary.
				"POSITION_PROPERTY" : 	"STORAGE_BOX_POSITION_" + groupNumber //Should be text.
			};
		}
		
		var numberOfStorageGroups = 65;
		for(var sIdx = 1; sIdx <= numberOfStorageGroups; sIdx++) {
			this.storagesConfiguration["STORAGE_PROPERTIES"].push(getStoragGroupFromTemplate(sIdx));
		}
	
		/* New Sample definition tests*/
		this.sampleTypeDefinitionsExtension = {
				"PLASMID_EXPRESSION" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Protocol",
														"TYPE": "PROTOCOLS",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : []
													}
													,
													{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMIDS",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : []
													}
													,
													{
														"LABEL" : "Inhibitor",
														"TYPE": "INHIBITORS",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : []
													}
													,
													{
														"LABEL" : "Cell Line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : []
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
														"LABEL" : "Cell",
														"TYPE": "CELL",
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
														"LABEL" : "siRNA",
														"TYPE": "SIRNA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Yeast strain",
														"TYPE": "STRAIN",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Yeast collection",
														"TYPE": "YEAST_COLLECTION",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																																
												],
				}

		}
}
});
