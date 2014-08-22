function TestProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(TestProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];
		this.notShowTypes = ["ANTIBODY_PANEL"];
		this.isShowUnavailablePreviewOnSampleTable = false;
		this.inventorySpaces = ["BODENMILLER_LAB"];
		
		//For testing	
		this.sampleTypeDefinitionsExtension = {
				"SYSTEM_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Protein",
														"TYPE": "PROTEIN",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : [ {"TYPE" : "PHOSPHO", "MANDATORY" : true }
														                           ,{"TYPE" : "ISOTYPE", "MANDATORY" : false }]
													}
												],
					"SAMPLE_CHILDREN_HINT" : [
											                             	{
																				"LABEL" : "Protein",
																				"TYPE": "PROTEIN",
																				"MIN_COUNT" : 1,
																				"ANNOTATION_PROPERTIES" : [ {"TYPE" : "PHOSPHO", "MANDATORY" : true }
																				                           ,{"TYPE" : "ISOTYPE", "MANDATORY" : false }]
																			}
																		]
				}
		}
//		
//		this.typePropertiesForSmallTable = {
//				"SYSTEM_EXPERIMENT" : ["NAME"],
//				"PROTEIN" : ["PROTEIN_NAME"]
//		}
		
		this.typeGroups = {
			"ANTIBODIES" : {
				"TYPE" : "ANTIBODIES",
				"DISPLAY_NAME" : "Antibodies",
				"LIST" : ["PROTEIN", "CLONE", "LOT", "CONJUGATED_CLONE"]
			},
			"CHEMICALS" : {
				"TYPE" : "CHEMICALS",
				"DISPLAY_NAME" : "Chemicals",
				"LIST" : ["CHEMICALS"]
			},
			"CELL_LINES" : {
				"TYPE" : "CELL_LINES",
				"DISPLAY_NAME" : "Cell Lines",
				"LIST" : ["CELL_LINES"]
			},
			"ANTIBODY_PANEL" : {
				"TYPE" : "ANTIBODY_PANEL",
				"DISPLAY_NAME" : "Antibody panels",
				"LIST" : ["ANTIBODY_PANEL"]
			},	
			"TUMORS" : {
				"TYPE" : "TUMORS",
				"DISPLAY_NAME" : "Tumors",
				"LIST" : ["TUMOR", "REGIONS", "ALIQUOT"]
			},								
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [""] 
			}
		};
	
		//this.typePropertiesForTable = {
		//	"SYSTEM_EXPERIMENT" : ["NAME", "GOALS", "RESULT_INTERPRETATION"],
		//}
	
		this.colorForInspectors = {
			"PROTEIN" : "#CCFFCC",
			"CLONE" : "#E3E3E3",
			"CONJUGATED_CLONE" : "#ACE8FC",
			"LOT" : "#CCCC99",
		};
	
		this.storagesConfiguration = {
				"isEnabled" : true,
				/*
				 * Should be the same across all storages, if not correct behaviour is not guaranteed.
				*/
				"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Storage information", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Group 1", //Storage Group Name
					"NAME_PROPERTY" : "FREEZER_NAME", //Should be a Vocabulary.
					"ROW_PROPERTY" : "ROW", //Should be an integer.
					"COLUMN_PROPERTY" : "COLUMN",  //Should be an integer.
					"BOX_PROPERTY" : "BOX_NUMBER", //Should be text.
					"USER_PROPERTY" : "USER_PROPERTY" //Should be text.
				},
				{
					"STORAGE_PROPERTY_GROUP" : "Storage information 2", //Where the storage will be painted.
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
					"TESTFREEZER" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"USER_BENCH" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								}
				}
			};
	
		/*
		 * Used by Sample Form
		 */
		this.getSpaceForSampleType = function(type) {
			if(type === "PROTEIN") {
				return "BODENMILLER_LAB";
			} else if(type === "CLONE") {
				return "BODENMILLER_LAB";
			} else if(type === "CONJUGATED_CLONE") {
				return "BODENMILLER_LAB";
			} else if(type === "LOT") {
				return "BODENMILLER_LAB";
			} else if(type === "CHEMICALS") {
				return "BODENMILLER_LAB";
			} else if(type === "CELL_LINES") {
				return "BODENMILLER_LAB";
			} else {
				return null;
			}
		}
		
		this.getExperimentIdentifierForSample = function(type, code, properties) {
			if(type === "PROTEIN") {
				return "/BODENMILLER_LAB/ANTIBODIES/ANTIBODIES";
			} else if(type === "CLONE") {
				return "/BODENMILLER_LAB/ANTIBODIES/ANTIBODIES";
			} else if(type === "CONJUGATED_CLONE") {
				return "/BODENMILLER_LAB/ANTIBODIES/ANTIBODIES";
			} else if(type === "LOT") {
				return "/BODENMILLER_LAB/ANTIBODIES/ANTIBODIES";
			} else if(type === "CHEMICALS") {
				return "/BODENMILLER_LAB/CHEMICALS/CHEMICALS";
			} else if(type === "CELL_LINES") {
				return "/BODENMILLER_LAB/CELL_LINES/CELL_LINES";
			} else {
				return null;
			}
		}

		/*
		 * Used by Main Menu
		 */
		this.mainMenuContentExtra = function() {
			return "<center><h5><i class='icon-info-sign'></i> Please log in into your google account on the brower to see your laboratory calendar.</h5></center><br /><iframe src='https://www.google.com/calendar/embed?src=kcm620topcrg5677ikbn5epg0s%40group.calendar.google.com&ctz=Europe/Zurich' margin-left = '20' style='border: 50' width='800' height='600' frameborder='0' scrolling='no'></iframe>";
		}
		
		/*
		 * Used by Sample Form
		 */
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "SYSTEM_EXPERIMENT") {
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