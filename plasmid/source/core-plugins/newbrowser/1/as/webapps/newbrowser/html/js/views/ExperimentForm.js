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
	this._isFormDirty = false;
	
	this.isDirty = function() {
		return this._isFormDirty;
	}
	
	this.init = function() {
		this.repaint();
		Util.unblockUI();
	}
	
	this.repaint = function() {
		var _this = this;
		$("#" + this._containerId).empty();
		
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
			
			var $editBtn = $("<a>", { "class" : "btn btn-default"}).append("<span class='glyphicon glyphicon-edit'></span> Enable Editing");
			$editBtn.click(function() {
				_this._mainController.changeView("showEditExperimentPageFromIdentifier", _this._experiment.identifier);
			});
			$formTitle.append(" ");
			$formTitle.append($editBtn);
		}
		
		//
		// Metadata Identification
		//
		var $identificationInfo = $('<div>').append($('<legend>').text("Identification Info"));
		$formColumn.append($identificationInfo);
		
		var identifierParts = this._experiment.identifier.split("/");
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Type", this._experiment.experimentTypeCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Project", identifierParts[0] + "/" + identifierParts[1] + "/" + identifierParts[2]));
		var $projectField = FormUtil._getInputField("text", "PROJECT", "project", null, true);
		$projectField.val(identifierParts[0] + "/" + identifierParts[1] + "/" + identifierParts[2]);
		$projectField.hide();
		$formColumn.append($projectField);
		
		if(this._mode === FormMode.VIEW || this._mode === FormMode.EDIT) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", identifierParts[3]));
			
			var $codeField = FormUtil._getInputField("text", "CODE", "code", null, true);
			$codeField.val(identifierParts[3]);
			$codeField.hide();
			$formColumn.append($codeField);
			
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registrator", this._experiment.registrationDetails.userId));
			$formColumn.append(FormUtil.getFieldForLabelWithText("Registration Date", (new Date(this._experiment.registrationDetails.registrationDate)).toLocaleString()));
		} else if(this._mode === FormMode.CREATE){
			var $codeField = FormUtil._getInputField("text", "CODE", "code", null, true);
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
				
				if(this._mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, this._experiment.properties[propertyType.code]);
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					
					//Update values if is into edit mode
					if(this._mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							$($component.children()[0]).prop('checked', this._experiment.properties[propertyType.code] === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
							var value = this._experiment.properties[propertyType.code];
							$($($component.children()[0]).children()[0]).val(value);
						} else {
							var value = this._experiment.properties[propertyType.code];
							if(!value && propertyType.code.charAt(0) === '$') {
								value = this._experiment.properties[propertyType.code.substr(1)];
							}
							$component.val(value);
						}
					}
					
					$component.change(function(event) {
						_this._isFormDirty = true;
					});
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				
				$fieldset.append($controlGroup);
			}
			
			$formColumn.append($fieldset);
		}
		
		if(this._mode === FormMode.EDIT || this._mode === FormMode.CREATE) {
			var label = "";
			
			if(this._mode === FormMode.EDIT) {
				label = "Update Experiment " + this._experiment.code;
			} else if(this._mode === FormMode.CREATE) {
				label = "Create Experiment";
			}
			
			var $submitButton = $('<fieldset>')
									.append($('<div>', { class : "form-group"}))
									.append($('<div>', {class: FormUtil.controlColumnClass})
									.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : label})));
			
			$formColumn.append($submitButton);
		}
		
		$("#" + this._containerId).append($form);
	}
	
	this._updateExperiment = function() {
		Util.blockUI();
		
		var experimentType = this._mainController.profile.getExperimentTypeForExperimentTypeCode(this._experiment.experimentTypeCode);
		
		//Identification Info
		var projectIdentifier = $("#PROJECT").val().split("/");
		var experimentSpace = projectIdentifier[1];
		var experimentProject = projectIdentifier[2];
		var experimentCode = $("#CODE").val();
		var experimentIdentifier = "/" + experimentSpace + "/" + experimentProject + "/" + experimentCode;
		//Properties
		var experimentProperties = {};
		
		for(var i = 0; i < experimentType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = experimentType.propertyTypeGroups[i];
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				var value = null;
				
				if (propertyType.dataType === "BOOLEAN") {
					value = $("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')+":checked").val() === "on";
				} else {
					value = Util.getEmptyIfNull($("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')).val());
				}
				
				experimentProperties[propertyType.code] = value;
			}
		}
		
		var method = "";
		if(this._mode === FormMode.CREATE) {
			method = "insertExperiment";
		} else if(this._mode === FormMode.EDIT) {
			method = "updateExperiment";
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"experimentType" : this._experiment.experimentTypeCode,
				"experimentIdentifier" : experimentIdentifier,
				"experimentSpace" : experimentSpace,
				"experimentProject" : experimentProject,
				"experimentCode" : experimentCode,
				//Properties
				"experimentProperties" : experimentProperties
		};
		
		var _this = this;
		
		if(this._mainController.profile.allDataStores.length > 0) {
			this._mainController.serverFacade.createReportFromAggregationService(this._mainController.profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var experimentType = profile.getExperimentTypeForExperimentTypeCode(_this._experiment.experimentTypeCode);
					var experimentTypeDisplayName = experimentType.description;
					if(!experimentTypeDisplayName) {
						experimentTypeDisplayName = _this._experiment.experimentTypeCode;
					}
					
					var message = "";
					if(_this._mode === FormMode.CREATE) {
						message = "Created.";
					} else if(_this._mode === FormMode.EDIT) {
						message = "Updated.";
					}
					
					var callbackOk = function() {
						_this._mainController.sideMenu.refreshExperiment($("#PROJECT").val(), experimentCode);
						_this._isFormDirty = false;
						_this._mainController.changeView("showExperimentPageFromIdentifier", experimentIdentifier);
						Util.unblockUI();
					}
					
					Util.showSuccess(experimentTypeDisplayName + " " + message, callbackOk);
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
				
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}