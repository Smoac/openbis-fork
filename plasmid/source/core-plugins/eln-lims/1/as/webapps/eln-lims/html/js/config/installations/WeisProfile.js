
function WeisProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(WeisProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		
		this.hideCodes = true;
		
		
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
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 1 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"DEFAULT_STORAGE" : { //Freezer name given by the NAME_PROPERTY
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					}
				}
			};
	
		/* New Sample definition tests*/
		this.sampleTypeDefinitionsExtension = {




				"GENERAL_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Restriction enzyme",
														"TYPE": "RESTRICTION_ENZYME",
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

			"STRAIN" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Yeast strain",
														"TYPE": "STRAIN",
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
										],
					"SAMPLE_PARENTS_ANNOTATIONS_COPY" : { "STRAIN" : ["PLASMID"] }
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
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Restriction enzyme",
														"TYPE": "RESTRICTION_ENZYME",
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
														"LABEL" : "Yeast strain",
														"TYPE": "STRAIN",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "General protocol",
														"TYPE": "GENERAL_PROTOCOL",
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
}
});