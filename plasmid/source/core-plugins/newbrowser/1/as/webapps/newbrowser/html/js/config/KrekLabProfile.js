
function KrekLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(KrekLabProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.inventorySpaces = ["INVENTORY"];
		this.isShowUnavailablePreviewOnSampleTable = true;

		this.getSpaceForSampleType = function(type) {
			if(type === "ANTIBODY") {
				return "INVENTORY";
			} else if(type === "PLASMID") {
				return "INVENTORY";
			} else if(type === "INHIBITOR") {
				return "INVENTORY";
			} else if(type === "CELL_LINE") {
				return "INVENTORY";
			} else {
				return null;
			}
		}	
	

		this.getExperimentIdentifierForSample = function(type, code, properties) {
			if(type === "ANTIBODY") {
				return "/INVENTORY/SAMPLES/ANTIBODIES";
			} else if(type === "PLASMID") {
				return "/INVENTORY/SAMPLES/PLASMIDS";
			} else if(type === "INHIBITOR") {
				return "/INVENTORY/SAMPLES/INHIBITORS";
			} else if(type === "CELL_LINE") {
				return "/INVENTORY/SAMPLES/CELL_LINES";
			} else {
				return null;
			}
		}
	
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
				}],
				/*
				 * Storages map, can hold configurations for several storages.
				*/
				"STORAGE_CONFIGS": {
					"H20.1_KREK-1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"H20.2_KREK-2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
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

		
		//The properties you want to appear on the tables, if you don�t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
}
});