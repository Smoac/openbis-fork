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
 * Creates an instance of Freezer.
 *
 * @constructor
 * @this {Freezer}
 * @param {SearchFacade} searchFacade The facade used to access server side search functionality.
 * @param {String} containerId The Container where the Freezer DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 * @param {String} sampleTypeCode The code of the sample type, needed to know where to check if the properties are available.
 * @param {Sample} sample The sample where check the properties for VIEW and EDIT modes.
 * @param {boolean} isDisabled If the freezer should allow to be edited.
 */
function Freezer(searchFacade, containerId, profile, sampleTypeCode, sample, isDisabled) {
	this.searchFacade = searchFacade;
	this.containerId = containerId;
	this.profile = profile;
	this.sampleType = profile.getTypeForTypeCode(sampleTypeCode);
	this.isFreezerAvailable = false;
	this.sample = sample; // Needed for edit mode
	this.isDisabled = isDisabled; //Needed for view mode
	this.selectedFreezerCache = null;
	
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
	
	this._getInputField = function(type, step, id, alt, isRequired, isHidden, pattern) {
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
		
		if(pattern) {
			$component.attr('pattern', pattern);
		}
		
		if(isHidden) {
			$component.attr('class', 'hiddenInput');
		}
		
		return $component;
	}
	
	this._getComponent = function(propertyType, isHidden, pattern) {
		var $propertyTypeComponent = null;
		
		if (propertyType.dataType === "INTEGER") {
			$propertyTypeComponent = this._getInputField("number", "1", propertyType.code, propertyType.description, propertyType.mandatory, isHidden, pattern);
		} else if (propertyType.dataType === "VARCHAR") {
			$propertyTypeComponent = this._getInputField("text", null, propertyType.code, propertyType.description, propertyType.mandatory, isHidden, pattern);
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
	this._getSelectedValue = function(propertyTypeCode, returnVocabularyAsInteger, sample) {
		var propertyType = this._getPropertyFromType(propertyTypeCode);
		
		var lastValue = null;

		if((!sample && $("#"+propertyTypeCode).length === 1) || //If you don't specify the sample, you can only look to the form, if exists
			(this.sample && sample && this.sample.identifier === sample.identifier && $("#"+propertyTypeCode).length === 1)) { //If you t specify the sample, you can only look to the form if they are the same sample
			if (returnVocabularyAsInteger && propertyType.dataType === "CONTROLLEDVOCABULARY") {
				lastValue = $("#"+propertyTypeCode)[0].selectedIndex;
			} else {
				lastValue = $("#"+propertyTypeCode)[0].value;
			}
			
		} else if(sample && sample.properties[propertyTypeCode]) {
			if (returnVocabularyAsInteger && propertyType.dataType === "CONTROLLEDVOCABULARY") {
				var vocabulary = null;
				if(isNaN(propertyType.vocabulary)) {
					vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
				} else {
					vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
				}
				
				var selectedTerm = sample.properties[propertyTypeCode];
				for(var i = 0; i < vocabulary.terms.length; i++) {
					if(vocabulary.terms[i].code === selectedTerm) {
						lastValue = i + 1;
					}
				}
			} else {
				lastValue = sample.properties[propertyTypeCode];
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
		var selectedFreezer = this._getSelectedValue(freezerNamePropertyCode, false, this.sample);
		
		//Build freezer cache and paint it afterwards
		if(selectedFreezer) {
			var freezerRowPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["ROW_PROPERTY"];
			var freezerColPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["COLUMN_PROPERTY"];
			var freezerBoxPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["BOX_PROPERTY"];
			
			var propertyTypeCodes = [freezerNamePropertyCode, freezerRowPropertyCode, freezerColPropertyCode, freezerBoxPropertyCode];
			var propertyValues = [selectedFreezer, "?*", "?*", "?*"];
			
			var localReference = this;
			 
			var $container = $("#"+this.containerId);
			$container.children().hide();
			$container
				.append($("<div>")
							.append($("<i>", { class: "icon-info-sign" }))
							.append(" Loading... ")
				);
			
			this.searchFacade.searchWithProperties(propertyTypeCodes, propertyValues,
				function(samples) {
					var boxes = []; //Rows
					
					samples.forEach(
						function(element, index, array) {
							var boxCode = element.properties[freezerBoxPropertyCode];
							
								//Ad new box 
								var boxRow = localReference._getSelectedValue(freezerRowPropertyCode, true, element);
								var boxCol = localReference._getSelectedValue(freezerColPropertyCode, true, element);
								
								var boxesRow = boxes[boxRow];
								if(!boxesRow) {
									boxesRow = [];
									boxes[boxRow] = boxesRow;
								}
								
								var boxesCol = boxesRow[boxCol];
								if(!boxesCol) {
									boxesCol = {};
									boxesRow[boxCol] = boxesCol;
								}
								
								boxesCol[boxCode] = true;
							
						}
					);
					
					localReference.selectedFreezerCache = boxes;
					localReference._repaint();
				});
		} else {
			this._repaint();
		}
	}
	
	this._repaint = function() {
		if(!this.isFreezerAvailable) { return; }
		
		var freezerNamePropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["NAME_PROPERTY"];
		var selectedFreezer = this._getSelectedValue(freezerNamePropertyCode, false, this.sample);
		
		var freezerRowPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["ROW_PROPERTY"];
		var selectedRow = this._getSelectedValue(freezerRowPropertyCode, true, this.sample);
		
		var freezerColPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["COLUMN_PROPERTY"];
		var selectedCol = this._getSelectedValue(freezerColPropertyCode, true, this.sample);
		
		var freezerBoxPropertyCode = this.profile.freezersConfiguration["FREEZER_PROPERTIES"]["BOX_PROPERTY"];
		var selectedBox = this._getSelectedValue(freezerBoxPropertyCode, false, this.sample);
		
		var localReference = this;
		var $container = $("#"+this.containerId);
		$container.empty();
		
		//
		// 1. Build a drop down to select the freezer
		//
		
		//Drop Down
		//Create and set the field
		var $freezerNameDropDown = this._getComponent(this._getPropertyFromType(freezerNamePropertyCode), false, null);
		$freezerNameDropDown.val(selectedFreezer);
		
		$freezerNameDropDown.change(
			function() {
				localReference._setSelectedValue(freezerRowPropertyCode, null);
				localReference._setSelectedValue(freezerColPropertyCode, null);
				localReference._setSelectedValue(freezerBoxPropertyCode, null);
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
		var $propertyTypeRowComponent = this._getComponent(this._getPropertyFromType(freezerRowPropertyCode), true, null);
		var $propertyTypeColComponent = this._getComponent(this._getPropertyFromType(freezerColPropertyCode), true, null);
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
				var $rackHeader = $("<th>");
				var rackId = "rack_0_" + i;
				$rackHeader.attr("id", rackId);
				
				if(i == 0) {
					$virtualFreezerRow.append($rackHeader);
				} else {
					$virtualFreezerRow.append($rackHeader.append(i));
				}
			}
			$virtualFreezer.append($virtualFreezerRow);
			
			//Paint Rows			
			for(var i = 0; i < freezerConfig["ROW_NUM"]; i++) {
				var $virtualFreezerRow = $("<tr>");
				for(var j = 0; j <= freezerConfig["COLUMN_NUM"]; j++) {
					if(j == 0) {
						var $rackHeader = $("<th>");
						var rackId = "rack_" + (i+1) + "_" + j;
						$rackHeader.attr("id", rackId);
						$virtualFreezerRow.append($rackHeader.append(i+1));
					} else {
						var $rack = $("<td>");
						
						//Used for validation
						var currentBoxes = 0;
						//Populate Box Names
						var boxesRow = localReference.selectedFreezerCache[(i+1)];
						if(boxesRow) {
							var boxesCol = boxesRow[j];
							if(boxesCol) {
								currentBoxes = boxesCol.length;
								currentBoxesArray = [];
								for(var box in boxesCol) {
									currentBoxesArray.push(box);
								}
								
								var sortedBoxesArray = currentBoxesArray.sort(naturalSort);
								
								for(var k = 0; k < sortedBoxesArray.length; k++) {
									$rack.append(
										$("<div>", { class: "freezerBox" })
											.append(sortedBoxesArray[k])
									);
								}
								
							}
						}
						
						//Atributes
						var rackId = "rack_" + (i+1) + "_" + j;
						$rack.attr("id", rackId);
						$rack.attr("rowNum", i+1);
						$rack.attr("colNum", j);
						$rack.attr("currentNum", currentBoxes);
						
						//Event
						$rack.click(function() {
							if(localReference.isDisabled) { return; }
							
							//Check if can be added
							var maxBoxes = freezerConfig["BOX_NUM"];
							if($(this).attr("currentNum") >= maxBoxes) {
								Util.showError("Limit of boxes reached on the rack, only allows " + maxBoxes + " boxes and found " + $(this).attr("currentNum") + ".", function() { Util.unblockUI(); });
								return;
							}
							
							//Clean the whole freezer
							$(".freezerSelectedRack").removeClass("freezerSelectedRack");
							
							//Select current spot
							var thisClass = $(this).attr("class");
							$(this).addClass('freezerSelectedRack');
						
							//Set the hidden fields
							localReference._setSelectedValue(freezerRowPropertyCode, $(this).attr("rowNum"));
							localReference._setSelectedValue(freezerColPropertyCode, $(this).attr("colNum"));
							
							localReference._repaint();
						});
						
						$rack.mouseover(function() {
							$(".freezerSelectedCorner").removeClass("freezerSelectedCorner");
							
							var rowNum = $(this).attr("rowNum");
							var colNum = $(this).attr("colNum");
							
							var rackIdRow = "#rack_" + rowNum + "_" + 0;
							$(rackIdRow).addClass('freezerSelectedCorner');
							
							var rackIdCol = "#rack_" + 0 + "_" + colNum;
							$(rackIdCol).addClass('freezerSelectedCorner');
							
							var rackId = "#rack_" + rowNum + "_" + colNum;
							$(rackId).addClass('freezerSelectedCorner');
						});
						
						//Append Rack
						$virtualFreezerRow.append($rack);
					}
				}
				$virtualFreezer.append($virtualFreezerRow);
			}
			
			if($virtualFreezer) {
				$virtualFreezer.mouseleave(function() {
					$(".freezerSelectedCorner").removeClass("freezerSelectedCorner");
				});
				
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
				var $propertyTypeBoxComponent = this._getComponent(this._getPropertyFromType(freezerBoxPropertyCode), false, '[A-Z0-9_]+');
				$propertyTypeBoxComponent.change(
					function() {
						$(this).val($(this).val().toUpperCase()); //Box Names can only be upper case
					}
				);

				
				$propertyTypeBoxComponent.val(selectedBox);
				$container
					.append($("<br>"))
					.append($("<div>")
								.append($("<i>", { class: "icon-info-sign" }))
								.append(" 3. Input the box name or number: ")
								.append($propertyTypeBoxComponent)
					);
			} else {
				var $propertyTypeBoxComponent = this._getComponent(this._getPropertyFromType(freezerBoxPropertyCode), true, null);
				$container.append($propertyTypeBoxComponent);
			}
			
			//
			// 4. Disable if needed
			//
			if(this.isDisabled) {
				$("#"+freezerNamePropertyCode).prop('disabled', true);
				$("#"+freezerRowPropertyCode).prop('disabled', true);
				$("#"+freezerColPropertyCode).prop('disabled', true);
				$("#"+freezerBoxPropertyCode).prop('disabled', true);
			}
		}
	}
}