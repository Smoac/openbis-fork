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

function DataSetFormView(dataSetFormController, dataSetFormModel) {
	this._dataSetFormController = dataSetFormController;
	this._dataSetFormModel = dataSetFormModel;
	
	this.repaint = function(views) {
		var $container = views.content;
		var _this = this;
		
		//Clean and prepare container
		var $wrapper = $('<form>', { 'id' : 'mainDataSetForm', 'role' : 'form'});
		if(this._dataSetFormModel.isMini) {
			$wrapper.css('margin', '10px');
			$wrapper.css('padding', '10px');
			$wrapper.css('background-color', '#f8f8f8');
		}
		$wrapper.submit(function(event) {_this._dataSetFormController.submitDataSet(); event.preventDefault();});
		
		//
		// Title
		//
		var spaceCode;
		var projectCode;
		var experimentCode;
		
		var experimentIdentifier = null;
		if(this._dataSetFormModel.isExperiment()) {
			experimentIdentifier = this._dataSetFormModel.entity.identifier.identifier;
		} else { //Both Sample and Experiment exist
			experimentIdentifier = this._dataSetFormModel.entity.experimentIdentifierOrNull;
		}
		if(experimentIdentifier) {
			spaceCode = experimentIdentifier.split("/")[1];
			projectCode = experimentIdentifier.split("/")[2];
			experimentCode = experimentIdentifier.split("/")[3];
		}
		var sampleCode;
		var sampleIdentifier;
		if(!this._dataSetFormModel.isExperiment()) {
			sampleCode = this._dataSetFormModel.entity.code;
			spaceCode = this._dataSetFormModel.entity.identifier.split("/")[1];
			sampleIdentifier = this._dataSetFormModel.entity.identifier;
		}
		var datasetCodeAndPermId = this._dataSetFormModel.dataSet.code;
		var entityPath = FormUtil.getFormPath(spaceCode, projectCode, experimentCode, null, null, sampleCode, sampleIdentifier, datasetCodeAndPermId);
		
		var nameLabel = this._dataSetFormModel.dataSet.properties[profile.propertyReplacingCode];
		if(!nameLabel) {
			nameLabel = this._dataSetFormModel.dataSet.code;
		}
		
		var titleText = null;
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			titleText = 'Create Dataset';
		} else if(this._dataSetFormModel.mode === FormMode.EDIT) {
			titleText = 'Update Dataset: ' + nameLabel;
		} else if(this._dataSetFormModel.mode === FormMode.VIEW) {
			titleText = 'Dataset: ' + nameLabel;
		}
		
		var $title = $('<div>');
		$title
			.append($("<h2>").append(titleText))
			.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._dataSetFormModel.mode === FormMode.VIEW && !this._dataSetFormModel.isMini) {
			//Edit Button
			var $editBtn = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
				mainController.changeView('showEditDataSetPageFromPermId', _this._dataSetFormModel.dataSet.code);
			});
			toolbarModel.push({ component : $editBtn, tooltip: "Edit" });
			
			//Delete Button
			var $deleteBtn = FormUtil.getDeleteButton(function(reason) {
				_this._dataSetFormController.deleteDataSet(reason);
			}, true);
			toolbarModel.push({ component : $deleteBtn, tooltip: "Delete" });
			
			//Export
			var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
				Util.blockUI();
				var facade = mainController.serverFacade;
				facade.exportAll([{ type: "DATASET", permId : _this._dataSetFormModel.dataSet.code, expand : true }], false, function(error, result) {
					if(error) {
						Util.showError(error);
					} else {
						Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
					}
				});
			});
			toolbarModel.push({ component : $export, tooltip: "Export" });
			
			//Jupyter Button
			if(profile.jupyterIntegrationServerEndpoint) {
				var $jupyterBtn = FormUtil.getButtonWithIcon("glyphicon-log-in", function () {
					var jupyterNotebook = new JupyterNotebookController(_this._dataSetFormModel.dataSet);
					jupyterNotebook.init();
				});
				toolbarModel.push({ component : $jupyterBtn, tooltip: "Create Jupyter notebook" });
			}
		} else if(!this._dataSetFormModel.isMini) {
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._dataSetFormController.submitDataSet();
			}, "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		if(!this._dataSetFormModel.isMini) {
			var $header = views.header;
			$header.append($title);
			$header.append(FormUtil.getToolbar(toolbarModel));
		}
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<div>');
		if(!this._dataSetFormModel.isMini) {
			$dataSetTypeFieldSet.append($('<legend>').text('Identification Info'));
		}
		$wrapper.append($dataSetTypeFieldSet);
		
		var $dataSetTypeSelector = null;
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			$dataSetTypeSelector = FormUtil.getDataSetsDropDown('DATASET_TYPE', this._dataSetFormModel.dataSetTypes);
			$dataSetTypeSelector.change(function() { 
				if(!_this._dataSetFormModel.isMini) {
					_this._repaintMetadata(
							_this._dataSetFormController._getDataSetType($('#DATASET_TYPE').val())
					);
				}
				_this.isFormDirty = true;
			});
			
			var $dataSetTypeDropDown = $('<div>', { class : 'form-group' });
			if(!this._dataSetFormModel.isMini) {
				$dataSetTypeDropDown.append($('<label>', {class: "control-label"}).html('Data Set Type&nbsp;(*):'));
			}
			
			var $dataSetTypeDropDowContainer = $('<div>');
			if(this._dataSetFormModel.isMini) {
				$dataSetTypeDropDowContainer.css('width', '100%');
			}
			$dataSetTypeDropDown.append(
				$dataSetTypeDropDowContainer.append($dataSetTypeSelector)
			);
			$dataSetTypeFieldSet.append($dataSetTypeDropDown);
		} else {
			var $dataSetTypeLabel = FormUtil.getFieldForLabelWithText('Data Set Type', this._dataSetFormModel.dataSet.dataSetTypeCode, "CODE");
			$dataSetTypeFieldSet.append($dataSetTypeLabel);
			var $dataSetCodeLabel = FormUtil.getFieldForLabelWithText('Code', this._dataSetFormModel.dataSet.code, null);
			$dataSetTypeFieldSet.append($dataSetCodeLabel);
			
			var datasetParents = "N/A";
			for(var pIdx = 0; pIdx < this._dataSetFormModel.dataSet.parentCodes.length; pIdx++) {
				if(pIdx === 0) {
					datasetParents = this._dataSetFormModel.dataSet.parentCodes[pIdx];
				} else {
					datasetParents += ", " + this._dataSetFormModel.dataSet.parentCodes[pIdx];
				}
			}
			
			var $dataSetParentsCodeLabel = FormUtil.getFieldForLabelWithText('Parents', datasetParents, null);
			$dataSetTypeFieldSet.append($dataSetParentsCodeLabel);
		}
		
		var ownerName = null;
		var owner = null;
		if(this._dataSetFormModel.isExperiment()) { 
			ownerName = ELNDictionary.ExperimentELN; //Only experiments on the ELN have datasets
			owner = this._dataSetFormModel.entity.identifier.identifier;
		} else {
			ownerName = ELNDictionary.Sample;
			if(this._dataSetFormModel.entity.experimentIdentifierOrNull) {
				owner = this._dataSetFormModel.entity.experimentIdentifierOrNull + "/" + this._dataSetFormModel.entity.code;
			} else {
				owner = this._dataSetFormModel.entity.identifier;
			}
		}
		
		if(!this._dataSetFormModel.isMini) {
			$dataSetTypeFieldSet.append(FormUtil.getFieldForLabelWithText(ownerName, owner));
		}
		
		//
		// Registration and modification info
		//
		if(this._dataSetFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._dataSetFormModel.dataSet.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$dataSetTypeFieldSet.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", Util.getFormatedDate(new Date(registrationDetails.registrationDate)))
			$dataSetTypeFieldSet.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$dataSetTypeFieldSet.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", Util.getFormatedDate(new Date(registrationDetails.modificationDate)));
			$dataSetTypeFieldSet.append($modificationDate);
		}
		
		//Metadata Container
		$wrapper.append($('<div>', { 'id' : 'metadataContainer'}));
		
		//Attach File
		$wrapper.append($('<div>', { 'id' : 'APIUploader' } ));
		
		$wrapper.append($('<div>', { 'id' : 'fileOptionsContainer' } ));
		
		//Show Files
		var filesViewer = $('<div>', { 'id' : 'filesViewer' } );
		$wrapper.append(filesViewer);
		
		//Submit Button
		if(this._dataSetFormModel.mode !== FormMode.VIEW) {
			if(_this._dataSetFormModel.isMini) {
				var btnText = "";
				if(this._dataSetFormModel.mode === FormMode.CREATE) {
					btnText = 'Create';
				} else if(this._dataSetFormModel.mode === FormMode.EDIT) {
					btnText = 'Update';
				}
				
				var $submitButton = $('<fieldset>')
				.append($('<div>', { class : "form-group"}))
				.append($('<div>')
							.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : btnText})));
				
				$wrapper.append($submitButton);
				
				var $autoUploadCheck = FormUtil._getBooleanField(null, 'Auto upload on drop');
					$($autoUploadCheck.children()[0]).children()[0].checked = _this._dataSetFormModel.isAutoUpload;
				
					$autoUploadCheck.css("display","inline");
					$autoUploadCheck.css("padding-top", "2px");
					$autoUploadCheck.change(function(){
						var isChecked = $($(this).children()[0]).children()[0].checked;
						_this._dataSetFormModel.isAutoUpload = isChecked;
						mainController.serverFacade.setSetting("DataSetFormModel.isAutoUpload", isChecked);
					});
					
				var $autoUploadGroup = $('<fieldset>')
						.append($('<div>', { class : "form-group"}))
						.append($('<div>')
						.append($autoUploadCheck).append(" Auto upload on drop"));
				
				$wrapper.append($('<fieldset>').append($autoUploadGroup));
			}
		}
		
		//Attach to main form
		$container.append($('<div>', { class : 'row'}).append($('<div>', { class : FormUtil.formColumClass}).append($wrapper)));
		
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			//Initialize file chooser
			var onComplete = function(data) {
				_this._dataSetFormModel.files.push(data.name);
				if(!_this._dataSetFormModel.isMini){
					_this._updateFileOptions();
				}
				var dataSetTypeCode = profile.getDataSetTypeForFileName(_this._dataSetFormModel.files, data.name);
				if(dataSetTypeCode != null) {
					var selectedDataSetTypeCode = $("#DATASET_TYPE").val();
					if(selectedDataSetTypeCode !== dataSetTypeCode) {
						$("#DATASET_TYPE").val(dataSetTypeCode);
						if(!_this._dataSetFormModel.isMini){
							_this._repaintMetadata(
									_this._dataSetFormController._getDataSetType(dataSetTypeCode)
							);
						}
					}
				}
				
				if(_this._dataSetFormModel.isMini && !Uploader.uploadsInProgress() && _this._dataSetFormModel.isAutoUpload) {
					if($("#DATASET_TYPE").val()) {
						_this._dataSetFormController.submitDataSet();
					} else {
						var showSelectDatasetType = function() {
							var $dropdown = FormUtil.getDataSetsDropDown("datasetTypeForDataset", _this._dataSetFormModel.dataSetTypes);
							Util.blockUI("Select the type for the Dataset: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='datasetTypeForDatasetCancel'>Cancel</a>");
							
							$("#datasetTypeForDataset").on("change", function(event) {
								var datasetTypeCode = $("#datasetTypeForDataset")[0].value;
								$("#DATASET_TYPE").val(datasetTypeCode);
								_this._dataSetFormController._getDataSetType(datasetTypeCode);
								_this._dataSetFormController.submitDataSet();
							});
							
							$("#datasetTypeForDatasetCancel").on("click", function(event) { 
								Util.unblockUI();
							});
						}
						showSelectDatasetType();
					}
					
				}
			}
			
			var onDelete = function(data) {
				for(var i=0; _this._dataSetFormModel.files.length; i++) {
					if(_this._dataSetFormModel.files[i] === data.name) {
						_this._dataSetFormModel.files.splice(i, 1);
						break;
					}
				}
				if(!_this._dataSetFormModel.isMini){
					_this._updateFileOptions();
				}
			}
			
			if(this._dataSetFormModel.mode === FormMode.CREATE) {
				mainController.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), onComplete, {
					main_title : $('<legend>').text('Files Uploader'),
					uploads_title : $('<legend>').text('File list'),
					ondelete:onDelete,
					hideHint:_this._dataSetFormModel.isMini
				});
			}
		} else {
			var dataSetType = _this._dataSetFormController._getDataSetType(this._dataSetFormModel.dataSet.dataSetTypeCode);
			this._repaintMetadata(dataSetType);
		}
		
		if(this._dataSetFormModel.mode !== FormMode.CREATE) {
			var dataSetViewer = new DataSetViewerController("filesViewer", profile, this._dataSetFormModel.entity, mainController.serverFacade, profile.getDefaultDataStoreURL(), [this._dataSetFormModel.dataSet], false, true);
			dataSetViewer.init();
		}
	}
	
	this._updateFileOptions = function() {
		var _this = this;
		$wrapper = $("#fileOptionsContainer"); //Clean existing
		$wrapper.empty();
		
		if( this._dataSetFormModel.files.length > 1
			||
			this._dataSetFormModel.files.length === 1 && this._dataSetFormModel.files[0].indexOf('zip', this._dataSetFormModel.files[0].length - 3) !== -1) {
			var $legend = $('<div>').append($('<legend>').text('Files Options'));
			$wrapper.append($legend);
		}
		
		if(this._dataSetFormModel.files.length > 1) {
			var $textField = FormUtil._getInputField('text', 'folderName', 'Folder Name', null, true);
			$textField.change(function(event) {
				_this.isFormDirty = true;
			});
			
			var $folderName = $('<div>')
			.append($('<div>', { class : "form-group"})
					.append($('<label>', {class : 'control-label'}).html('Folder Name&nbsp;(*):'))
					.append($('<div>')
						.append($textField))
			);
			$wrapper.append($folderName);
		}
		
		if(this._dataSetFormModel.files.length === 1 && 
				this._dataSetFormModel.files[0].indexOf('zip', this._dataSetFormModel.files[0].length - 3) !== -1) {
			var isZipDirectoryUpload = profile.isZipDirectoryUpload($('#DATASET_TYPE').val());
			if(isZipDirectoryUpload === null) {
				var $fileFieldSetIsDirectory = $('<div>')
				.append($('<div>', { class : "form-group"})
							.append($('<label>', {class : 'control-label'}).text('Uncompress before import:'))
							.append($('<div>')
								.append(FormUtil._getBooleanField('isZipDirectoryUpload', 'Uncompress before import:')))
				);
				$wrapper.append($fileFieldSetIsDirectory);
				
				$("#isZipDirectoryUpload").change(function() {
					_this.isFormDirty = true;
					if($("#isZipDirectoryUpload"+":checked").val() === "on") {
						var $textField = FormUtil._getInputField('text', 'folderName', 'Folder Name', null, true);
						$textField.change(function(event) {
							_this.isFormDirty = true;
						});
						
						var $folderName = $('<div>', { "id" : "folderNameContainer"})
						.append($('<div>', { class : "form-group"})
								.append($('<label>', {class : 'control-label' }).html('Folder Name&nbsp;(*):'))
								.append($('<div>')
									.append($textField))
						);
						$("#fileOptionsContainer").append($folderName);
						$("#folderName").val(_this._dataSetFormModel.files[0].substring(0, _this._dataSetFormModel.files[0].indexOf(".")));
					} else {
						$( "#folderNameContainer" ).remove();
					}
				})
			}
		}
	}
	
	this._repaintMetadata = function(dataSetType) {
		var _this = this;
		$("#metadataContainer").empty();
		var $wrapper = $("<div>");
		
		for(var i = 0; i < dataSetType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = dataSetType.propertyTypeGroups[i];
			
			var $fieldset = $('<div>');
			var $legend = $('<legend>'); 
			$fieldset.append($legend);
			
			if(propertyTypeGroup.name) {
				$legend.text(propertyTypeGroup.name);
			} else if(dataSetType.propertyTypeGroups.length === 1) { //Only when there is only one group without name to render it with a default title.
				$legend.text("Metadata Fields");
			} else {
				$legend.remove();
			}
			
			var propertyGroupPropertiesOnForm = 0;
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				FormUtil.fixStringPropertiesForForm(propertyType, this._dataSetFormModel.dataSet);
				
				if(!propertyType.showInEditViews && this._dataSetFormController.mode === FormMode.EDIT) { //Skip
					continue;
				} else if(propertyType.dinamic && this._dataSetFormController.mode === FormMode.CREATE) { //Skip
					continue;
				}
				
				if(propertyType.code === "XMLCOMMENTS") {
					var $commentsContainer = $("<div>");
					$fieldset.append($commentsContainer);
					var isAvailable = this._dataSetFormController._addCommentsWidget($commentsContainer);
					if(!isAvailable) {
						continue;
					}
				} else {
					if(propertyType.code === "SHOW_IN_PROJECT_OVERVIEW") {
						var identifier = null;
						if(this._dataSetFormModel.isExperiment()) { 
							identifier = this._dataSetFormModel.entity.identifier.identifier;
						} else {
							identifier = this._dataSetFormModel.entity.identifier;
						}
						
						if(!(profile.inventorySpaces.length > 0 && $.inArray(identifier.split("/")[1], profile.inventorySpaces) === -1)) {
							continue;
						}
					}
					
					var value = "";
					if(this._dataSetFormModel.mode !== FormMode.CREATE) {
						value = this._dataSetFormModel.dataSet.properties[propertyType.code];
						if(!value && propertyType.code.charAt(0) === '$') {
							value = this._dataSetFormModel.dataSet.properties[propertyType.code.substr(1)];
							this._dataSetFormModel.dataSet.properties[propertyType.code] = value;
							delete this._dataSetFormModel.dataSet.properties[propertyType.code.substr(1)];
						}
					}
					
					if(this._dataSetFormModel.mode === FormMode.VIEW) {
						if(Util.getEmptyIfNull(value) !== "") { //Don't show empty fields, whole empty sections will show the title
							if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
								value = FormUtil.getVocabularyLabelForTermCode(propertyType, value);
							}
							var $controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, value, propertyType.code);
							$fieldset.append($controlGroup);
						} else {
							continue;
						}
					} else {
						var $controlGroup = $('<div>', {class : 'form-group'});
						var requiredStar = (propertyType.mandatory)?"&nbsp;(*)":"";				
						var $controlLabel = $('<label>', {'class' : "control-label" }).html(propertyType.label + requiredStar + ":");
						var $controls = $('<div>');
						
						$controlGroup.append($controlLabel);
						$controlGroup.append($controls);
						
						var $component = FormUtil.getFieldForPropertyType(propertyType, value);
						
						//Update model
						var changeEvent = function(propertyType) {
							return function(jsEvent, newValue) {
								var propertyTypeCode = null;
								propertyTypeCode = propertyType.code;
								_this._dataSetFormModel.isFormDirty = true;
								var field = $(this);
								if(propertyType.dataType === "BOOLEAN") {
									_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = $(field.children()[0]).children()[0].checked;
								} else if (propertyType.dataType === "TIMESTAMP") {
									var timeValue = $($(field.children()[0]).children()[0]).val();
									_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = timeValue;
								} else {
									if(newValue !== undefined && newValue !== null) {
										_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = Util.getEmptyIfNull(newValue);
									} else {
										_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
									}
								}
							}
						}
						
						//Update values if is into edit mode
						if(this._dataSetFormModel.mode === FormMode.EDIT) {
							if(propertyType.dataType === "BOOLEAN") {
								$($($component.children()[0]).children()[0]).prop('checked', value === "true");
							} else if(propertyType.dataType === "TIMESTAMP") {
							} else {
								$component.val(value);
							}
						} else {
							$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
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
						
						$controls.append($component);
						
						$fieldset.append($controlGroup);
					}
				}
				propertyGroupPropertiesOnForm++;
			}
			
			if(propertyGroupPropertiesOnForm === 0) {
				$legend.remove();
			}
			
			$wrapper.append($fieldset);
		}
		
		$("#metadataContainer").append($wrapper);
	}
}