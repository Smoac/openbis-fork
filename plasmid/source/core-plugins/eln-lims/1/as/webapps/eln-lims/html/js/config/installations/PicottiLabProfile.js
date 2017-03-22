
function PicottiLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(PicottiLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);

		this.storagesConfiguration = {
				"isEnabled" : true,
				"storageSpaceLowWarning" : 0.8, //Storage goes over 80%
				"boxSpaceLowWarning" : 0.8, //Box goes over 80%
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
					"BENCH" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.BOX_POSITION, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 1 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS80_TOPLOADER" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.BOX_POSITION, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 8, //Number of rows
						"COLUMN_NUM" : 9, //Number of columns
						"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS80_UPRIGHT" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 16, //Number of rows
						"COLUMN_NUM" : 12, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_SMALL_1" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_SMALL_2" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_SMALL_3" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_SMALL_4" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_SMALL_5" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_SMALL_6" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"MINUS20_BIG_1" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 14, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"FRIDGE_1" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"FRIDGE_2" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"FRIDGE_3" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"FRIDGE_4" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"FRIDGE_5" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"CHEMICAL_STORAGE" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 3, //Number of rows
						"COLUMN_NUM" : 4, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},	
					"FUMEHOOD" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					}															
				}
			};	
		
}
});
