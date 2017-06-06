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
	
	this.repaint = function(views) {
		var $container = views.content;
		var _this = this;
		
		var $form = $("<span>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		
		var $rightPanel = null;
		if(this._experimentFormModel.mode === FormMode.VIEW) {
			$rightPanel = views.auxContent;
		}
		
		$form.append($formColumn);
		
		//
		// Title
		//
		var $formTitle = $("<div>");
		var nameLabel = this._experimentFormModel.experiment.properties[profile.propertyReplacingCode];
		if(!nameLabel) {
			nameLabel = this._experimentFormModel.experiment.code;
		}
		var entityPath = null;
		
		var typeTitle = "" + ELNDictionary.getExperimentKindName(this._experimentFormModel.experiment.identifier) + ": ";
		
		var title = null;
		switch(this._experimentFormModel.mode) {
	    	case FormMode.CREATE:
	    		title = "Create " + typeTitle + this._experimentFormModel.experiment.experimentTypeCode;
	    		entityPath = "";
	    		break;
	    	case FormMode.EDIT:
	    		title = "Update " + typeTitle + nameLabel;
	    		entityPath = this._experimentFormModel.experiment.identifier;
	    		break;
	    	case FormMode.VIEW:
	    		title = typeTitle + nameLabel;
	    		entityPath = this._experimentFormModel.experiment.identifier;
	    		break;
		}
		
		$formTitle
			.append($("<h2>").append(title))
			.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._experimentFormModel.mode === FormMode.VIEW) {
			//Create Experiment Step
			if(profile.getSampleTypeForSampleTypeCode("EXPERIMENTAL_STEP") && !profile.isSampleTypeHidden("EXPERIMENTAL_STEP")) {
				var $createBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
					var argsMap = {
							"sampleTypeCode" : "EXPERIMENTAL_STEP",
							"experimentIdentifier" : _this._experimentFormModel.experiment.identifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					Util.unblockUI();
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				toolbarModel.push({ component : $createBtn, tooltip: "Create Experimental Step" });
			}
			
			//Edit
			var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
				mainController.changeView("showEditExperimentPageFromIdentifier", _this._experimentFormModel.experiment.identifier);
			});
			toolbarModel.push({ component : $editBtn, tooltip: "Edit" });
			
			//Delete
			var $deleteBtn = FormUtil.getDeleteButton(function(reason) {
				_this._experimentFormController.deleteExperiment(reason);
			}, true);
			toolbarModel.push({ component : $deleteBtn, tooltip: "Delete" });
			
			//Create Dataset
			var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-upload", function () {
				mainController.changeView('showCreateDataSetPageFromExpPermId',_this._experimentFormModel.experiment.permId);
			});
			toolbarModel.push({ component : $uploadBtn, tooltip: "Upload Dataset" });

			//Get dropbox folder name
			var $uploadBtn = FormUtil.getButtonWithIcon("glyphicon-circle-arrow-up", (function () {
				var splitExperimentIdentifier = _this._experimentFormModel.experiment.identifier.split("/");
				var space = splitExperimentIdentifier[1];
				var project = splitExperimentIdentifier[2];
				var nameElements = [
					"E",
					space,
					project,
					_this._experimentFormModel.experiment.code,
				];
				FormUtil.showDropboxFolderNameDialog(nameElements);
			}).bind(this));
			toolbarModel.push({ component : $uploadBtn, tooltip: "Name generator for dataset upload with dropbox" });

			//Export
			var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
				Util.blockUI();
				var facade = mainController.serverFacade;
				facade.exportAll([{ type: "EXPERIMENT", permId : _this._experimentFormModel.experiment.permId, expand : true }], false, function(error, result) {
					if(error) {
						Util.showError(error);
					} else {
						Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
					}
				});
			});
			toolbarModel.push({ component : $export, tooltip: "Export" });
		} else { //Create and Edit
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._experimentFormController.updateExperiment();
			}, "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		var $header = views.header;
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));
		
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
		if(experimentType.propertyTypeGroups) {
			for(var i = 0; i < experimentType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = experimentType.propertyTypeGroups[i];
				this._paintPropertiesForSection($formColumn, propertyTypeGroup, i);
			}
		}
		
		//Sample List Container
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			$formColumn.append($("<legend>").append(ELNDictionary.Samples));
			var sampleListHeader = $("<div>");
			var sampleListContainer = $("<div>");
			$formColumn.append(sampleListHeader);
			$formColumn.append(sampleListContainer);
			var views = {
					header : sampleListHeader,
					content : sampleListContainer
			}
			var sampleList = new SampleTableController(this._experimentFormController, null, this._experimentFormModel.experiment.identifier, null, null, this._experimentFormModel.experiment);
			sampleList.init(views);
		}
		
		//
		// Identification Info on not Create
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			this._paintIdentificationInfo($formColumn);
		}
		
		//
		// PREVIEW IMAGE
		//
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			var $previewImage = $("<img>", { 'data-preview-loaded' : 'false',
											 'class' : 'zoomableImage',
											 'id' : 'preview-image',
											 'src' : './img/image_loading.gif',
											 'style' : 'max-width:100%; display:none;'
											});
			$previewImage.click(function() {
				Util.showImage($("#preview-image").attr("src"));
			});
			
			if($rightPanel !== null) { //Min Desktop resolution
				$rightPanel.append($previewImage);
			} else {
				$formColumn.append($previewImage);
			}
		}
		
		//
		// DATASETS
		//
		
		// Viewer
		var $dataSetViewerContainer = $("<div>", { 'id' : 'dataSetViewerContainer', 'style' : 'margin-top:10px;'});
		// Uploader
		var $dataSetUploaderContainer = $("<div>");
		
		if($rightPanel) {
			$rightPanel.append($dataSetViewerContainer);
			$rightPanel.append($dataSetUploaderContainer);
		} else {
			$formColumn.append($dataSetViewerContainer);
			$formColumn.append($dataSetUploaderContainer);
		}
		
		//
		// INIT
		//
		$container.append($form);
		
		Util.unblockUI();
		
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : this._experimentFormModel.experiment.permId } };
			var experimentCriteria = { entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules };
			mainController.serverFacade.searchForExperimentsAdvanced(experimentCriteria, null, function(data) {
				// Viewer
				_this._experimentFormModel.dataSetViewer = new DataSetViewerController("dataSetViewerContainer", profile, data.objects[0], mainController.serverFacade, profile.getDefaultDataStoreURL(), null, false, true);
				_this._experimentFormModel.dataSetViewer.init();
				if(_this._experimentFormModel.mode === FormMode.VIEW) {
					// Uploader
					var $dataSetFormController = new DataSetFormController(_this, FormMode.CREATE, data.objects[0], null, true);
					var viewsForDS = {
							content : $dataSetUploaderContainer
					}
					$dataSetFormController.init(viewsForDS);
				}
			});
		}
		
		if(this._experimentFormModel.mode !== FormMode.CREATE) {
			//Preview image
			this._reloadPreviewImage();
		}
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
				_this._experimentFormModel.isFormDirty = true;
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
					_this._experimentFormModel.isFormDirty = true;
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
			FormUtil.fixStringPropertiesForForm(propertyType, this._experimentFormModel.experiment);
			
			if(!propertyType.showInEditViews && this._experimentFormController.mode === FormMode.EDIT) { //Skip
				continue;
			} else if(propertyType.dinamic && this._experimentFormController.mode === FormMode.CREATE) { //Skip
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
				if(propertyType.code === "SHOW_IN_PROJECT_OVERVIEW") {
					if(!(profile.inventorySpaces.length > 0 && $.inArray(this._experimentFormModel.experiment.identifier.split("/")[1], profile.inventorySpaces) === -1)) {
						continue;
					}
				}
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
					var $component = FormUtil.getFieldForPropertyType(propertyType, value);
					//Update values if is into edit mode
					if(this._experimentFormModel.mode === FormMode.EDIT) {
						if(propertyType.dataType === "BOOLEAN") {
							$($($component.children()[0]).children()[0]).prop('checked', value === "true");
						} else if(propertyType.dataType === "TIMESTAMP") {
						} else {
							$component.val(value);
						}
					} else {
						$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
					}
						
					var changeEvent = function(propertyType) {
						return function(jsEvent, newValue) {
							var propertyTypeCode = null;
							propertyTypeCode = propertyType.code;
							_this._experimentFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = $(field.children()[0]).children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._experimentFormModel.experiment.properties[propertyTypeCode] = timeValue;
							} else {
								if(newValue !== undefined && newValue !== null) {
									_this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(newValue);
								} else {
									_this._experimentFormModel.experiment.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
								}
							}
						}
					}
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}
					
					if(propertyType.dataType === "MULTILINE_VARCHAR") {
						$component = FormUtil.activateRichTextProperties($component, changeEvent(propertyType), propertyType);
					} else if(propertyType.dataType === "TIMESTAMP") {
						$component.on("dp.change", changeEvent(propertyType));
					} else {
						$component.change(changeEvent(propertyType));
					}
					
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
	
	//
	// Preview Image
	//
	this._reloadPreviewImage = function() {
		var _this = this;
		var previewCallback =  function(data) {
				if (data.objects.length == 0) {
					_this._updateLoadingToNotAvailableImage();
				} else {
					var listFilesForDataSetCallback = function(dataFiles) {
							var found = false;
							if(!dataFiles.result) {
								//DSS Is not running probably
							} else {
								for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
									if(!dataFiles.result[pathIdx].isDirectory) {
										var elementId = 'preview-image';
										var downloadUrl = profile.getDefaultDataStoreURL() + '/' + data.objects[0].code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
										
										var img = $("#" + elementId);
										img.attr('src', downloadUrl);
										img.attr('data-preview-loaded', 'true');
										img.show();
										break;
									}
								}
							}
					};
					mainController.serverFacade.listFilesForDataSet(data.objects[0].code, "/", true, listFilesForDataSetCallback);
				}
		};
		
		var datasetRules = { "UUIDv4.1" : { type : "Experiment", name : "ATTR.PERM_ID", value : this._experimentFormModel.experiment.permId },
							 "UUIDv4.2" : { type : "Attribute", name : "DATA_SET_TYPE", value : "ELN_PREVIEW" },
							 "UUIDv4.3" : { type : "Sample", name : "NULL.NULL", value : "NULL" }
							 };
		
    	mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, previewCallback);
	}
	
	this._updateLoadingToNotAvailableImage = function() {
		var notLoadedImages = $("[data-preview-loaded='false']");
		notLoadedImages.attr('src', "./img/image_unavailable.png");
	}
}