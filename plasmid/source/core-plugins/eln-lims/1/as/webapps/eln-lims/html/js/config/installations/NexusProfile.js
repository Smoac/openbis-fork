
function NexusProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(NexusProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.inventorySpaces = ["LIBRARIES"];
		
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
					"MY_FRIDGE-1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 4, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"MY_FRIDGE-2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 5, //Number of rows
									"COLUMN_NUM" : 4, //Number of columns
									"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								}
				}
		};
		
		this.sampleTypeDefinitionsExtension = {}
}
});
