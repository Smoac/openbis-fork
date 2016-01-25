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
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.updateExperiment();'
		});
		
		$form.append($formColumn);
		
		//
		// Title
		//
		var $formTitle = $("<div>");
		var nameLabel = this._experimentFormModel.experiment.properties[profile.propertyReplacingCode];
		var entityPath = this._experimentFormModel.experiment.identifier;
		var isName = (nameLabel)?true:false;
		
		var title = null;
		switch(this._experimentFormModel.mode) {
	    	case FormMode.CREATE:
	    		title = "Create Experiment " + this._experimentFormModel.experiment.experimentTypeCode;
	    		break;
	    	case FormMode.EDIT:
	    		title = "Update Experiment ";
	    		break;
	    	case FormMode.VIEW:
	    		title = "Experiment ";
	    		break;
		}
		
		if(isName) {
			title += nameLabel;
			$formTitle
				.append($("<h2>").append(title))
				.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		} else {
			if(this._experimentFormModel.mode !== FormMode.CREATE) {
				title += entityPath;
			}
			$formTitle
				.append($("<h2>").append(title));
		}
		$formColumn.append($formTitle);
		$formColumn.append($("<br>"));
		
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			//Delete
			$formTitle.append("&nbsp;");
			$formTitle.append(FormUtil.getDeleteButton(function(reason) {
				_this._experimentFormController.deleteExperiment(reason);
			}, true));
		}
		
		//
		// Create Sub Experiment
		//
		if(this._experimentFormModel.mode === FormMode.VIEW) {
			var $editBtn = $("<a>", { "class" : "btn btn-default"}).append("<span class='glyphicon glyphicon-edit'></span> Enable Editing");
			$editBtn.click(function() {
				mainController.changeView("showEditExperimentPageFromIdentifier", _this._experimentFormModel.experiment.identifier);
			});
			$formTitle.append("&nbsp;");
			$formTitle.append($editBtn);
		}
		
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			//Add Experiment Step
			if(profile.getSampleTypeForSampleTypeCode("EXPERIMENTAL_STEP") && !profile.isSampleTypeHidden("EXPERIMENTAL_STEP")) {
				$formTitle.append("&nbsp;");
				$formTitle.append(FormUtil.getButtonWithText("Create Exp. Step", function() {
					var argsMap = {
							"sampleTypeCode" : "EXPERIMENTAL_STEP",
							"experimentIdentifier" : _this._experimentFormModel.experiment.identifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					Util.unblockUI();
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				}));
			}
		}
		
		//
		// Identification Info on Create
		//
		if(this._experimentFormModel.mode === FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn);
		}
		
		//
		// Form Defined Properties from General Section
		//
		var experimentType = mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		for(var i = 0; i < experimentType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = experimentType.propertyTypeGroups[i];
			this._paintPropertiesForSection($formColumn, propertyTypeGroup, i);
		}
		
		//Create/Update Buttons
		if(this._experimentFormModel.mode === FormMode.EDIT || this._experimentFormModel.mode === FormMode.CREATE) {
			var $updateBtn = $("<input>", { "type": "submit", "class" : "btn btn-primary", 'value' : title });
			$formColumn.append($updateBtn);
		}
		
		//
		// Identification Info on Create
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn);
		}
		
		//Sample List Container
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			$formColumn.append($("<legend>").append("Samples"));
			var sampleListContainer = $("<div>");
			$formColumn.append(sampleListContainer);
			var sampleList = new SampleTableController(this._experimentFormController, null, this._experimentFormModel.experiment.identifier);
			sampleList.init(sampleListContainer);
		}
		
		$container.append($form);
		FormUtil.activateRichTextProperties();
		
		Util.unblockUI();
	}
	
	this._paintIdentificationInfo = function($formColumn) {
		var _this = this;
		
		var $identificationInfo = $('<div>').append($('<legend>').text("Identification Info"));
		$formColumn.append($identificationInfo);
		
		var identifierParts = this._experimentFormModel.experiment.identifier.split("/");
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Type", this._experimentFormModel.experiment.experimentTypeCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Project", "/" + identifierParts[1] + "/" + identifierParts[2]));
		var $projectField = FormUtil._getInputField("text", null, "project", null, true);
		$projectField.val("/" + identifierParts[1] + "/" + identifierParts[2]);
		$projectField.hide();
		$formColumn.append($projectField);
		
		if(this._experimentFormModel.mode === FormMode.VIEW || this._experimentFormModel.mode === FormMode.EDIT) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._experimentFormModel.experiment.code));
			
			var $codeField = FormUtil._getInputField("text", null, "code", null, true);
			$codeField.val(identifierParts[3]);
			$codeField.hide();
			$formColumn.append($codeField);
		} else if(this._experimentFormModel.mode === FormMode.CREATE) {
			var $codeField = FormUtil._getInputField("text", null, "code", null, true);
			$codeField.keyup(function() {
				var caretPosition = this.selectionStart;
				$(this).val($(this).val().toUpperCase());
				this.selectionStart = caretPosition;
				this.selectionEnd = caretPosition;
				_this._experimentFormModel.experiment.code = $(this).val();
				
				//Full Identifier
				var currentIdentifier = _this._experimentFormModel.experiment.identifier.split("/");
				var experimentIdentifier = "/" + currentIdentifier[1] + "/" + currentIdentifier[2] + "/" + _this._experimentFormModel.experiment.code;
				_this._experimentFormModel.experiment.identifier = experimentIdentifier;
			})
			var $codeFieldRow = FormUtil.getFieldForComponentWithLabel($codeField, "Code");
			$formColumn.append($codeFieldRow);
			
			mainController.serverFacade.getProjectFromIdentifier($projectField.val(), function(project) {
				delete project["@id"];
				delete project["@type"];
				mainController.serverFacade.listExperiments([project], function(data) {
					var autoGeneratedCode = identifierParts[2] + "_EXP_" + (data.result.length + 1);
					$codeField.val(autoGeneratedCode);
					_this._experimentFormModel.experiment.code = autoGeneratedCode;
					
					//Full Identifier
					var currentIdentifier = _this._experimentFormModel.experiment.identifier.split("/");
					var experimentIdentifier = "/" + currentIdentifier[1] + "/" + currentIdentifier[2] + "/" + _this._experimentFormModel.experiment.code;
					_this._experimentFormModel.experiment.identifier = experimentIdentifier;
				});
			});
		}
		
		//
		// Registration and modification info
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._experimentFormModel.experiment.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$formColumn.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)))
			$formColumn.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$formColumn.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$formColumn.append($modificationDate);
		}
	}
	
	this._paintPropertiesForSection = function($formColumn, propertyTypeGroup, i) {
		var _this = this;
		var experimentType = mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		
		var $fieldset = $('<div>');
		var $legend = $('<legend>'); 
		$fieldset.append($legend);
		
		if((propertyTypeGroup.name !== null) && (propertyTypeGroup.name !== "")) {
			$legend.text(propertyTypeGroup.name);
		} else if((i === 0) || ((i !== 0) && (experimentType.propertyTypeGroups[i-1].name !== null) && (experimentType.propertyTypeGroups[i-1].name !== ""))) {
			$legend.text("Metadata");
		} else {
			$legend.remove();
		}
		
		var propertyGroupPropertiesOnForm = 0;
		for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
			var propertyType = propertyTypeGroup.propertyTypes[j];
			if(!propertyType.showInEditViews && this._experimentFormController.mode === FormMode.EDIT) { //Skip
				continue;
			}
			
			if(propertyType.code === "XMLCOMMENTS") {
				var $commentsContainer = $("<div>");
				$fieldset.append($commentsContainer);
				var isAvailable = this._experimentFormController._addCommentsWidget($commentsContainer);
				if(!isAvailable) {
					continue;
				}
			} else {
				var $controlGroup =  null;
				
				var value = this._experimentFormModel.experiment.properties[propertyType.code];
				if(!value && propertyType.code.charAt(0) === '$') {
					value = this._experimentFormModel.experiment.properties[propertyType.code.substr(1)];
					this._experimentFormModel.experiment.properties[propertyType.code] = value;
					delete this._experimentFormModel.experiment.properties[propertyType.code.substr(1)];
				}
				
				if(this._experimentFormModel.mode === FormMode.VIEW) { //Show values without input boxes if the form is in view mode
					if(Util.getEmptyIfNull(value) !== "") { //Don't show empty fields, whole empty sections will show the title
						if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
							value = FormUtil.getVocabularyLabelForTermCode(propertyType, value);
						}
						$controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, value);
					} else {
						continue;
					}
				} else {
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					//Update values if is into edit mode
					if(this._experimentFormModel.mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							$($component.children()[0]).prop('checked', value === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
							$($($component.children()[0]).children()[0]).val(value);
						} else {
							$component.val(value);
						}
					} else {
						$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
					}
						
					var changeEvent = function(propertyType) {
						return function() {
							var propertyTypeCode = null;
							propertyTypeCode = propertyType.code;
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
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}
					
					$component.change(changeEvent(propertyType));
					$controlGroup = FormUtil.getFieldForComponentWithLabel($component, propertyType.label);
				}
				
				$fieldset.append($controlGroup);
			}
			propertyGroupPropertiesOnForm++;
		}
		
		if(propertyGroupPropertiesOnForm === 0) {
			$legend.remove();
		}
		
		$formColumn.append($fieldset);
	}
}