function BodenmillerLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(BodenmillerLabProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];
		this.notShowTypes = ["ANTIBODY_PANEL", "SYSTEM_EXPERIMENT"];
		this.isShowUnavailablePreviewOnSampleTable = false;
		this.inventorySpaces = ["BODENMILLER_LAB"];
		
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
	
		this.colorForInspectors = {
			"PROTEIN" : "#CCFFCC",
			"CLONE" : "#E3E3E3",
			"CONJUGATED_CLONE" : "#ACE8FC",
			"LOT" : "#CCCC99",
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
			if(sampleTypeCode === "ANTIBODY_PANEL") {
				var isEnabled = mainController.currentView.mode !== SampleFormMode.VIEW;
				var dilutionWidget = new DilutionWidget(containerId, this.serverFacade, isEnabled);
				dilutionWidget.init();
			}
		}
	}
});