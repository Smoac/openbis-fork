
function BSAProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(BSAProfile.prototype, DefaultProfile.prototype, {
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
					"USER_PROPERTY" : 		"STORAGE_USER", //Should be text.
					"POSITION_PROPERTY" : 	"STORAGE_POSITION" //Should be text.
				}],
				/*
				 * Storages map, can hold configurations for several storages.
				*/
				"STORAGE_CONFIGS": {
					"BENCH" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"DEFAULT_STORAGE" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 999999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					}
				}
			};
	
		/* New Sample definition tests*/
		this.sampleTypeDefinitionsExtension = {
				"EXPERIMENTAL_STEP" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	
					                             	{
														"LABEL" : "Cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMISTRY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Cloning insert",
														"TYPE": "CLONING_INSERT",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Cloning vector",
														"TYPE": "CLONING_VECTOR",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Linearised vector",
														"TYPE": "LINEARIZED_VECTOR",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
													{
														"LABEL" : "Vector",
														"TYPE": "VECTOR",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Molecular biology",
														"TYPE": "MOLECULAR_BIOLOGY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Primer",
														"TYPE": "PRIMER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Protocol",
														"TYPE": "PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}
	
					                             																														
												]
				}
		
		} 
		
		
}
});