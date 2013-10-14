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
 * Creates an instance of Storage.
 *
 * @constructor
 * @this {Storage}
 * @param {ServerFacade} serverFacade The facade used to access server side search functionality.
 * @param {String} containerId The Container where the Storage DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 * @param {String} sampleTypeCode The code of the sample type, needed to know where to check if the properties are available.
 * @param {Sample} sample The sample where check the properties for VIEW and EDIT modes.
 * @param {boolean} isDisabled If the storage should allow to be edited.
 */
function Storage(serverFacade, containerId, profile, sampleTypeCode, sample, isDisabled) {
	this.serverFacade = serverFacade;
	this.containerId = containerId;
	this.profile = profile;
	this.sampleType = profile.getTypeForTypeCode(sampleTypeCode);
	this.isStorageAvailable = false;
	this.sample = sample; // Needed for edit mode
	this.isDisabled = isDisabled; //Needed for view mode
	this.selectedStorageCache = null;
	
	//
	// Private utility methods used to check if a storage is properly configured for certain entity type.
	//
	this._isStorageForType = function() {
		for(var storageProperty in this.profile.storagesConfiguration["STORAGE_PROPERTIES"]) {
			var storagePropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"][storageProperty];
			if(!(this._getPropertyFromType(storagePropertyCode) !== null)) {
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
	
	this._setSelectedValue = function(propertyTypeCode, storageIndex) {
		var propertyType = this._getPropertyFromType(propertyTypeCode);
		if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			if(storageIndex === null) {
				$('#' + propertyTypeCode).prop('selectedIndex', 0);
			} else {
				$('#' + propertyTypeCode).prop('selectedIndex', storageIndex);
			}
		} else {
			if(storageIndex === null) {
				$('#' + propertyTypeCode).val("");
			} else {
				$('#' + propertyTypeCode).val(storageIndex);
			}
		}
	}
		
	//
	// Public utility methods used by the sample form to know what is responsability of the storage. To behave correctly, init should be executed first.
	//
	this.isPropertyGroupFromStorage = function(propertyGroupName) {
		if(!this.isStorageAvailable) { return false; }
		
		if(this.profile.storagesConfiguration["STORAGE_PROPERTY_GROUP"] === propertyGroupName) {
			return true;
		}
		return false;
	}
	
	this.isPropertyFromStorage = function(samplePropertyTypeCode) {
		if(!this.isStorageAvailable) { return false; }
		
		var storageProperties = this.profile.storagesConfiguration["STORAGE_PROPERTIES"];
		for(var propertyType in storageProperties) {
			var storagePropertyTypeCode = storageProperties[propertyType];
			if(storagePropertyTypeCode === samplePropertyTypeCode) {
				return true;
			}
		}
		return false;
	}
	
	//
	// Main methods to interact with the storage
	//
	this.init = function() {
		this.isStorageAvailable = this.profile.storagesConfiguration["isEnabled"] && this._isStorageForType();
	}
	
	this.repaint = function() {
		if(!this.isStorageAvailable) { return; }
			
		var storageNamePropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["NAME_PROPERTY"];
		var selectedStorage = this._getSelectedValue(storageNamePropertyCode, false, this.sample);
		
		//Build storage cache and paint it afterwards
		if(selectedStorage) {
			var storageRowPropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["ROW_PROPERTY"];
			var storageColPropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["COLUMN_PROPERTY"];
			var storageBoxPropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["BOX_PROPERTY"];
			
			var propertyTypeCodes = [storageNamePropertyCode, storageRowPropertyCode, storageColPropertyCode, storageBoxPropertyCode];
			var propertyValues = ["'" + selectedStorage + "'", "?*", "?*", "?*"];
			
			var localReference = this;
			 
			var $container = $("#"+this.containerId);
			$container.children().hide();
			$container
				.append($("<div>")
							.append($("<i>", { class: "icon-info-sign" }))
							.append(" Loading... ")
				);
			
			this.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues,
				function(samples) {
					var boxes = []; //Rows
					
					samples.forEach(
						function(element, index, array) {
							var boxCode = element.properties[storageBoxPropertyCode];
							
								//Ad new box 
								var boxRow = localReference._getSelectedValue(storageRowPropertyCode, true, element);
								var boxCol = localReference._getSelectedValue(storageColPropertyCode, true, element);
								
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
					
					localReference.selectedStorageCache = boxes;
					localReference._repaint();
				});
		} else {
			this._repaint();
		}
	}
	
	this._repaint = function() {
		if(!this.isStorageAvailable) { return; }
		
		var storageNamePropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["NAME_PROPERTY"];
		var selectedStorage = this._getSelectedValue(storageNamePropertyCode, false, this.sample);
		
		var storageRowPropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["ROW_PROPERTY"];
		var selectedRow = this._getSelectedValue(storageRowPropertyCode, true, this.sample);
		
		var storageColPropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["COLUMN_PROPERTY"];
		var selectedCol = this._getSelectedValue(storageColPropertyCode, true, this.sample);
		
		var storageBoxPropertyCode = this.profile.storagesConfiguration["STORAGE_PROPERTIES"]["BOX_PROPERTY"];
		var selectedBox = this._getSelectedValue(storageBoxPropertyCode, false, this.sample);
		
		var localReference = this;
		var $container = $("#"+this.containerId);
		$container.empty();
		
		//
		// 1. Build a drop down to select the storage
		//
		
		//Drop Down
		//Create and set the field
		var $storageNameDropDown = this._getComponent(this._getPropertyFromType(storageNamePropertyCode), false, null);
		$storageNameDropDown.val(selectedStorage);
		
		$storageNameDropDown.change(
			function() {
				localReference._setSelectedValue(storageRowPropertyCode, null);
				localReference._setSelectedValue(storageColPropertyCode, null);
				localReference._setSelectedValue(storageBoxPropertyCode, null);
				localReference.repaint();
			}
		);
		
		//Component with Dropdown
		$container
			.append($("<div>")
						.append($("<i>", { class: "icon-info-sign" }))
						.append(" 1. Select a storage: ")
						.append($storageNameDropDown)
			);
		
		//
		// 2. Build a table to select the row and column.
		//
		var $propertyTypeRowComponent = null;
		var $propertyTypeColComponent = null;

		//Generate virtual storage representation after the storage has been selected
		var storageConfig = null;
		if(selectedStorage !== null && selectedStorage !== "") {
			storageConfig = this.profile.storagesConfiguration["STORAGE_CONFIGS"][selectedStorage];
			
			if(!storageConfig) {
				Util.showError("Storage configuration missing, visual storage can't be displayed.", function() { Util.unblockUI(); });
			} else {

			//Attach row and column hidden fields
			$propertyTypeRowComponent = this._getComponent(this._getPropertyFromType(storageRowPropertyCode), true, null);
			$propertyTypeColComponent = this._getComponent(this._getPropertyFromType(storageColPropertyCode), true, null);
			
			$container
				.append($propertyTypeRowComponent)
				.append($propertyTypeColComponent);
			}
		}
		
		var $virtualStorage = null;
		if(storageConfig) {
			$virtualStorage = $("<table>");
			$virtualStorage.attr('class', 'storageTable');
			
			//Paint Columns on the header
			var $virtualStorageRow = $("<tr>");
			for(var i = 0; i <= storageConfig["COLUMN_NUM"]; i++) {
				var $rackHeader = $("<th>");
				var rackId = "rack_0_" + i;
				$rackHeader.attr("id", rackId);
				
				if(i == 0) {
					$virtualStorageRow.append($rackHeader);
				} else {
					$virtualStorageRow.append($rackHeader.append(i));
				}
			}
			$virtualStorage.append($virtualStorageRow);
			
			//Paint Rows			
			for(var i = 0; i < storageConfig["ROW_NUM"]; i++) {
				var $virtualStorageRow = $("<tr>");
				for(var j = 0; j <= storageConfig["COLUMN_NUM"]; j++) {
					if(j == 0) {
						var $rackHeader = $("<th>");
						var rackId = "rack_" + (i+1) + "_" + j;
						$rackHeader.attr("id", rackId);
						$virtualStorageRow.append($rackHeader.append(i+1));
					} else {
						var $rack = $("<td>");
						
						//Used for validation
						var currentBoxes = 0;
						//Populate Box Names
						var boxesRow = localReference.selectedStorageCache[(i+1)];
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
										$("<div>", { class: "storageBox" })
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
							var maxBoxes = storageConfig["BOX_NUM"];
							if($(this).attr("currentNum") >= maxBoxes) {
								Util.showError("Limit of boxes reached on the rack, only allows " + maxBoxes + " boxes and found " + $(this).attr("currentNum") + ".", function() { Util.unblockUI(); });
								return;
							}
							
							//Clean the whole storage
							$(".storageSelectedRack").removeClass("storageSelectedRack");
							
							//Select current spot
							$(this).addClass('storageSelectedRack');
						
							//Set the hidden fields
							localReference._setSelectedValue(storageRowPropertyCode, $(this).attr("rowNum"));
							localReference._setSelectedValue(storageColPropertyCode, $(this).attr("colNum"));
							
							localReference._repaint();
						});
						
						$rack.mouseover(function() {
							$(".storageSelectedCorner").removeClass("storageSelectedCorner");
							
							var rowNum = $(this).attr("rowNum");
							var colNum = $(this).attr("colNum");
							
							var rackIdRow = "#rack_" + rowNum + "_" + 0;
							$(rackIdRow).addClass('storageSelectedCorner');
							
							var rackIdCol = "#rack_" + 0 + "_" + colNum;
							$(rackIdCol).addClass('storageSelectedCorner');
							
							var rackId = "#rack_" + rowNum + "_" + colNum;
							$(rackId).addClass('storageSelectedCorner');
						});
						
						//Append Rack
						$virtualStorageRow.append($rack);
					}
				}
				$virtualStorage.append($virtualStorageRow);
			}
			
			if($virtualStorage) {
				$virtualStorage.mouseleave(function() {
					$(".storageSelectedCorner").removeClass("storageSelectedCorner");
				});
				
				$container
					.append($("<div>")
								.append($("<i>", { class: "icon-info-sign" }))
								.append(" 2. Select a rack: ")
								.append($virtualStorage)
					)
			
				if(selectedRow && selectedCol) {
					//Set the visual storage
					var rackId = "#rack_" + selectedRow + "_" + selectedCol;
					$(rackId).addClass('storageSelectedRack');
					//Set the hidden fields
					localReference._setSelectedValue(storageRowPropertyCode, selectedRow);
					localReference._setSelectedValue(storageColPropertyCode, selectedCol);
				}
			}
			
			//
			// 3. Input the box name
			//
			if($virtualStorage && selectedRow && selectedCol) {
				//Create and set the field
				var $propertyTypeBoxComponent = this._getComponent(this._getPropertyFromType(storageBoxPropertyCode), false, '[A-Z0-9_]+');
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
			} if(storageConfig) {
				var $propertyTypeBoxComponent = this._getComponent(this._getPropertyFromType(storageBoxPropertyCode), true, null);
				$container.append($propertyTypeBoxComponent);
			}
			
		} else if(selectedStorage){ //Configuration don´t exists
			$propertyTypeRowComponent = this._getComponent(this._getPropertyFromType(storageRowPropertyCode), false, null);
			$container
				.append($("<div>")
					.append($("<i>", { class: "icon-info-sign" }))
					.append(" 2. Select Row: ")
					.append($propertyTypeRowComponent)
				);
			localReference._setSelectedValue(storageRowPropertyCode, selectedRow);

			$propertyTypeColComponent = this._getComponent(this._getPropertyFromType(storageColPropertyCode), false, null);
			$container
				.append($("<div>")
					.append($("<i>", { class: "icon-info-sign" }))
					.append(" 3. Select Column: ")
					.append($propertyTypeColComponent)
				);
			localReference._setSelectedValue(storageColPropertyCode, selectedCol);

			var $propertyTypeBoxComponent = this._getComponent(this._getPropertyFromType(storageBoxPropertyCode), false, null);
			$container
				.append($("<div>")
							.append($("<i>", { class: "icon-info-sign" }))
							.append(" 4. Input the box name or number: ")
							.append($propertyTypeBoxComponent)
				);
			localReference._setSelectedValue(storageBoxPropertyCode, selectedBox);
		}

		//
		// 4. Disable if needed
		//
		if(this.isDisabled) {
			$("#"+storageNamePropertyCode).prop('disabled', true);
			$("#"+storageRowPropertyCode).prop('disabled', true);
			$("#"+storageColPropertyCode).prop('disabled', true);
			$("#"+storageBoxPropertyCode).prop('disabled', true);
		}
	}
}