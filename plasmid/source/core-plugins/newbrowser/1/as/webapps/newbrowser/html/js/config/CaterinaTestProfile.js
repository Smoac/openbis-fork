
function CaterinaTestProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(CaterinaTestProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.typeGroups = {
			"MATERIALS" : {
				"TYPE" : "MATERIALS",
				"DISPLAY_NAME" : "Materials",
				"LIST" : ["INHIBITORS"]
			},
			"SAMPLES" : {
				"TYPE" : "SAMPLES",
				"DISPLAY_NAME" : "Samples",
				"LIST" : ["CELL_LINE", "ANTIBODIES", "PLASMIDS"]
			},
			"METHODS" : {
				"TYPE" : "METHODS",
				"DISPLAY_NAME" : "Protocols",
				"LIST" : ["PROTOCOL"]
			},
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] 
			}
		};

		this.inventorySpaces = ["INVENTORY"];
	
	
		this.storagesConfiguration = {
				"isEnabled" : true,
				/*
				 * Should be the same across all storages, if not correct behaviour is not guaranteed.
				*/
				"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Storage", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Group 1", //Storage Group Name
					"NAME_PROPERTY" : "FREEZER_NAME", //Should be a Vocabulary.
					"ROW_PROPERTY" : "ROW", //Should be an integer.
					"COLUMN_PROPERTY" : "COLUMN",  //Should be an integer.
					"BOX_PROPERTY" : "BOX_NUMBER", //Should be text.
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
				}
		}

		
		//The properties you want to appear on the tables, if you don«t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
		
		//The colors for the notes, if you don«t specify the color, light yellow will be used by default.
		this.colorForInspectors = {};


		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "FACS") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				var freeFormTableController = new FreeFormTableController(sample, isEnabled);
				freeFormTableController.init($("#" + containerId));
			}
//			if(sampleTypeCode === "SYSTEM_EXPERIMENT") {
//				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
//				var dilutionWidgetController = new DilutionTableController(sample, isEnabled);
//				dilutionWidgetController.init($("#" + containerId));
//			}
		}
}
});