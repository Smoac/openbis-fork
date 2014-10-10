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
		
		$wrapper.append($('<h2>').text('Create Data Set'));
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<div>');
		$dataSetTypeFieldSet.append($('<legend>').text('Type Info'));
		
		var $dataSetTypeDropDownObj = FormUtil.getDataSetsDropDown('DATASET_TYPE', this._dataSetFormModel.dataSetTypes);
		$dataSetTypeDropDownObj.change(function() { 
			_this._repaintMetadata(
					_this._dataSetFormController._getDataSetType($('#DATASET_TYPE').val())
			);
			_this.isFormDirty = true;
		});
		
		var $dataSetTypeDropDown = $('<div>', { class : 'form-group'});
		$dataSetTypeDropDown.append($('<label>', {class: "control-label " + FormUtil.labelColumnClass}).html('Data Set Type&nbsp;(*):'));
		$dataSetTypeDropDown.append(
			$('<div>', {class: FormUtil.controlColumnClass})
				.append($dataSetTypeDropDownObj)
		);
		$dataSetTypeFieldSet.append($dataSetTypeDropDown);
		$wrapper.append($dataSetTypeFieldSet);
		
		//Metadata Container
		$wrapper.append($('<div>', { 'id' : 'metadataContainer'}));
		
		//Attach File
		$wrapper.append($('<div>', { 'id' : 'APIUploader' } ));
		
		$wrapper.append($('<div>', { 'id' : 'fileOptionsContainer' } ));
		
		//Submit Button
		var $submitButton = $('<fieldset>')
			.append($('<div>', { class : "form-group"}))
			.append($('<div>', {class: FormUtil.controlColumnClass})
						.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : 'Create Dataset'})));
		$wrapper.append($submitButton);
		
		//Attach to main form
		$container.append($('<div>', { class : 'row'}).append($('<div>', { class : FormUtil.formColumClass}).append($wrapper)));
		
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
		
		mainController.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), onComplete, {
			main_title : $('<legend>').text('Files Uploader'),
			uploads_title : $('<legend>').text('File list'),
			ondelete:onDelete
		});
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
		var localInstance = this;
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
			
				var $controlGroup = $('<div>', {class : 'form-group'});
				var requiredStar = (propertyType.mandatory)?"&nbsp;(*)":"";				
				var $controlLabel = $('<label>', {'class' : "control-label " + FormUtil.labelColumnClass}).html(propertyType.label + requiredStar + ":");
				var $controls = $('<div>', {class : FormUtil.controlColumnClass});
				
				$controlGroup.append($controlLabel);
				$controlGroup.append($controls);
				$fieldset.append($controlGroup);
				
				var $component = FormUtil.getFieldForPropertyType(propertyType);
				$component.change(function(event) {
					localInstance.isFormDirty = true;
				});
				
				$controls.append($component);
				
			}
			
			$wrapper.append($fieldset);
		}
		
		$("#metadataContainer").append($wrapper);
	}
}