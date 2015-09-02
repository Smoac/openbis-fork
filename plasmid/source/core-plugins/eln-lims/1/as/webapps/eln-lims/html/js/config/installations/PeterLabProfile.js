
function PeterLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(PeterLabProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.inventorySpaces = ["MATERIALS", "METHODS"];
		this.isShowUnavailablePreviewOnSampleTable = true;
		this.hideCodes = true;
	
	
		this.storagesConfiguration = {
				"isEnabled" : true,
				/*
				 * Should be the same across all storages, if not correct behaviour is not guaranteed.
				*/
				/*
				"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Storage", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Group 1", //Storage Group Name
					"NAME_PROPERTY" : "STORAGE_NAME_1", //Should be a Vocabulary.
					"ROW_PROPERTY" : "STORAGE_ROW", //Should be an integer.
					"COLUMN_PROPERTY" : "STORAGE_COLUMN",  //Should be an integer.
					"BOX_PROPERTY" : "STORAGE_BOX_NAME", //Should be text.
					"USER_PROPERTY" : "USER_PROPERTY" //Should be text.
				},
				{
					"STORAGE_PROPERTY_GROUP" : "Storage 2", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Group 2", //Storage Group Name
					"NAME_PROPERTY" : "FREEZER_NAME_2", //Should be a Vocabulary.
					"ROW_PROPERTY" : "ROW_2", //Should be an integer.
					"COLUMN_PROPERTY" : "COLUMN_2", //Should be an integer.
					"BOX_PROPERTY" : "BOX_NUMBER_2", //Should be text.
					"USER_PROPERTY" : "USER_PROPERTY_2" //Should be text.
				}],

*/
			"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Physical Storage_1", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage_1", //Storage Group Name
					"NAME_PROPERTY" : 		"STORAGE_NAME_1", //Should be a Vocabulary.
					"ROW_PROPERTY" : 		"STORAGE_ROW_1", //Should be an integer.
					"COLUMN_PROPERTY" : 	"STORAGE_COLUMN_1",  //Should be an integer.
					"BOX_PROPERTY" : 		"STORAGE_BOX_NAME_1", //Should be text.
					"USER_PROPERTY" : 		"STORAGE_USER_1", //Should be text.
					"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE_1", //Should be Vocabulary.
					"POSITION_PROPERTY" : 	"STORAGE_BOX_POSITION_1" //Should be text.
				},
				{
					"STORAGE_PROPERTY_GROUP" : "Physical Storage_2", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage_2", //Storage Group Name
					"NAME_PROPERTY" : 		"STORAGE_NAME_2", //Should be a Vocabulary.
					"ROW_PROPERTY" : 		"STORAGE_ROW_2", //Should be an integer.
					"COLUMN_PROPERTY" : 	"STORAGE_COLUMN_2",  //Should be an integer.
					"BOX_PROPERTY" : 		"STORAGE_BOX_NAME_2", //Should be text.
					"USER_PROPERTY" : 		"STORAGE_USER_2", //Should be text.
					"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE_2", //Should be Vocabulary.
					"POSITION_PROPERTY" : 	"STORAGE_BOX_POSITION_2" //Should be text.
				},


				],
				/*

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

		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "EXPERIMENTAL_STEP") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				var freeFormTableController = new FreeFormTableController(sample, isEnabled);
				freeFormTableController.init($("#" + containerId));
			}
		}
		
		//The properties you want to appear on the tables, if you don�t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
}
});
