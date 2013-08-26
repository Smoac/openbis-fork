/*
 * This component is used by the new/edit sample form to show the links from different types of samples.
 *
 * sampleTableId : Id of the DIV that will hold this table.
 * profile : global configuraiton.
 */
function SampleLinksTable(sampleTableId, profile) {
	this.sampleTableId = sampleTableId;
	this.profile = profile;
	this.samples = new Array();
	
	this.repaint = function() {
		//1. Build Table
		var table = "<table class='table'>";
		for(var i = 0; i < this.samples.length; i++) {
			var sampleToPrint = this.samples[i];
			var rowId = this.sampleTableId + "_" + sampleToPrint.code;
			
			var propertiesToShow = this.profile.typePropertiesForTable[sampleToPrint.sampleTypeCode];
			if(propertiesToShow === null || propertiesToShow === undefined) {
				propertiesToShow = this.profile.getAllPropertiCodesForTypeCode(sampleToPrint.sampleTypeCode);
			}
			var propertiesToShowDisplayNames = this.profile.getPropertiesDisplayNamesForTypeCode(sampleToPrint.sampleTypeCode, propertiesToShow);
			
			table += "<tr>";
			table += "<td><span style='font-weight:bold;''>" + sampleToPrint.code + "</span></td>";
			table += "<td><span style='font-weight:bold;''>" + this.profile.getTypeForTypeCode(sampleToPrint.sampleTypeCode).description + "</span></td>";
			for(var j = 0; j < propertiesToShow.length; j++) {
				var propertyToShow = Util.getEmptyIfNull(sampleToPrint.properties[propertiesToShow[j]]);
				var propertyToShowDisplayName = propertiesToShowDisplayNames[j];
				table += "<td><span style='font-weight:bold;''>" + propertyToShowDisplayName + ":</span> <span>" + propertyToShow + "<span></td>";
			}
			table += "<td><button id='" + rowId + "' class='btn' type='button'><i class='icon-minus'></i></button></td>";
			table += "</tr>";
		}
		table += "</table>";
		
		//2. Add Table to DOM
		document.getElementById(this.sampleTableId).innerHTML = table;
		
		//3. Add Button Events
		var localReference = this;
		for(var i = 0; i < this.samples.length; i++) {
			var selectedSample = this.samples[i];
			var rowId = this.sampleTableId + "_" + selectedSample.code;
			
			document.getElementById(rowId).onclick = function() {
				localReference.removeSample(selectedSample);
			};
		}
	}
	
	this.addSample = function(sampleToAdd) {
		var found = false;
		for(var i = 0; i < this.samples.length; i++) {
			if(this.samples[i].code === sampleToAdd.code) {
				found = true;
			}
		}
		if(!found) {
			this.samples.push(sampleToAdd);
			this.repaint();
		}
	}
	
	this.removeSample = function(sampleToRemove) {
		var foundIndex = -1;
		for(var i = 0; i < this.samples.length; i++) {
			if(this.samples[i].code === sampleToRemove.code) {
				foundIndex = i;
			}
		}
		if(foundIndex != -1) {
			this.samples.splice(foundIndex);
			this.repaint();
		}
	}
	
	this.getSamplesIdentifiers = function() {
		var sampleIdentifiers = new Array();
		for(var i = 0; i < this.samples.length; i++) {
			sampleIdentifiers.push(this.samples[i].identifier);
		}
		return sampleIdentifiers;
	}
}