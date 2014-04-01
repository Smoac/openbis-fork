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
				
				var typeToAdd = {
						"LABEL" : sampleTypeCode,
						"TYPE": sampleTypeCode,
						"MIN_COUNT" : 0
				};
				
				_this.addOneSlot(typeToAdd);
				Util.unblockUI();
			}
		});
	}
	
	this._getButton = function(id, sampleTypeHint) {
		var _this = this;
		var tableId = id + "-table";
		var sampleId = id + "-sample";
		var $component = $("<div>", {"id" : id , "class" : "control-group", "sample-type-code" : sampleTypeHint["TYPE"], "sample-min-count" : sampleTypeHint["MIN_COUNT"] } );
		$component.css({"border-radius" : "10px", "padding" : "10px"});
		
		var requiredText = "";
		if(sampleTypeHint["MIN_COUNT"] > 0) {
			requiredText = " (Required at least " + sampleTypeHint["MIN_COUNT"] + ")";
		}
		
		var labelText = sampleTypeHint["LABEL"] + requiredText + ":";
		if(sampleTypeHint["LABEL"] === null) {
			labelText = "";
		}
		var $label = $("<label>", { "class" : "control-label" }).text(labelText);	
		var $controls = $("<div>", { "class" : "controls"});
			
			var $buttonTextField = $("<a>", {"class" : "btn", "type" : "button", "id" : sampleId});
			$buttonTextField.css({
				"max-width" : "90%",
				"text-align" : "left"
			});
			
			$buttonTextField.append("Select");
			$controls.append($buttonTextField);
			$controls.append(" ");
			
			var $buttonPlusOne = $("<a>", {"class" : "btn" });
			$buttonPlusOne.append($("<i>", { "class" : "icon-plus-sign"}));
			$controls.append($buttonPlusOne);
			$controls.append(" ");
			
			var $buttonDelete = $("<a>", {"class" : "btn" });
			$buttonDelete.append($("<i>", { "class" : "icon-minus-sign"}));
			$controls.append($buttonDelete);
			
			if(this.isDisabled) {
				$buttonTextField.attr("disabled", "");
				$buttonPlusOne.attr("disabled", "");
				$buttonDelete.attr("disabled", "");
			} else {
				$buttonTextField.click(function(elem) {
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
				
				$buttonPlusOne.click(function(elem) {
					_this.addOneSlot(sampleTypeHint);
				});
				$buttonDelete.click(function(elem) {
					_this.removeSample(sampleId);
				});
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
		} else {
			var _this = this;
			var onClick = function(elem) {
				_this._addAny(id, tableId, null);
			};
			$buttonPlus.click(onClick);
		}
		
		$component.append($controls);
		$component.append($("<div>", { "id" : tableId}));
		return $component;
	}
	
	this.repaint = function() {
		$('#'+this.containerId).empty();
		
		//Create Component
		var $component = $("<fieldset>");
		$component.append($("<legend>").text(this.title))
		$component.append(this._getPlus());
		$('#'+this.containerId).append($component);
		
		//Add predefined slots
		for(var i = 0; i < this.sampleTypeHints.length; i++) {
			this.addOneSlot(sampleTypeHints[i]);
		}
		
		//Add sample links to edit
		for(var i = 0; i < this.samplesToEdit.length; i++) {
			this.addSample(this.samplesToEdit[i]);
		}
	}
	
	this.addOneSlot = function(sampleTypeHint) {
		
		//Find latest slot from that type
		var containerId = null;
		for(var i = 0; i < this._lastIndex; i++) {
			var containerIdAux = this.containerId + "-" + i;
			var freePredefinedTypeCodeAux = $("#" + containerIdAux).attr("sample-type-code");
			if(sampleTypeHint["TYPE"] === freePredefinedTypeCodeAux) {
				containerId = containerIdAux;
			}
		}
		
		//If the slot exists, empty label
		if(containerId) {
			sampleTypeHint["LABEL"] = null;
		}
		
		//Create the new slot
		var $newSlot = this._getButton(this.containerId + "-" + this._lastIndex, sampleTypeHint);
		this._lastIndex++;
		
		if(containerId) { //Insert after that slot
			$("#" + containerId).after($newSlot);
		} else { //Insert before plus
			$("#" + this.containerId + "-plus-button").before($newSlot);
		}
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
			if(!freePredefinedSampleId) {
				for(var i = 0; i < this._lastIndex; i++) {
					var predefinedSampleId = this.containerId + "-" + i + "-sample";
					if(!this.samples[predefinedSampleId]) {
						var containerId = this.containerId + "-" + i;
						var freePredefinedTypeCodeAux = $("#" + containerId).attr("sample-type-code");
						if("null" === freePredefinedTypeCodeAux) {
							freePredefinedSampleId = predefinedSampleId;
							break;
						}
					}
				}
			}
			
			//Create a new slot if nothing is found
			if(!freePredefinedSampleId) { //Create a new slot if not found
				var sampleId = this.containerId + "-" + this._lastIndex + "-sample";
				freePredefinedSampleId = sampleId;
				
				var typeToAdd = {
					"LABEL" : sampleToAdd.sampleTypeCode,
					"TYPE": sampleToAdd.sampleTypeCode,
					"MIN_COUNT" : 0
				};
				this.addOneSlot(typeToAdd);
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
					propertyToShow = sampleToAdd.properties[propertiesToShow[j].substr(1)];
				}
				var propertyToShowDisplayName = propertiesToShowDisplayNames[j];
				
				meaningfulInfo += "<b>" + propertyToShowDisplayName + ": </b>" + Util.getEmptyIfNull(propertyToShow) + " ";
			}
			var $input = $("#" +freePredefinedSampleId);
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
			$('#'+sampleId).append("Select");
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