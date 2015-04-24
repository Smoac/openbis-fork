
function CiaudoLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(CiaudoLabProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.inventorySpaces = ["ANTIBODIES", "CELLS", "ENZYMES", "EQUIPMENT", "LAB_MEETINGS", "PLASMIDS", "PRIMERS", "PROTOCOLS", "RESTRICTION_ENZYMES"];
		this.isShowUnavailablePreviewOnSampleTable = true;
		
		this.getSpaceForSampleType = function(type) {
			if(type === "ANTIBODIES") {
				return "ANTIBODIES";
			} else if(type === "PLASMIDS") {
				return "PLASMIDS";
			} else if(type === "STRAINS") {
				return "PLASMIDS";
			} else if(type === "PRIMERS") {
				return "PRIMERS";
			} else if(type === "CELLS") {
				return "CELLS";
			} else if(type === "ENZYMES") {
				return "ENZYMES";
			} else if(type === "PROTOCOLS") {
				return "PROTOCOLS";
			} else if(type === "LAB_MEETINGS") {
				return "LAB_MEETINGS";
			} else if(type === "EQUIPMENT") {
				return "EQUIPMENT";
			} else {
				return null;
			}
		}	
	

		this.getExperimentIdentifierForSample = function(type, code, properties) {
			if(type === "LAB_MEETINGS") {
				return "/LAB_MEETINGS/LAB_MEETINGS/LAB_MEETINGS";
			} else if(type === "PROTOCOLS") {
				return "/PROTOCOLS/PROTOCOLS/PROTOCOLS";
			} else if(type === "ANTIBODIES") {
				return "/ANTIBODIES/ANTIBODIES/ANTIBODIES";
			} else if(type === "CELLS") {
				return "/CELLS/CELLS/CELLS";
			} else if(type === "ENZYMES") {
				return "/ENZYMES/ENZYMES/ENZYMES";
			} else if(type === "EQUIPMENT") {
				return "/EQUIPMENT/EQUIPMENT/EQUIPMENT";
			} else if(type === "PLASMIDS") {
				return "/PLASMIDS/PLASMIDS/PLASMIDS";
			} else if(type === "STRAINS") {
				return "/PLASMIDS/PLASMIDS/STRAINS";
			} else if(type === "PRIMERS") {
				return "/PRIMERS/PRIMERS/PRIMERS";
			} else if(type === "RESTRICTION_ENZYMES") {
				return "/RESTRICTION_ENZYMES/RESTRICTION_ENZYMES/RESTRICTION_ENZYMES";
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
					"MINUS80" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"MINUS20" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"FRIDGE" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"RT" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								}
				}
			};
	


		
		//The properties you want to appear on the tables, if you don�t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
}
});