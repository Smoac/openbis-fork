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

function ExperimentFormView(experimentFormController, experimentFormModel) {
	this._experimentFormController = experimentFormController;
	this._experimentFormModel = experimentFormModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "row"});
		var $formColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : "mainController.currentView._updateExperiment();"
		});
			
		$form.append($formColumn);
		
		//
		// Title
		//
		var $formTitle = null;
		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			$formTitle = $("<h2>").append("Experiment " + this._experimentFormModel.experiment.identifier);
		} else {
			$formTitle = $("<h2>").append("Create " + this._experimentFormModel.experiment.experimentTypeCode);
		}
		$formColumn.append($formTitle);
		
		//
		// Create Sub Experiment
		//
		if(this._experimentFormModel.mode === FormMode.VIEW) {
			var $createSubExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Sub Experiment");
			$createSubExpBtn.click(function() {
				var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true);
				Util.blockUI("Select the type for the sub Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeDropdownCancel'>Cancel</a>");
				
				$("#sampleTypeDropdown").on("change", function(event) {
					var sampleTypeCode = $("#sampleTypeDropdown")[0].value;
					var argsMap = {
							"sampleTypeCode" : sampleTypeCode,
							"experimentIdentifier" : _this._experimentFormModel.experiment.identifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				
				$("#sampleTypeDropdownCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			});
			$formTitle.append(" ");
			$formTitle.append($createSubExpBtn);
			
			var $editBtn = $("<a>", { "class" : "btn btn-default"}).append("<span class='glyphicon glyphicon-edit'></span> Enable Editing");
			$editBtn.click(function() {
				mainController.changeView("showEditExperimentPageFromIdentifier", _this._experimentFormModel.experiment.identifier);
			});
			$formTitle.append(" ");
			$formTitle.append($editBtn);
		}
		
		//
		// Metadata Identification
		//
		var $identificationInfo = $('<div>').append($('<legend>').text("Identification Info"));
		$formColumn.append($identificationInfo);
		
		var identifierParts = this._experimentFormModel.experiment.identifier.split("/");
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Type", this._experimentFormModel.experiment.experimentTypeCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Project", identifierParts[0] + "/" + identifierParts[1] + "/" + identifierParts[2]));
		var $projectField = FormUtil._getInputField("text", null, "project", null, true);
		$projectField.val(identifierParts[0] + "/" + identifierParts[1] + "/" + identifierParts[2]);
		$projectField.hide();
		$formColumn.append($projectField);
		
		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._experimentFormModel.experiment.code));
			
			var $codeField = FormUtil._getInputField("text", null, "code", null, true);
			$codeField.val(identifierParts[3]);
			$codeField.hide();
			$formColumn.append($codeField);
			
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registrator", this._experimentFormModel.experiment.registrationDetails.userId));
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registration Date", (new Date(this._experimentFormModel.experiment.registrationDetails.registrationDate)).toLocaleString()));
		} else if(this._experimentFormModel.mode === FormMode.CREATE){
			var $codeField = FormUtil._getInputField("text", null, "code", null, true);
			$codeField.change(function() {
				_this._experimentFormModel.experiment.code = $(this).val();
			})
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($codeField, "Code"));
		}
		
		//
		// Metadata Fields
		//
		var experimentType = mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		
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
				
				if(this._experimentFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, this._experimentFormModel.experiment.properties[propertyType.code]);
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					var propertyTypeCode = null;
					//Update values if is into edit mode
					if(this._experimentFormModel.mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							$($component.children()[0]).prop('checked', this._experimentFormModel.experiment.properties[propertyType.code] === "true");
							propertyTypeCode = propertyType.code;
						} else if(propertyType.dataType === "TIMESTAMP") {
							var value = this._experimentFormModel.experiment.properties[propertyType.code];
							$($($component.children()[0]).children()[0]).val(value);
							propertyTypeCode = propertyType.code;
						} else {
							var value = this._experimentFormModel.experiment.properties[propertyType.code];
							propertyTypeCode = propertyType.code;
							if(!value && propertyType.code.charAt(0) === '$') {
								value = this._experimentFormModel.experiment.properties[propertyType.code.substr(1)];
								propertyTypeCode = propertyType.code.substr(1);
							}
							$component.val(value);
						}
					}
					
					var changeEvent = function(propertyType, propertyTypeCode) {
						return function() {
							_this._experimentFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = field.children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = timeValue;
							} else {
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
							}
						}
					}
					
					$component.change(changeEvent(propertyType, propertyTypeCode));
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				
				$fieldset.append($controlGroup);
			}
			
			$formColumn.append($fieldset);
		}
		
		if(this._experimentFormModel.mode === FormMode.EDIT || this._experimentFormModel.mode === FormMode.CREATE) {
			var label = "";
			
			if(this._experimentFormModel.mode === FormMode.EDIT) {
				label = "Update Experiment " + this._experimentFormModel.experiment.code;
			} else if(this._experimentFormModel.mode === FormMode.CREATE) {
				label = "Create Experiment";
			}
			
			var $updateBtn = $("<a>", { "class" : "btn btn-primary"}).append(label);
			$updateBtn.click(function() {
				_this._experimentFormController.updateExperiment();
			});
			
			$formColumn.append($updateBtn);
		}
		
		$container.append($form);
		Util.unblockUI();
	}
}