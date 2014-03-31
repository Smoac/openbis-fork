/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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
function SampleLinksWidget(containerId, profile, serverFacade, title, sampleTypeHints, isDisabled, samplesToEdit) {
	this.containerId = containerId;
	this.profile = profile;
	this.serverFacade = serverFacade;
	this.title = title;
	this.sampleTypeHints = sampleTypeHints;
	this.isDisabled = isDisabled;
	this.samplesToEdit = (samplesToEdit)?samplesToEdit:new Array(); //Only used to populate the widget
	this.samples = {};
	
	this._lastUsedId = null;
	this._lastIndex = 0;
	
	this._addAny = function(id, tableId, sampleId) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var component = "<select id='sampleTypeSelector' required>";
		component += "<option disabled=\"disabled\" selected></option>";
		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
			var label = Util.getEmptyIfNull(sampleType.description);
			if(label === "") {
				label = sampleType.code;
			}
			
			component += "<option value='" + sampleType.code + "'>" + label + "</option>";
		}
		component += "</select>";
		
		Util.blockUI("Select type: <br><br>" + component + "<br> or <a class='btn' id='sampleTypeSelectorCancel'>Cancel</a>");
		
		$("#sampleTypeSelectorCancel").on("click", function(event) { 
			Util.unblockUI();
		});
		
		var _this = this;
		$("#sampleTypeSelector").on("change", function(event) {
			var sampleTypeCode = $("#sampleTypeSelector")[0].value;
			var sampleType = _this.profile.getTypeForTypeCode(sampleTypeCode);
			
			if(sampleType !== null) {
				if(_this._lastUsedId) {
					$('#'+_this._lastUsedId + "-table").empty();
					$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
				}
				var onClick = function(sample) {
					$('#'+_this._lastUsedId + "-table").empty();
					$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
					_this.removeSample(sampleId);
					_this.addSample(sample);
					$("#" + id).css({"background-color" : "#FFFFFF" });
				}
				$("#" + id).css({"border-radius" : "10px", "padding" : "10px", "background-color" : "#EEEEEE" });
				var	sampleTable = new SampleTable(_this.serverFacade,tableId,_this.profile, sampleTypeCode, false, false, onClick, false, true);
				sampleTable.init();
				_this._lastUsedId = id;
				Util.unblockUI();
			}
		});
	}
	
	this._getButton = function(id, sampleTypeHint) {
		var _this = this;
		var tableId = id + "-table";
		var sampleId = id + "-sample";
		var labelId = id + "-label";
		var $component = $("<div>", {"id" : id , "class" : "control-group", "sample-type-code" : sampleTypeHint["TYPE"]} );
		$component.css({"border-radius" : "10px", "padding" : "10px"});
		var $label = $("<label>", { "class" : "control-label", "id" : labelId}).text(sampleTypeHint["LABEL"] + ":");	
		var $controls = $("<div>", { "class" : "controls"});
			
			var $textField = $("<a>", {"class" : "btn", "type" : "button", "id" : sampleId, "disabled" : ""});
			$textField.css({
				"max-width" : "90%",
				"text-align" : "left"
			});
			if(sampleTypeHint["REQUIRED"]) {
				$textField.attr("required", "");
			}
			$controls.append($textField);
			$controls.append(" ");
			var $buttonSelect = $("<a>", {"class" : "btn" });
			$buttonSelect.append("Select");
			$controls.append($buttonSelect);
			$controls.append(" ");
			var $buttonDelete = $("<a>", {"class" : "btn" });
			$buttonDelete.append("Delete");
			$controls.append($buttonDelete);
			
			if(this.isDisabled) {
				$buttonSelect.attr("disabled", "");
				$buttonDelete.attr("disabled", "");
			}
			
			$buttonSelect.click(function(elem) {
				var $buttonClicked = $(elem);
				var sampleTypeCode = sampleTypeHint["TYPE"];
				var sampleType = _this.profile.getTypeForTypeCode(sampleTypeCode);
				
				if(sampleType !== null) {
					if(_this._lastUsedId) {
						$('#'+_this._lastUsedId + "-table").empty();
						$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
					}
					var onClick = function(sample) {
						$('#'+_this._lastUsedId + "-table").empty();
						$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
						_this.removeSample(sampleId);
						_this.addSample(sample);
						$("#" + id).css({"background-color" : "#FFFFFF" });
					}
					$("#" + id).css({"border-radius" : "10px", "padding" : "10px", "background-color" : "#EEEEEE" });
					var	sampleTable = new SampleTable(_this.serverFacade,tableId,_this.profile, sampleTypeCode, false, false, onClick, false, true);
					sampleTable.init();
					_this._lastUsedId = id;
				} else {
					_this._addAny(id, tableId, sampleId);
				}
			});
			
			$buttonDelete.click(function(elem) {
				_this.removeSample(sampleId);
			});
			
			if(sampleTypeHint["REQUIRED"]) {
				$controls.append(" (Required)");
			}
			
			$component.append($label);
			$component.append($controls);
			
			$component.append($("<div>", { "id" : tableId}));
		return $component;
	}
	
	this._getPlus = function() {
		var id = this.containerId + "-plus-button";
		var tableId = id + "-table";
		var $component = $("<div>", { "id" : id, "class" : "control-group"} );
		var $controls = $("<div>", { "class" : "controls"});
		
		var $buttonPlus = $("<a>", {"class" : "btn"});
		$buttonPlus.append($("<i>", { "class" : "icon-plus-sign"}));
		$controls.append($buttonPlus);
			
		if(this.isDisabled) {
			$buttonPlus.attr("disabled", "");
		}
		
		var _this = this;
		
		var onClick = function(elem) {
			_this._addAny(id, tableId, null);
		};
		
		$buttonPlus.click(onClick);
		$component.append($controls);
		$component.append($("<div>", { "id" : tableId}));
		return $component;
	}
	
	this.repaint = function() {
		$('#'+this.containerId).empty();
		
		var $component = $("<fieldset>");
		$component.append($("<legend>").text(this.title))
		
		for(var i = 0; i < this.sampleTypeHints.length; i++) {
			$component.append(this._getButton(this.containerId + "-" + this._lastIndex, sampleTypeHints[i]));
			this._lastIndex++;
		}
		
		$component.append(this._getPlus());
		
		$('#'+this.containerId).append($component);
		
		//Add sample links to edit
		for(var i = 0; i < this.samplesToEdit.length; i++) {
			this.addSample(this.samplesToEdit[i]);
		}
	}
	
	this.addOneSlot = function() {
		var id = this.containerId + "-plus-button";
		var addAny = {
				"LABEL" : "... ",
				"TYPE": "null",
				"REQUIRED" : false
		};
		
		var $plusButton = $("#" + id);
		$plusButton.remove();
		$("#" + this.containerId).append(this._getButton(this.containerId + "-" + this._lastIndex, addAny));
		this._lastIndex++;
		$("#" + this.containerId).append(this._getPlus());
	}
	
	this.addSample = function(sampleToAdd) {
		var found = false;
		for(sampleObjKey in this.samples) {
			var sampleObj = this.samples[sampleObjKey];
			if(sampleObj && sampleObj.identifier === sampleToAdd.identifier) {
				found = true;
			}
		}
		
		if(!found) {
			//Check for a predefined slot that is free
			var freePredefinedSampleId = null;
			for(var i = 0; i < this._lastIndex; i++) {
				var predefinedSampleId = this.containerId + "-" + i + "-sample";
				if(!this.samples[predefinedSampleId]) {
					var containerId = this.containerId + "-" + i;
					var freePredefinedTypeCodeAux = $("#" + containerId).attr("sample-type-code");
					if(sampleToAdd.sampleTypeCode === freePredefinedTypeCodeAux) {
						freePredefinedSampleId = predefinedSampleId;
						break;
					}
				}
			}
			
			//Check for a non predefined slot that is free
			var labelId = null;
			if(!freePredefinedSampleId) {
				for(var i = 0; i < this._lastIndex; i++) {
					var predefinedSampleId = this.containerId + "-" + i + "-sample";
					if(!this.samples[predefinedSampleId]) {
						var containerId = this.containerId + "-" + i;
						var freePredefinedTypeCodeAux = $("#" + containerId).attr("sample-type-code");
						if("null" === freePredefinedTypeCodeAux) {
							labelId = this.containerId + "-" + i + "-label";
							freePredefinedSampleId = predefinedSampleId;
							break;
						}
					}
				}
			}
			
			//Create a new slot if nothing is found
			if(!freePredefinedSampleId) { //Create a new slot if not found
				var sampleId = this.containerId + "-" + this._lastIndex + "-sample";
				labelId = this.containerId + "-" + this._lastIndex + "-label";
				freePredefinedSampleId = sampleId;
				this.addOneSlot();
			}
			
			//Finally, add the sample
			this.samples[freePredefinedSampleId] = sampleToAdd;
			
			//Show meaningful information
			var propertiesToShow = this.profile.typePropertiesForTable[sampleToAdd.sampleTypeCode];
			if(propertiesToShow === null || propertiesToShow === undefined) {
				propertiesToShow = this.profile.getAllPropertiCodesForTypeCode(sampleToAdd.sampleTypeCode);
			}
			var propertiesToShowDisplayNames = this.profile.getPropertiesDisplayNamesForTypeCode(sampleToAdd.sampleTypeCode, propertiesToShow);
			
			var meaningfulInfo = "<b>Code: </b>" + sampleToAdd.code + " ";
			
			var max3Length = (propertiesToShow.length > 3)?3:propertiesToShow.length;
			for(var j = 0; j < max3Length; j++) {
				var propertyToShow = sampleToAdd.properties[propertiesToShow[j]];
				if(!propertyToShow && propertiesToShow[j].charAt(0) === '$') {
					propertyToShow = sampleToPrint.properties[propertiesToShow[j].substr(1)];
				}
				var propertyToShowDisplayName = propertiesToShowDisplayNames[j];
				
				meaningfulInfo += "<b>" + propertyToShowDisplayName + ": </b>" + Util.getEmptyIfNull(propertyToShow) + " ";
			}
			var $input = $("#" +freePredefinedSampleId);
			if(labelId) {
				$("#"+labelId).text(sampleToAdd.sampleTypeCode +":");
			}
			if(meaningfulInfo.length > 200) {
				meaningfulInfo = meaningfulInfo.substring(0, 200) + "...";
			}
			$input.empty();
			$input.append(meaningfulInfo);
		} else {
			Util.showError("Item Already selected, choose another.");
		}
	}

	this.removeSample = function(sampleId) {
		if(this.samples[sampleId]) {
			$('#'+sampleId).empty();
			this.samples[sampleId] = null;
		}
	}
	
	this.getSamplesIdentifiers = function() {
		var sampleIdentifiers = new Array();
		for(sampleObjKey in this.samples) {
			var sampleObj = this.samples[sampleObjKey];
			if(sampleObj !== null) {
				sampleIdentifiers.push(sampleObj.identifier);
			}
		}
		return sampleIdentifiers;
	}
}