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
		$wrapper.submit(function(event) {
			event.preventDefault();
			_this._dataSetFormController.submitDataSet(); 
		});
		
		//
		// Title
		//
		var $title = $('<div>');
		
		var titleText = 'Create DataSet';
		$title.append($("<h2>").append(titleText));
		
		$wrapper.append($title);
		
		//
		// Toolbar
		//
		var $toolbar = $('<div>');
		$wrapper.append($toolbar);
		
		$wrapper.append("<br>");
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<div>');
		$dataSetTypeFieldSet.append($('<legend>').text('Identification Info'));
		$wrapper.append($dataSetTypeFieldSet);
		
		var $dataSetTypeSelector = FormUtil.getDataSetsDropDown('DATASET_TYPE', this._dataSetFormModel.dataSetTypes);
		$dataSetTypeSelector.change(function() { 
			_this._repaintMetadata(
					_this._dataSetFormController._getDataSetType($('#DATASET_TYPE').val())
			);
			_this.isFormDirty = true;
		});
		
		var $dataSetTypeDropDown = $('<div>', { class : 'form-group'});
		$dataSetTypeDropDown.append($('<label>', {class: "control-label " + FormUtil.labelColumnClass}).html('Data Set Type&nbsp;(*):'));
		$dataSetTypeDropDown.append(
			$('<div>', {class: FormUtil.controlColumnClass}).append($dataSetTypeSelector)
		);
		$dataSetTypeFieldSet.append($dataSetTypeDropDown);
		
		var owner = this._dataSetFormModel.sample.identifier;
		if(this._dataSetFormModel.sample.experimentIdentifierOrNull) {
			owner = this._dataSetFormModel.sample.experimentIdentifierOrNull + "/" + this._dataSetFormModel.sample.code;
		}
		$dataSetTypeFieldSet.append(FormUtil.getFieldForTextWithLabel(owner, "Sample"));
		
		//
		// Registration and modification info
		//
		
		//Metadata Container
		$wrapper.append($('<div>', { 'id' : 'metadataContainer'}));
		
		//Attach File
		$wrapper.append($('<div>', { 'id' : 'APIUploader' } ));
		
		$wrapper.append($('<div>', { 'id' : 'fileOptionsContainer' } ));
		
		//Show Files
		var filesViewer = $('<div>', { 'id' : 'filesViewer' } );
		$wrapper.append(filesViewer);
		
		//Submit Button
		var $submitButton = $('<fieldset>')
		.append($('<div>', { class : "form-group"}))
		.append($('<div>', {class: FormUtil.controlColumnClass})
					.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : titleText})));
		$wrapper.append($submitButton);
		
		//Attach to main form
		$container.append($('<div>', { class : 'row'}).append($('<div>', { class : FormUtil.formColumClass}).append($wrapper)));
		
		//Initialize file chooser
		var onComplete = function(data) {
			_this._dataSetFormModel.files.push(data.name);
			_this._updateFileOptions();
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
		
		openBIS.createSessionWorkspaceUploader($("#APIUploader"), onComplete, {
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
			var $fileFieldSetIsDirectory = $('<div>')
			.append($('<div>', { class : "form-group"})
						.append($('<label>', {class : 'control-label '+ FormUtil.labelColumnClass}).text('Uncompress before import:'))
						.append($('<div>', {class: FormUtil.controlColumnClass})
							.append(FormUtil.getFieldForPropertyType({
								dataType : "BOOLEAN",
								code : 'isZipDirectoryUpload',
								description : 'Uncompress before import:'
							})))
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
				var value = "";
				var isSystemProperty = false;
				var $controlGroup = $('<div>', {class : 'form-group'});
				var requiredStar = (propertyType.mandatory)?"&nbsp;(*)":"";				
				var $controlLabel = $('<label>', {'class' : "control-label " + FormUtil.labelColumnClass}).html(propertyType.label + requiredStar + ":");
				var $controls = $('<div>', {class : FormUtil.controlColumnClass});
				
				$controlGroup.append($controlLabel);
				$controlGroup.append($controls);
				
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
				$component.val(""); //HACK-FIX: Not all browsers show the placeholder in Bootstrap 3 if you don't set an empty value.
				
				$controls.append($component);
				
				$fieldset.append($controlGroup);
				
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