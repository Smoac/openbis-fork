
function MateescuLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(MateescuLabProfile.prototype, StandardProfile.prototype, {
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
				"NAME_PROPERTY" : "STORAGE_NAMES", //Should be a Vocabulary.
				"ROW_PROPERTY" : "STORAGE_ROW", //Should be an integer.
				"COLUMN_PROPERTY" : "STORAGE_COLUMN", //Should be an integer.
				"BOX_PROPERTY" : "STORAGE_BOX_NAME", //Should be text.
				"BOX_SIZE_PROPERTY" : "STORAGE_BOX_SIZE", //Should be Vocabulary.
				"USER_PROPERTY" : "STORAGE_USER", //Should be text.
				"POSITION_PROPERTY" : "STORAGE_POSITION" //Should be text.
				}],
				/*
				 * Storages map, can hold configurations for several storages.
				*/
				"STORAGE_CONFIGS": {
					"N2_TANK_A" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 4, //Number of columns
						"BOX_NUM" : 1 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"N2_TANK_B" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 4, //Number of columns
						"BOX_NUM" : 1 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS80_1" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 6, //Number of rows
						"COLUMN_NUM" : 6, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS80_2" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 5, //Number of rows
						"COLUMN_NUM" : 5, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS20_MAIN" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 6, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 18 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS20_SMALL" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 4, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 10 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"MINUS20_CELL_CULTURE" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 3, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 10 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"FRIDGE_LAB" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"FRIDGE_CELL_CULTURE" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"COLD_ROOM" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"CABINET_LAB_RT" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"SHELF_LAB_RT" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"LAB_BENCH1 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"LAB_BENCH2 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"LAB_BENCH3 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"LAB_BENCH4 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"LAB_BENCH5 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"CELL_CULTURE_BENCH1 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"CELL_CULTURE_BENCH2 : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.RACK,
						"ROW_NUM" : 10, //Number of rows
						"COLUMN_NUM" : 10, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					}
					
					
					
				}
			};
			
}
});
