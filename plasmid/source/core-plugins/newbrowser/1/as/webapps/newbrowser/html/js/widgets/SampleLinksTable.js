/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Creates an instance of SampleLinksTable.
 *
 * This component is used to render the parents of a sample in a friendly manner.
 *
 * @constructor
 * @this {SampleLinksTable}
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 * @param {boolean} isDisabled Disables the component.
 */
function SampleLinksTable(containerId, profile, isDisabled) {
	this.containerId = containerId;
	this.profile = profile;
	this.samples = new Array();
	
	this.repaint = function() {
		//1. Build Table
		var table = "<table class='table'>";
		for(var i = 0; i < this.samples.length; i++) {
			var sampleToPrint = this.samples[i];
			var rowId = this.containerId + "_" + sampleToPrint.code;
			
			var propertiesToShow = this.profile.typePropertiesForTable[sampleToPrint.sampleTypeCode];
			if(propertiesToShow === null || propertiesToShow === undefined) {
				propertiesToShow = this.profile.getAllPropertiCodesForTypeCode(sampleToPrint.sampleTypeCode);
			}
			var propertiesToShowDisplayNames = this.profile.getPropertiesDisplayNamesForTypeCode(sampleToPrint.sampleTypeCode, propertiesToShow);
			
			table += "<tr>";
			table += "<td><span style='font-weight:bold;''>" + sampleToPrint.code + "</span></td>";
			table += "<td><span style='font-weight:bold;''>" + this.profile.getTypeForTypeCode(sampleToPrint.sampleTypeCode).description + "</span></td>";
			for(var j = 0; j < propertiesToShow.length; j++) {
				var propertyToShow = sampleToPrint.properties[propertiesToShow[j]];
				if(!propertyToShow && propertiesToShow[j].charAt(0) === '$') {
					propertyToShow = sampleToPrint.properties[propertiesToShow[j].substr(1)];
				}
				var propertyToShowDisplayName = propertiesToShowDisplayNames[j];
				table += "<td><span style='font-weight:bold;''>" + propertyToShowDisplayName + ":</span> <span>" + Util.getEmptyIfNull(propertyToShow) + "<span></td>";
			}
			if(!isDisabled) {
				table += "<td><button id='" + rowId + "' class='btn' type='button'><i class='icon-minus'></i></button></td>";
			}
			
			table += "</tr>";
		}
		table += "</table>";
		
		//2. Add Table to DOM
		document.getElementById(this.containerId).innerHTML = table;
		
		if(!isDisabled) {
			//3. Add Button Events
			var localReference = this;
			for(var i = 0; i < this.samples.length; i++) {
				var selectedSample = this.samples[i];
				var rowId = this.containerId + "_" + selectedSample.code;
			
				document.getElementById(rowId).onclick = function() {
					localReference.removeSample(selectedSample);
				};
			}
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