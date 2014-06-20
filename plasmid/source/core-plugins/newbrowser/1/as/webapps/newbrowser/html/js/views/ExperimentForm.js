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
function ExperimentForm(containerId, mainController, experiment, mode) {
	this._containerId = containerId;
	this._mainController = mainController;
	this._experiment = experiment;
	this._mode = mode;
	this.isFormDirty = false;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var _this = this;
		$("#" + this._containerId).empty();
		
		var $form = $("<div>", { "class" : "row"});
		var $formColumn = $("<div>", { "class" : FormUtil.formColumClass + " form-horizontal", 'role' : "form"});
			
		$form.append($formColumn);
		
		//
		// Title
		//
		var $formTitle = null;
		if(this._mode === FormMode.VIEW || this._mode === FormMode.EDIT) {
			$formTitle = $("<h2>").append("Experiment " + this._experiment.identifier);
		} else {
			$formTitle = $("<h2>").append("Create " + this._experiment.experimentTypeCode);
		}
		$formColumn.append($formTitle);
		
		//
		// Create Sub Experiment
		//
		if(this._mode === FormMode.VIEW) {
			var $createSubExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Sub Experiment");
			$createSubExpBtn.click(function() {
				var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true);
				Util.blockUI("Select the type for the sub Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeDropdownCancel'>Cancel</a>");
				
				$("#sampleTypeDropdown").on("change", function(event) {
					var sampleTypeCode = $("#sampleTypeDropdown")[0].value;
					var argsMap = {
							"sampleTypeCode" : sampleTypeCode,
							"experimentIdentifier" : _this._experiment.identifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					
					_this._mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				
				$("#sampleTypeDropdownCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			});
			$formTitle.append(" ");
			$formTitle.append($createSubExpBtn);
		}
		
		//
		// Metadata Identification
		//
		var $identificationInfo = $('<div>').append($('<legend>').text("Identification Info"));
		$formColumn.append($identificationInfo);
		
		var identifierParts = this._experiment.identifier.split("/");
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Type", this._experiment.experimentTypeCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Project", identifierParts[2]));
		
		if(this._mode === FormMode.VIEW || this._mode === FormMode.EDIT) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", identifierParts[3]));
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registrator", this._experiment.registrationDetails.userId));
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registration Date", (new Date(this._experiment.registrationDetails.registrationDate)).toLocaleString()));
		} else if(this._mode === FormMode.CREATE){
			var $codeField = FormUtil._getInputField("text", "code", "code", null, true);
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($codeField, "Code"));
		}
		
		//
		// Metadata Fields
		//
		var experimentType = this._mainController.profile.getExperimentTypeForExperimentTypeCode(this._experiment.experimentTypeCode);
		
		for(var i = 0; i < experimentType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = experimentType.propertyTypeGroups[i];
			
			var $fieldset = $('<div>');
			var $legend = $('<legend>'); 
			$fieldset.append($legend);
			
			if(propertyTypeGroup.name) {
				$legend.text(propertyTypeGroup.name);
			} else if(experimentType.propertyTypeGroups.length === 1) { //Only when there is only one group without name to render it with a default title.
				$legend.text("Metadata Fields");
			} else {
				$legend.remove();
			}
			
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				
				var $controlGroup =  null;
				
				if(this._mode === FormMode.VIEW) {
					$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, this._experiment.properties[propertyType.code]);
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					$component.change(function(event) {
						localInstance.isFormDirty = true;
					});
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				
				$fieldset.append($controlGroup);
			}
			
			$formColumn.append($fieldset);
		}
		
		$("#" + this._containerId).append($form);
	}
}