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
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		//Clean and prepare container
		var $wrapper = $('<form>', { class : 'form-horizontal ', 'id' : 'mainDataSetForm', 'role' : 'form'});
		$wrapper.submit(function(event) {_this._dataSetFormController.submitDataSet(); event.preventDefault();});
		
		//Title
		var titleText = null;
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			titleText = 'Create Dataset';
		} else if(this._dataSetFormModel.mode === FormMode.EDIT) {
			titleText = 'Update Dataset';
		} else if(this._dataSetFormModel.mode === FormMode.VIEW) {
			titleText = 'View Dataset';
		}
		var $title = $('<h2>').text(titleText);
		$wrapper.append($title);
		
		//Delete Button
		if(this._dataSetFormModel.mode !== FormMode.CREATE) {
			$title.append("&nbsp;");
			$title.append(FormUtil.getDeleteButton(function(reason) {
				_this._dataSetFormController.deleteDataSet(reason);
			}, true));
		}
		
		//Edit Button
		if(this._dataSetFormModel.mode === FormMode.VIEW) {
			$title.append("&nbsp;");
			var $editButton = $("<a>", { 'class' : 'btn btn-default'} )
				.append($('<span>', { 'class' : 'glyphicon glyphicon-edit' }))
				.append(' Enable Editing');
		
			$editButton.click(function() {
				mainController.changeView('showEditDataSetPageFromPermId', _this._dataSetFormModel.dataSet.code);
			});
			
			$title.append($editButton)
		}
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<div>');
		$dataSetTypeFieldSet.append($('<legend>').text('Type Info'));
		
		var $dataSetTypeSelector = null;
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			$dataSetTypeSelector = FormUtil.getDataSetsDropDown('DATASET_TYPE', this._dataSetFormModel.dataSetTypes);
			$dataSetTypeSelector.change(function() { 
				_this._repaintMetadata(
						_this._dataSetFormController._getDataSetType($('#DATASET_TYPE').val())
				);
				_this.isFormDirty = true;
			});
			
			var $dataSetTypeDropDown = $('<div>', { class : 'form-group'});
			$dataSetTypeDropDown.append($('<label>', {class: "control-label " + FormUtil.labelColumnClass}).html('Data Set Type&nbsp;(*):'));
			$dataSetTypeDropDown.append(
				$('<div>', {class: FormUtil.controlColumnClass})
					.append($dataSetTypeSelector)
			);
			$dataSetTypeFieldSet.append($dataSetTypeDropDown);
			$wrapper.append($dataSetTypeFieldSet);
		} else {
			var $dataSetTypeLabel = FormUtil.getFieldForLabelWithText('Data Set Type', this._dataSetFormModel.dataSet.dataSetTypeCode, "CODE");
			$wrapper.append($dataSetTypeLabel);
		}
		
		var ownerName = "Sample";
		var owner = this._dataSetFormModel.sample.identifier;
		if(this._dataSetFormModel.sample.experimentIdentifierOrNull) {
			owner = this._dataSetFormModel.sample.experimentIdentifierOrNull + "/" + this._dataSetFormModel.sample.code;
		}
		$wrapper.append(FormUtil.getFieldForLabelWithText(ownerName, owner));
		
		//
		// Registration and modification info
		//
		if(this._dataSetFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._dataSetFormModel.dataSet.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$wrapper.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", (new Date(registrationDetails.registrationDate)).toLocaleString())
			$wrapper.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$wrapper.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("Modification Date", (new Date(registrationDetails.modificationDate)).toLocaleString());
			$wrapper.append($modificationDate);
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
			var $submitButton = $('<fieldset>')
			.append($('<div>', { class : "form-group"}))
			.append($('<div>', {class: FormUtil.controlColumnClass})
						.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : titleText})));
			$wrapper.append($submitButton);
		}
		
		//Attach to main form
		$container.append($('<div>', { class : 'row'}).append($('<div>', { class : FormUtil.formColumClass}).append($wrapper)));
		
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			//Initialize file chooser
			var onComplete = function(data) {
				_this._dataSetFormModel.files.push(data.name);
				_this._updateFileOptions();
				var dataSetType = profile.getDataSetTypeForFileName(_this._dataSetFormModel.files, data.name);
				if(dataSetType != null) {
					$("#DATASET_TYPE").val(dataSetType);
					$("#DATASET_TYPE").prop('disabled', true);
					_this._repaintMetadata($('#DATASET_TYPE').val())
				}
			}
			
			var onDelete = function(data) {
				for(var i=0; _this._dataSetFormModel.files.length; i++) {
					if(_this._dataSetFormModel.files[i] === data.name) {
						_this._dataSetFormModel.files.splice(i, 1);
						break;
					}
				}
				_this._updateFileOptions();
			}
			
			if(this._dataSetFormModel.mode === FormMode.CREATE) {
				mainController.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), onComplete, {
					main_title : $('<legend>').text('Files Uploader'),
					uploads_title : $('<legend>').text('File list'),
					ondelete:onDelete
				});
			}
		} else {
			var dataSetType = _this._dataSetFormController._getDataSetType(this._dataSetFormModel.dataSet.dataSetTypeCode);
			this._repaintMetadata(dataSetType);
		}
		
		if(this._dataSetFormModel.mode !== FormMode.CREATE) {
			var dataSetViewer = new DataSetViewer("filesViewer", profile, null, mainController.serverFacade, profile.getDefaultDataStoreURL(), { result : [this._dataSetFormModel.dataSet] }, false, false);
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
					.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).html('Folder Name&nbsp;(*):'))
					.append($('<div>', {class: FormUtil.controlColumnClass})
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
							.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).text('Uncompress before import:'))
							.append($('<div>', {class: FormUtil.controlColumnClass})
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
								.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).html('Folder Name&nbsp;(*):'))
								.append($('<div>', {class: FormUtil.controlColumnClass})
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
			
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				if(propertyType.code === "XMLCOMMENTS") {
					var $commentsContainer = $("<div>");
					$fieldset.append($commentsContainer);
					this._dataSetFormController._addCommentsWidget($commentsContainer);
					continue;
				}
				var value = "";
				var isSystemProperty = false;
				if(this._dataSetFormModel.mode !== FormMode.CREATE) {
					value = this._dataSetFormModel.dataSet.properties[propertyType.code];
					if(!value && propertyType.code.charAt(0) === '$') {
						value = this._dataSetFormModel.dataSet.properties[propertyType.code.substr(1)];
						isSystemProperty = true;
					}
				}
				
				if(this._dataSetFormModel.mode === FormMode.VIEW) {
					if(Util.getEmptyIfNull(value) !== "") { //Don't show empty fields, whole empty sections will show the title
						if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
							value = FormUtil.getVocabularyLabelForTermCode(propertyType, value);
						}
						var $controlGroup = FormUtil.getFieldForLabelWithText(propertyType.label, value, propertyType.code);
						$fieldset.append($controlGroup);
					}
				} else {
					var $controlGroup = $('<div>', {class : 'form-group'});
					var requiredStar = (propertyType.mandatory)?"&nbsp;(*)":"";				
					var $controlLabel = $('<label>', {'class' : "control-label " + FormUtil.labelColumnClass}).html(propertyType.label + requiredStar + ":");
					var $controls = $('<div>', {class : FormUtil.controlColumnClass});
					
					$controlGroup.append($controlLabel);
					$controlGroup.append($controls);
					$fieldset.append($controlGroup);
					
					var $component = FormUtil.getFieldForPropertyType(propertyType);
					
					//Update model
					var changeEvent = function(propertyType, isSystemProperty) {
						return function() {
							var propertyTypeCode = null;
							if(isSystemProperty) {
								propertyTypeCode = propertyType.code.substr(1);
							} else {
								propertyTypeCode = propertyType.code;
							}
							_this._dataSetFormModel.isFormDirty = true;
							var field = $(this);
							if(propertyType.dataType === "BOOLEAN") {
								_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = field.children()[0].checked;
							} else if (propertyType.dataType === "TIMESTAMP") {
								var timeValue = $($(field.children()[0]).children()[0]).val();
								_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = timeValue;
							} else {
								_this._dataSetFormModel.dataSet.properties[propertyTypeCode] = Util.getEmptyIfNull(field.val());
							}
						}
					}
					
					//Avoid modifications in properties managed by scripts
					if(propertyType.managed || propertyType.dinamic) {
						$component.prop('disabled', true);
					}
					
					$component.change(changeEvent(propertyType, isSystemProperty));
					
					//Update values if is into edit mode
					if(this._dataSetFormModel.mode === FormMode.EDIT) {
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
					
					$controls.append($component);
				}
			}
			
			$wrapper.append($fieldset);
		}
		
		$("#metadataContainer").append($wrapper);
	}
}