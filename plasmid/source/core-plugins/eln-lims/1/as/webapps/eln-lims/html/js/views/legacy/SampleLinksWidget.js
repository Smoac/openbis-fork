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

	
	this._writeState = function(sample, propertyTypeCode, propertyTypeValue, isDelete) {

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
		} else if(propertyTypeCode && propertyTypeValue !== null && propertyTypeValue !== undefined) {
			sampleTypeAnnotations[propertyTypeCode] = propertyTypeValue;
		}
		
		var xmlDoc = FormUtil.getXMLFromAnnotations(this.stateObj);
		mainController.currentView._sampleFormModel.sample.properties["ANNOTATIONS_STATE"] = xmlDoc;
	}
	
	this._readState = function() {
		var isStateFieldAvailable = false;
		
		if(mainController.currentView._sampleFormModel.sample) {
			var availableFields = profile.getAllPropertiCodesForTypeCode(mainController.currentView._sampleFormModel.sample.sampleTypeCode);
			var pos = $.inArray("ANNOTATIONS_STATE", availableFields);
			isStateFieldAvailable = (pos !== -1);
		}
		
		if(!isStateFieldAvailable) {
			if(this.sampleTypeHints && this.sampleTypeHints.length !== 0) { //Indicates annotations are needed
				Util.showError("You need a property with code ANNOTATIONS_STATE on this entity to store the state of the annotations.");
			}
		} else {
			//Hide State Field
			var fieldset = $("#ANNOTATIONS_STATE").parent().parent().parent();
			fieldset.hide();
			
			//Update Values
			var stateField = mainController.currentView._sampleFormModel.sample.properties["ANNOTATIONS_STATE"];
			this.stateObj = FormUtil.getAnnotationsFromField(stateField);
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
		var $controls = $("<div>", { "class" : "controls " + FormUtil.controlColumnClassBig, "style" : "margin-top:6px;"});
			
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
					
					$controls.append("<b>" + propertyType.label + "</b>: ");
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
				$buttonPlusOne.hide();
				$buttonDelete.attr("disabled", "");
				$buttonDelete.hide();
			} else {
				$buttonTextField.click(function(elem) {
					var $buttonClicked = $(elem);
					var sampleTypeCode = sampleTypeHint["TYPE"];
					var sampleType = _this.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
					
					if(sampleType !== null) {
						
						mainController.serverFacade.searchWithType(sampleTypeCode, null, false, function(samples) {
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
							
							var dataGrid = SampleDataGridUtil.getSampleDataGrid(sampleTypeCode, samples, rowClick);
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
			$buttonPlus.hide();
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
			
			var name = null;
			if(sampleToAdd.properties) {
				name = sampleToAdd.properties["NAME"];
				if(!name) {
					name = sampleToAdd.properties["PLASMID_NAME"];
				}
				if(!name) {
					name = sampleToAdd.properties["YEAST_STRAIN_NAME"];
				}
			}
			
			var info = $("<span>");
			
			var codeLink = sampleToAdd.code;
			if(this.isDisabled) {
				var style = "";
				var notFound = "";
				if(sampleToAdd.notFound) {
					style = "color: red;"
					notFound = " (Not Found)"
				}
				
				var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), "showViewSamplePageFromPermId", sampleToAdd.permId);
				codeLink = $("<a>", { "style" : style, "href" : href, "class" : "browser-compatible-javascript-link" }).append(sampleToAdd.code + notFound);
				
				codeLink.click(function() {
					mainController.changeView("showViewSamplePageFromPermId", sampleToAdd.permId);
//					var url = document.location.href;
//					url = url.substring(0,url.lastIndexOf("/?") + 1);
//					url = url+"?viewName=showViewSamplePageFromPermId&viewData=" + sampleToAdd.permId + "&hideMenu=true";
//					var newWindow = window.open(url);
				});
			}
			
			var infoCode = $("<span>")
								.append($("<b>").append("Code: "))
								.append(codeLink);
			info.append(infoCode);
			
			if(name) {
				var infoName = $("<span>")
						.append($("<b>").append("&nbsp;Name: "))
						.append($("<span>").text(name));
				info.append(infoName);
			}
			
			var $input = $("#" +freePredefinedSampleId);
			
			$input.empty();
			$input.append(info);
			
			if(this.isDisabled) {
				$input.after(info);
				$input.hide();
			}
			//Update annotations when adding an existing sample for updates
			var sampleState = this.stateObj[sampleToAdd.permId];
			var items = $input.parent().children();
			for(var i = 0; i < items.length; i++) {
				var item = $(items[i]);
				var propertyTypeCode = item.attr("property-type-code");
				if(		propertyTypeCode && sampleState && 
						sampleState[propertyTypeCode] !== null && 
						sampleState[propertyTypeCode] !== undefined) {
					if (this.profile.getPropertyType(propertyTypeCode).dataType === "BOOLEAN") {
						item.children()[0].checked = sampleState[propertyTypeCode] === "true";
					} else {
						item.val(sampleState[propertyTypeCode]);
					}
					
					if(this.isDisabled) {
						item.after($("<span>").text(sampleState[propertyTypeCode]));
					}
				}
				
				if(this.isDisabled && propertyTypeCode) {
					item.hide();
				} else if(!this.isDisabled) {
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
			var items = $input.parent().children();
			for(var i = 0; i < items.length; i++) {
				var item = $(items[i]);
				var propertyTypeCode = item.attr("property-type-code");
				if(propertyTypeCode) {
					item.val("");
					item.prop("disabled", true);
				}
			}
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