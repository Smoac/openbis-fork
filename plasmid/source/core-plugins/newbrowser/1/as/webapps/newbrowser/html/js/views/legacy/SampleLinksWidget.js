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

function SampleLinksWidget(containerId, profile, serverFacade, title, sampleTypeHints, isDisabled, samplesToEdit, showAnnotableTypes) {
	this.containerId = containerId;
	this.profile = profile;
	this.serverFacade = serverFacade;
	this.title = title;
	this.sampleTypeHints = sampleTypeHints;
	this.isDisabled = isDisabled;
	this.samplesToEdit = (samplesToEdit)?samplesToEdit:[]; //Only used to populate the widget
	this.showAnnotableTypes = showAnnotableTypes;
	this.samples = {};
	this.samplesRemoved = {};
	this.stateObj = {};
		
	this._lastUsedId = null;
	this._lastIndex = 0;
	
//	this._enableAnnotations = function() {
//		var enableAnnotations = false;
//		for(var i = 0; i < this.sampleTypeHints.length; i++) {
//			var sampleTypeHint = this.sampleTypeHints[i];
//			if(sampleTypeHint["ANNOTATION_PROPERTIES"].length > 0) {
//				enableAnnotations = true;
//			}
//		}
//		return true;
//	}
	
	this._writeState = function(sample, propertyTypeCode, propertyTypeValue, isDelete) {
//		if(!this._enableAnnotations()) {
//			return;
//		}
		this._readState();
		
		var sampleTypeAnnotations = this.stateObj[sample.permId];
		if(!sampleTypeAnnotations) {
			sampleTypeAnnotations = {};
			this.stateObj[sample.permId] = sampleTypeAnnotations;
		}
		
		sampleTypeAnnotations["identifier"] =  sample.identifier; //Adds code to the annotations if not present
		sampleTypeAnnotations["sampleType"] =  sample.sampleTypeCode; //Adds sampleType code to the annotations if not present
		
		if(isDelete) {
			delete this.stateObj[sample.permId];
		} else if(propertyTypeCode && propertyTypeValue) {
			sampleTypeAnnotations[propertyTypeCode] = propertyTypeValue;
		}
		
		var xmlDoc = "<root>";
		
		for(var permId in this.stateObj) {
			xmlDoc	+= "<Sample permId=\"" + permId + "\""; 
			for(var propertyTypeCode in this.stateObj[permId]) {
				if(propertyTypeCode == "identifier") {
					var propertyTypeValue = this.stateObj[permId][propertyTypeCode];
					xmlDoc	+= " " + propertyTypeCode + "=\"" + propertyTypeValue +"\"";
				}
			}
			
			for(var propertyTypeCode in this.stateObj[permId]) {
				if(propertyTypeCode == "sampleType") {
					var propertyTypeValue = this.stateObj[permId][propertyTypeCode];
					xmlDoc	+= " " + propertyTypeCode + "=\"" + propertyTypeValue +"\"";
				}
			}
			
			for(var propertyTypeCode in this.stateObj[permId]) {
				if(propertyTypeCode != "identifier" && propertyTypeCode != "sampleType") {
					var propertyTypeValue = this.stateObj[permId][propertyTypeCode];
					xmlDoc	+= " " + propertyTypeCode + "=\"" + propertyTypeValue +"\"";
				}
			}
			
			xmlDoc	+= " />";
		}
		
		xmlDoc	+= "</root>";
		
		$("#ANNOTATIONS_STATE").val(xmlDoc);
		
		//Compatibility mode for refactored sample form
		if(mainController.currentView._sampleFormModel) {
			mainController.currentView._sampleFormModel.sample.properties["ANNOTATIONS_STATE"] = xmlDoc;
		}
	}
	
	this._readState = function() {
//		if(!this._enableAnnotations()) {
//			return;
//		}
		var stateField = $("#ANNOTATIONS_STATE");
		if(stateField.length === 0) {
			if(this.sampleTypeHints && this.sampleTypeHints.length !== 0) { //Indicates annotations are needed
				Util.showError("You need a property with code ANNOTATIONS_STATE on this entity to store the state of the annotations.");
			}
		} else {
			//Hide State Field
			var fieldset = stateField.parent().parent().parent();
			fieldset.hide();
			
			//Update Values
			this.stateObj = {};
			var stateFieldValue = Util.getEmptyIfNull(stateField.val());
			//Hack to fix for new sample form on view mode
			if(mainController.currentView._sampleFormModel && mainController.currentView._sampleFormModel.mode === FormMode.VIEW) {
				stateFieldValue = Util.getEmptyIfNull(stateField.text());
			}
			//
			if(stateFieldValue === "") {
				return;
			}
			var xmlDoc = new DOMParser().parseFromString(stateFieldValue, 'text/xml');
			var samples = xmlDoc.getElementsByTagName("Sample");
			for(var i = 0; i < samples.length; i++) {
				var sample = samples[i];
				var permId = sample.attributes["permId"].value;
				for(var j = 0; j < sample.attributes.length; j++) {
					var attribute = sample.attributes[j];
					if(attribute.name !== "permId") {
						if(!this.stateObj[permId]) {
							this.stateObj[permId] = {};
						}
						this.stateObj[permId][attribute.name] = attribute.value;
					}
				}
			}
		}
	}
	
	this._getDefaultSampleHint = function(sampleTypeCode) {
		var defaultMinCount = 0;
		var defaultProperties = [];
		
		for(var i = 0; i < sampleTypeHints.length; i++) {
			if(sampleTypeHints[i]["TYPE"] === sampleTypeCode) {
				defaultMinCount = sampleTypeHints[i]["MIN_COUNT"];
				defaultProperties = sampleTypeHints[i]["ANNOTATION_PROPERTIES"];
			}
		}
		
		var typeToAdd = {
				"LABEL" : sampleTypeCode,
				"TYPE": sampleTypeCode,
				"MIN_COUNT" : defaultMinCount,
				"ANNOTATION_PROPERTIES" : defaultProperties
		};
		
		return typeToAdd;
	}
	this._addAny = function(id, tableId, sampleId) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var component = "<select id='sampleTypeSelector' class='form-control' required>";
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
		
		Util.blockUI("Select type: <br><br>" + component + "<br> or <a class='btn btn-default' id='sampleTypeSelectorCancel'>Cancel</a>");
		
		$("#sampleTypeSelectorCancel").on("click", function(event) { 
			Util.unblockUI();
		});
		
		var _this = this;
		$("#sampleTypeSelector").on("change", function(event) {
			var sampleTypeCode = $("#sampleTypeSelector")[0].value;
			var sampleType = _this.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
			
			if(sampleType !== null) {
				if(_this._lastUsedId) {
					$('#'+_this._lastUsedId + "-table").empty();
					$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
				}
				
				var typeToAdd = _this._getDefaultSampleHint(sampleTypeCode);
				_this.addOneSlot(typeToAdd);
				Util.unblockUI();
			}
		});
	}
	
	this._getButton = function(id, sampleTypeHint) {
		var _this = this;
		var tableId = id + "-table";
		var sampleId = id + "-sample";
		var $component = $("<div>", {"id" : id , "class" : "form-inline control-group row", "sample-type-code" : sampleTypeHint["TYPE"], "sample-min-count" : sampleTypeHint["MIN_COUNT"] } );
		$component.css({"border-radius" : "10px", "padding-left" : "10px", "margin-top" : "10px"});
		
		var requiredText = "";
		if(sampleTypeHint["MIN_COUNT"] > 0) {
			requiredText = " (Required at least " + sampleTypeHint["MIN_COUNT"] + ")";
		}
		
		var labelText = sampleTypeHint["LABEL"] + requiredText + ":";
		if(sampleTypeHint["LABEL"] === null) {
			labelText = "";
		}
		var $label = $("<label>", { "class" : "control-label " + FormUtil.labelColumnClass }).text(labelText);	
		var $controls = $("<div>", { "class" : "controls " + FormUtil.controlColumnClassBig});
			
			var $buttonTextField = $("<a>", {"class" : "btn btn-default", "type" : "button", "id" : sampleId});
			$buttonTextField.css({
				"max-width" : "90%",
				"text-align" : "left"
			});
			
			$buttonTextField.append("Select");
			$controls.append($buttonTextField);
			$controls.append(" ");
			
			var annotations = sampleTypeHint["ANNOTATION_PROPERTIES"];
			var annotationComponents =  [];
			
			for(var i = 0; i < annotations.length; i++) {
					var propertyType = this.profile.getPropertyType(annotations[i]["TYPE"]);
					propertyType.mandatory = annotations[i]["MANDATORY"];
					var $propertyField = FormUtil.getFieldForPropertyType(propertyType);
					if (propertyType.dataType === "BOOLEAN") {
						$($propertyField.children()[0]).css({
							'margin-bottom' : '15px'
						});
					}
					$propertyField.attr("property-type-code" , annotations[i]["TYPE"]);
					$propertyField.prop("disabled", true);
					$propertyField.change(function() {
						var $field = $(this);
						var sample = _this.samples[sampleId];
						var propertyTypeCode = $field.attr("property-type-code");
						var propertyType = _this.profile.getPropertyType(propertyTypeCode)
						var propertyTypeValue;
						if (propertyType.dataType === "BOOLEAN") {
							propertyTypeValue = $field.children().is(":checked");
						} else {
							propertyTypeValue = $field.val();
						}
						_this._writeState(sample, propertyTypeCode, propertyTypeValue, false);
					});
					
					$controls.append(propertyType.label + ": ");
					$controls.append($propertyField);
					$controls.append(" ");
					annotationComponents.push($propertyField);
			}
			
			var $buttonPlusOne = $("<a>", {"class" : "btn btn-default"});
			$buttonPlusOne.append($("<span>", { "class" : "glyphicon glyphicon-plus-sign"}));
			$controls.append($buttonPlusOne);
			$controls.append(" ");
			
			var $buttonDelete = $("<a>", {"class" : "btn btn-default"});
			$buttonDelete.append($("<span>", { "class" : "glyphicon glyphicon-minus-sign"}));
			$controls.append($buttonDelete);
			
			if(this.isDisabled) {
				$buttonTextField.attr("disabled", "");
				for(var i = 0; i < annotationComponents.length; i++) {
					annotationComponents[i].attr("disabled", "");
				}
				$buttonPlusOne.attr("disabled", "");
				$buttonDelete.attr("disabled", "");
			} else {
				$buttonTextField.click(function(elem) {
					var $buttonClicked = $(elem);
					var sampleTypeCode = sampleTypeHint["TYPE"];
					var sampleType = _this.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
					
					if(sampleType !== null) {
						
						mainController.serverFacade.searchWithType(sampleTypeCode, null, function(samples) {
							//Clear last state
							if(_this._lastUsedId) {
								$('#'+_this._lastUsedId + "-table").empty();
								$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
							}
							//Put new state
							var rowClick = function(e) {
								$('#'+_this._lastUsedId + "-table").empty();
								$("#"+_this._lastUsedId).css({"background-color" : "#FFFFFF" });
								_this.removeSample(sampleId);
								mainController.serverFacade.searchWithUniqueId(e.data.permId, function(data) {
									_this.addSample(data[0]);
									_this._writeState(data[0], null, null, false);
								});
								$("#" + id).css({"background-color" : "#FFFFFF" });
							}
							$("#" + id).css({"border-radius" : "10px", "padding" : "10px", "background-color" : "#EEEEEE" });
							
							//Create grid model for sample type
							var propertyCodes = profile.getAllPropertiCodesForTypeCode(sampleTypeCode);
							var propertyCodesDisplayNames = profile.getPropertiesDisplayNamesForTypeCode(sampleTypeCode, propertyCodes);
							
							//Fill Columns model
							var columns = [ {
								label : 'Code',
								property : 'code',
								sortable : true
							}, {
								label : 'Preview',
								property : 'preview',
								sortable : false,
								render : function(data) {
									var previewContainer = $("<div>");
									mainController.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [data.permId], function(data) {
										data.result.forEach(function(dataset) {
											var listFilesForDataSetCallback = function(dataFiles) {
												for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
													if(!dataFiles.result[pathIdx].isDirectory) {
														var downloadUrl = profile.allDataStores[0].downloadUrl + '/' + dataset.code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
														var previewImage = $("<img>", { 'src' : downloadUrl, 'class' : 'zoomableImage', 'style' : 'height:80px;' });
														previewImage.click(function(event) {
															Util.showImage(downloadUrl);
															event.stopPropagation();
														});
														previewContainer.append(previewImage);
														break;
													}
												}
											};
											mainController.serverFacade.listFilesForDataSet(dataset.code, "/", true, listFilesForDataSetCallback);
										});
									});
									return previewContainer;
								},
								filter : function(data, filter) {
									return false;
								},
								sort : function(data1, data2, asc) {
									return 0;
								}
							}];
							
							
							for (var idx = 0; idx < propertyCodes.length; idx++) {
								columns.push({
									label : propertyCodesDisplayNames[idx],
									property : propertyCodes[idx],
									sortable : true
								});
							}
							
							//Fill data model
							var getDataList = function(callback) {
								var dataList = [];
								for(var sIdx = 0; sIdx < samples.length; sIdx++) {
									var sample = samples[sIdx];
									var sampleModel = { 'code' : sample.code, 'permId' : sample.permId };
									for (var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
										var property = propertyCodes[pIdx];
										sampleModel[property] = sample.properties[property];
									}
									dataList.push(sampleModel);
								}
								callback(dataList);
							};
							
							var dataGrid = new DataGridController(null, columns, getDataList, rowClick);
							dataGrid.init($("#" + tableId));
							
							//Store new state
							_this._lastUsedId = id;
						});
						
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
			
			$component.append($("<div>", { "id" : tableId, 'style' : 'clear: both;' }));
		return $component;
	}
	
	this._getPlus = function() {
		var id = this.containerId + "-plus-button";
		var tableId = id + "-table";
		var $component = $("<div>", { "id" : id, "class" : "form-group", "style" : 'padding: 10px 0px 0px 10px'} );
		var $controls = $("<div>", { "class" : "col-md-12"});
		
		var $buttonPlus = $("<a>", {"class" : "btn btn-default"});
		$buttonPlus.append($("<span>", { "class" : "glyphicon glyphicon-plus-sign"}));
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
		
		//Add predefined slots if they are mandatory
		for(var i = 0; i < this.sampleTypeHints.length; i++) {
			if(this.showAnnotableTypes || sampleTypeHints[i].MIN_COUNT > 0) {
				this.addOneSlot(sampleTypeHints[i]);
			}
		}
		
		//Initialize annotations from property
		this._readState();
		
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
				
				var typeToAdd = this._getDefaultSampleHint(sampleToAdd.sampleTypeCode);
				this.addOneSlot(typeToAdd);
			}
			
			//Finally, add the sample
			this.samples[freePredefinedSampleId] = sampleToAdd;
			
			//Show meaningful information
			var propertiesToShow = this.profile.typePropertiesForSmallTable[sampleToAdd.sampleTypeCode];
			if(propertiesToShow === null || propertiesToShow === undefined) {
				propertiesToShow = [];
			}
			
			var propertiesToShowDisplayNames = this.profile.getPropertiesDisplayNamesForTypeCode(sampleToAdd.sampleTypeCode, propertiesToShow);
			
			var meaningfulInfo = "<b>Code: </b>" + sampleToAdd.code + " ";
			
			
			for(var j = 0; j < propertiesToShow.length; j++) {
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
			
			//Update annotations when adding an existing sample for updates
			var sampleState = this.stateObj[sampleToAdd.permId];
			var items = $input.parent().children();
			for(var i = 0; i < items.length; i++) {
				var item = $(items[i]);
				var propertyTypeCode = item.attr("property-type-code");
				if(propertyTypeCode && sampleState && sampleState[propertyTypeCode]) {
					if (this.profile.getPropertyType(propertyTypeCode).dataType === "BOOLEAN") {
						item.children()[0].checked = sampleState[propertyTypeCode] === "true";
					} else {
						item.val(sampleState[propertyTypeCode]);
					}
				}
				if(!this.isDisabled) {
					item.prop("disabled", false);
				}
			}
		} else {
			Util.showError("Item Already selected, choose another.");
		}
	}

	this.removeSample = function(sampleId) {
		var sample = this.samples[sampleId];
		if(sample) {
			//Remove Link
			var $input = $('#'+sampleId);
			$input.empty();
			$input.append("Select");
			
			//Remove Link Annotations
//			var items = $input.parent().children();
//			for(var i = 0; i < items.length; i++) {
//				var item = $(items[i]);
//				var propertyTypeCode = item.attr("property-type-code");
//				if(propertyTypeCode) {
//					item.val("");
//					item.prop("disabled", true);
//				}
//			}
			this._writeState(sample, null, null, true);
			//Update
			this.samplesRemoved[sampleId] = this.samples[sampleId];
			this.samples[sampleId] = null;
		}
	}
	
	this.getSamples = function() {
		var samples = new Array();
		for(sampleObjKey in this.samples) {
			var sampleObj = this.samples[sampleObjKey];
			if(sampleObj !== null) {
				samples.push(sampleObj);
			}
		}
		return samples;
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
	
	this.getSampleByIdentifier = function(identifier) {
		for(sampleObjKey in this.samples) {
			var sampleObj = this.samples[sampleObjKey];
			if(sampleObj !== null && sampleObj.identifier === identifier) {
				return sampleObj;
			}
		}
		return null;
	}
	
	this.getSamplesRemovedIdentifiers = function() {
		var sampleIdentifiers = new Array();
		for(sampleObjKey in this.samplesRemoved) {
			var sampleObj = this.samplesRemoved[sampleObjKey];
			if(sampleObj !== null) {
				sampleIdentifiers.push(sampleObj.identifier);
			}
		}
		return sampleIdentifiers;
	}
	
	this.isValid = function() {
		var isValid = true;
		for(sampleTypeHintKey in this.sampleTypeHints) {
			var sampleTypeHint = this.sampleTypeHints[sampleTypeHintKey];
			var minCount = sampleTypeHint["MIN_COUNT"];
			var found = 0;
			for(sampleKey in this.samples) {
				var sample = this.samples[sampleKey];
				if(sample && sample.sampleTypeCode === sampleTypeHint["TYPE"]) {
					found++;
				}
			}
			
			if(found < minCount) {
				isValid = false;
			}
		}
		return isValid;
	}
}