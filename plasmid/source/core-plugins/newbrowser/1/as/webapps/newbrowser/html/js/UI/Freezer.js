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
 
function Freezer(containerId, profile, sampleTypeCode, sample, isDisabled) {
	this.containerId = containerId;
	this.profile = profile;
	this.sampleType = profile.getTypeForTypeCode(sampleTypeCode);
	this.isFreezerAvailable = false;
	this.sample = sample; // Needed for edit mode
	this.isDisabled = isDisabled; //Needed for view mode
	
	//
	// Private utility methods used to check if a freezer is properly configured for certain entity type.
	//
	this._isFreezerForType = function() {
		for(var freezerProperty in this.profile.freezersConfiguration["FREEZER_PROPERTIES"]) {
			var freezerPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"][freezerProperty];
			if(!(this._getPropertyFromType(freezerPropertyCode) !== null)) {
				return false;
			}
		}
		return true;
	}
	
	this._getPropertyFromType = function(propertyTypeCode) {
		for(var i = 0; i < this.sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = this.sampleType.propertyTypeGroups[i];
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				if(propertyType.code === propertyTypeCode) {
					return propertyType;
				}
			}
		}
		return null;
	}
	
	//
	// Private Methods to create a form
	//
	this._getDropDownField = function(code, vocabularyTerms, isRequired, isHidden) {
		var $component = $("<select>");
		$component.attr('id', code);
		
		if(isRequired) {
			$component.attr('required', '');
		}
		
		if(isHidden) {
			$component.attr('class', 'hiddenInput');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < vocabularyTerms.length; i++) {
			$component.append($("<option>").attr('value',vocabularyTerms[i].code).text(vocabularyTerms[i].label));
		}
		
		return $component;
	}
	
	this._getInputField = function(type, step, id, alt, isRequired, isHidden) {
		var $component = $("<input>");
		$component.attr('type', type);
		$component.attr('id', id);
		$component.attr('alt', alt);
		
		if(step) {
			$component.attr('step', step);
		}
		
		if(isRequired) {
			$component.attr('required', '');
		}
		
		if(isHidden) {
			$component.attr('class', 'hiddenInput');
		}
		
		return $component;
	}
	
	this._getComponent = function(propertyType, isHidden) {
		var $propertyTypeComponent = null;
		
		if (propertyType.dataType === "INTEGER") {
			$propertyTypeComponent = this._getInputField("number", "1", propertyType.code, propertyType.description, propertyType.mandatory, isHidden);
		} else if (propertyType.dataType === "VARCHAR") {
			$propertyTypeComponent = this._getInputField("text", null, propertyType.code, propertyType.description, propertyType.mandatory, isHidden);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			var vocabulary = null;
			if(isNaN(propertyType.vocabulary)) {
				vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
			} else {
				vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
			}
			$propertyTypeComponent = this._getDropDownField(propertyType.code, vocabulary.terms, propertyType.mandatory, isHidden);
		}
		
		return $propertyTypeComponent;
	}
	
	//
	// Private methods to interact and update the form
	//
	this._getSelectedValue = function(propertyTypeCode, returnVocabularyAsInteger) {
		var propertyType = this._getPropertyFromType(propertyTypeCode);
		
		var lastValue = null;
		if($("#"+propertyTypeCode).length === 1) {
			if (returnVocabularyAsInteger && propertyType.dataType === "CONTROLLEDVOCABULARY") {
				lastValue = $("#"+propertyTypeCode)[0].selectedIndex;
			} else {
				lastValue = $("#"+propertyTypeCode)[0].value;
			}
			
		} else if(this.sample && this.sample.properties[propertyTypeCode]) {
			if (returnVocabularyAsInteger && propertyType.dataType === "CONTROLLEDVOCABULARY") {
				var vocabulary = null;
				if(isNaN(propertyType.vocabulary)) {
					vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
				} else {
					vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
				}
				
				var selectedTerm = this.sample.properties[propertyTypeCode];
				for(var i = 0; i < vocabulary.terms.length; i++) {
					if(vocabulary.terms[i].code === selectedTerm) {
						lastValue = i + 1;
					}
				}
			} else {
				lastValue = this.sample.properties[propertyTypeCode];
			}
			
		}
		return lastValue;
	}
	
	this._setSelectedValue = function(propertyTypeCode, freezerIndex) {
		var propertyType = this._getPropertyFromType(propertyTypeCode);
		if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			if(freezerIndex === null) {
				$('#' + propertyTypeCode).prop('selectedIndex', 0);
			} else {
				$('#' + propertyTypeCode).prop('selectedIndex', freezerIndex);
			}
		} else {
			if(freezerIndex === null) {
				$('#' + propertyTypeCode).val("");
			} else {
				$('#' + propertyTypeCode).val(freezerIndex);
			}
		}
	}
		
	//
	// Public utility methods used by the sample form to know what is responsability of the freezer. To behave correctly, init should be executed first.
	//
	this.isPropertyGroupFromFreezer = function(propertyGroupName) {
		if(!this.isFreezerAvailable) { return false; }
		
		if(this.profile.freezersConfiguration["FREEZER_PROPERTY_GROUP"] === propertyGroupName) {
			return true;
		}
		return false;
	}
	
	this.isPropertyFromFreezer = function(samplePropertyTypeCode) {
		if(!this.isFreezerAvailable) { return false; }
		
		var freezerProperties = this.profile.freezersConfiguration["FREEZER_PROPERTIES"];
		for(var propertyType in freezerProperties) {
			var freezerPropertyTypeCode = freezerProperties[propertyType];
			if(freezerPropertyTypeCode === samplePropertyTypeCode) {
				return true;
			}
		}
		return false;
	}
	
	//
	// Main methods to interact with the freezer
	//
	this.init = function() {
		this.isFreezerAvailable = this._isFreezerForType();
	}
	
	this.repaint = function() {
		if(!this.isFreezerAvailable) { return; }
		
		var freezerNamePropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["NAME_PROPERTY"];
		var selectedFreezer = this._getSelectedValue(freezerNamePropertyCode, false);
		
		var freezerRowPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["ROW_PROPERTY"];
		var selectedRow = this._getSelectedValue(freezerRowPropertyCode, true);
		
		var freezerColPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["COLUMN_PROPERTY"];
		var selectedCol = this._getSelectedValue(freezerColPropertyCode, true);
		
		var freezerBoxPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["BOX_PROPERTY"];
		var selectedBox = this._getSelectedValue(freezerBoxPropertyCode, false);
		
		var localReference = this;
		var $container = $("#"+this.containerId);
		$container.empty();
		
		//
		// 1. Build a drop down to select the freezer
		//
		
		//Drop Down
		//Create and set the field
		var $freezerNameDropDown = this._getComponent(this._getPropertyFromType(freezerNamePropertyCode), false);
		$freezerNameDropDown.val(selectedFreezer);
		
		$freezerNameDropDown.change(
			function() {
				localReference._setSelectedValue(freezerRowPropertyCode, null);
				localReference._setSelectedValue(freezerColPropertyCode, null);
				localReference.repaint();
			}
		);
		
		//Component with Dropdown
		$container
			.append($("<div>")
						.append($("<i>", { class: "icon-info-sign" }))
						.append(" 1. Select a freezer: ")
						.append($freezerNameDropDown)
			);
		
		//
		// 2. Build a table to select the row and column.
		//
		
		//Attach row and column hidden fields
		var $propertyTypeRowComponent = this._getComponent(this._getPropertyFromType(freezerRowPropertyCode), true);
		var $propertyTypeColComponent = this._getComponent(this._getPropertyFromType(freezerColPropertyCode), true);
		$container
			.append($propertyTypeRowComponent)
			.append($propertyTypeColComponent);
		
		//Generate virtual freezer representation after the freezer has been selected
		var freezerConfig = null;
		if(selectedFreezer !== null && selectedFreezer !== "") {
			freezerConfig = this.profile.freezersConfiguration["FREEZER_CONFIGS"][selectedFreezer];
			
			if(!freezerConfig) {
				Util.showError("Freezer configuration missing, the freezer can't be displayed.", function() { Util.unblockUI(); });
			}
		}
		
		var $virtualFreezer = null;
		if(freezerConfig) {
			$virtualFreezer = $("<table>");
			$virtualFreezer.attr('class', 'freezerTable');
			
			//Paint Columns on the header
			var $virtualFreezerRow = $("<tr>");
			for(var i = 0; i <= freezerConfig["COLUMN_NUM"]; i++) {
				if(i == 0) {
					$virtualFreezerRow.append($("<th>"));
				} else {
					$virtualFreezerRow.append($("<th>").append(i));
				}
			}
			$virtualFreezer.append($virtualFreezerRow);
			
			//Paint Rows			
			for(var i = 0; i <= freezerConfig["ROW_NUM"]; i++) {
				var $virtualFreezerRow = $("<tr>");
				for(var j = 0; j <= freezerConfig["COLUMN_NUM"]; j++) {
					if(j == 0) {
						$virtualFreezerRow.append($("<th>").append(i+1));
					} else {
						var $rack = $("<td>");
						var rackId = "rack_" + (i+1) + "_" + j;
						$rack.attr("id", rackId);
						$rack.attr("rowNum", i+1);
						$rack.attr("colNum", j);
						
						$rack.click(function() {
							if(localReference.isDisabled) { return; }
							
							//Clean the whole freezer
							for(var i = 0; i <= freezerConfig["ROW_NUM"]; i++) {
								for(var j = 0; j <= freezerConfig["COLUMN_NUM"]; j++) {
									var rackId = "#rack_" + (i+1) + "_" + j;
									var rackClass = $(rackId).attr("class");
									if(rackClass === 'freezerSelectedRack') {
										$(rackId).removeClass('freezerSelectedRack');
									}
								}
							}
							
							//Select current spot
							var thisClass = $(this).attr("class");
							$(this).addClass('freezerSelectedRack');
						
							//Set the hidden fields
							localReference._setSelectedValue(freezerRowPropertyCode, $(this).attr("rowNum"));
							localReference._setSelectedValue(freezerColPropertyCode, $(this).attr("colNum"));
							
							localReference.repaint();
						});
						
						
						
						$virtualFreezerRow.append($rack);
					}
				}
				$virtualFreezer.append($virtualFreezerRow);
			}
			
			if($virtualFreezer) {
				$container
					.append($("<div>")
								.append($("<i>", { class: "icon-info-sign" }))
								.append(" 2. Select a rack: ")
								.append($virtualFreezer)
					)
			
				if(selectedRow && selectedCol) {
					//Set the visual freezer
					var rackId = "#rack_" + selectedRow + "_" + selectedCol;
					$(rackId).addClass('freezerSelectedRack');
					//Set the hidden fields
					localReference._setSelectedValue(freezerRowPropertyCode, selectedRow);
					localReference._setSelectedValue(freezerColPropertyCode, selectedCol);
				}
			}
			
			//
			// 3. Input the box name
			//
			if($virtualFreezer && selectedRow && selectedCol) {
				//Create and set the field
				var $propertyTypeBoxComponent = this._getComponent(this._getPropertyFromType(freezerBoxPropertyCode), false);
				$propertyTypeBoxComponent.val(selectedBox);
				$container
					.append($("<br>"))
					.append($("<div>")
								.append($("<i>", { class: "icon-info-sign" }))
								.append(" 3. Input the box name or number: ")
								.append($propertyTypeBoxComponent)
					);
			}
			
			//
			// 4. Disable if needed
			//
			if(this.isDisabled) {
				$("#"+freezerNamePropertyCode.code).prop('disabled', true);
				$("#"+freezerRowPropertyCode.code).prop('disabled', true);
				$("#"+freezerColPropertyCode.code).prop('disabled', true);
				$("#"+freezerBoxPropertyCode.code).prop('disabled', true);
			}
		}
	}
}