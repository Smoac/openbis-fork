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

DataSetFormMode = {
    CREATE : 0
}

function DataSetForm(serverFacade, containerId, profile, sample, mode) {
	this.serverFacade = serverFacade;
	this.containerId = containerId;
	this.profile = profile;
	this.sample = sample;
	this.mode = mode;
	this.dataSetTypes = null;
	this.files = [];
	this.isFormDirty = false;
	
	this.formColumClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.controlColumnClass = 'col-md-6';
		
	this.isDirty = function() {
		return this.isFormDirty;
	}
	
	this.init = function() {
		var localInstance = this;
		
		this.serverFacade.listDataSetTypes(
				function(data) {
					localInstance.dataSetTypes = data.result;
					localInstance._repaint();
				}
		);	
	}
	
	this._getDropDownForField = function(code, dataSetTypes) {
		var $component = $("<select>", { class : 'form-control ' });
		$component.attr('id', code);
		
		$component.attr('required', '');
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < dataSetTypes.length; i++) {
			$component.append($("<option>").attr('value',dataSetTypes[i].code).text(dataSetTypes[i].code));
		}
		
		return $component;
	}
	
	this._repaint = function() {
		//Clean and prepare container
		var localInstance = this;
		var container = $("#"+containerId);
		container.empty();
		
		var $wrapper = $('<form>', { class : 'form-horizontal ', 'id' : 'mainDataSetForm', 'role' : 'form'});
		$wrapper.submit(function(event) {localInstance._submitDataSet(); event.preventDefault();});
		
		$wrapper.append($('<h2>').text('Create Data Set'));
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<div>');
		$dataSetTypeFieldSet.append($('<legend>').text('Type Info'));
		
		var $dataSetTypeDropDownObj = this._getDropDownForField('DATASET_TYPE', this.dataSetTypes);
		$dataSetTypeDropDownObj.change(function() { 
			localInstance._repaintMetadata(
					localInstance._getDataSetType($('#DATASET_TYPE').val())
			);
			localInstance.isFormDirty = true;
		});
		
		var $dataSetTypeDropDown = $('<div>', { class : 'form-group'});
		$dataSetTypeDropDown.append($('<label>', {class: "control-label " + this.labelColumnClass}).html('Data Set Type&nbsp;(*):'));
		$dataSetTypeDropDown.append(
			$('<div>', {class: this.controlColumnClass})
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
			.append($('<div>', {class: this.controlColumnClass})
						.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : 'Create Dataset'})));
		$wrapper.append($submitButton);
		
		//Attach to main form
		container.append($('<div>', { class : 'row'}).append($('<div>', { class : this.formColumClass}).append($wrapper)));
		
		//Initialize file chooser
		var onComplete = function(data) {
			localInstance.files.push(data.name);
			localInstance._updateFileOptions();
			var dataSetType = localInstance.profile.getDataSetTypeForFileName(localInstance.files, data.name);
			if(dataSetType != null) {
				$("#DATASET_TYPE").val(dataSetType);
				$("#DATASET_TYPE").prop('disabled', true);
				localInstance._repaintMetadata($('#DATASET_TYPE').val())
			}
		}
		
		var onDelete = function(data) {
			for(var i=0; localInstance.files.length; i++) {
				if(localInstance.files[i] === data.name) {
					localInstance.files.splice(i, 1);
					break;
				}
			}
			localInstance._updateFileOptions();
		}
		
		this.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), onComplete, {
			main_title : $('<legend>').text('Files Uploader'),
			uploads_title : $('<legend>').text('File list'),
			ondelete:onDelete
		});
	}
	
	this._updateFileOptions = function() {
		var localInstance = this;
		$wrapper = $("#fileOptionsContainer"); //Clean existing
		$wrapper.empty();
		
		if( this.files.length > 1
			||
			this.files.length === 1 && this.files[0].indexOf('zip', this.files[0].length - 3) !== -1) {
			var $legend = $('<div>').append($('<legend>').text('Files Options'));
			$wrapper.append($legend);
		}
		
		if(this.files.length > 1) {
			var $textField = FormUtil._getInputField('text', 'folderName', 'Folder Name', null, true);
			$textField.change(function(event) {
				localInstance.isFormDirty = true;
			});
			
			var $folderName = $('<div>')
			.append($('<div>', { class : "form-group"})
					.append($('<label>', {class : 'control-label '+this.labelColumnClass}).html('Folder Name&nbsp;(*):'))
					.append($('<div>', {class: this.controlColumnClass})
						.append($textField))
			);
			$wrapper.append($folderName);
		}
		
		if(this.files.length === 1 && 
				this.files[0].indexOf('zip', this.files[0].length - 3) !== -1) {
			var isZipDirectoryUpload = this.profile.isZipDirectoryUpload($('#DATASET_TYPE').val());
			if(isZipDirectoryUpload === null) {
				var $fileFieldSetIsDirectory = $('<div>')
				.append($('<div>', { class : "form-group"})
							.append($('<label>', {class : 'control-label '+this.labelColumnClass}).text('Uncompress before import:'))
							.append($('<div>', {class: this.controlColumnClass})
								.append(FormUtil._getBooleanField('isZipDirectoryUpload', 'Uncompress before import:')))
				);
				$wrapper.append($fileFieldSetIsDirectory);
				
				$("#isZipDirectoryUpload").change(function() {
					localInstance.isFormDirty = true;
					if($("#isZipDirectoryUpload"+":checked").val() === "on") {
						var $textField = FormUtil._getInputField('text', 'folderName', 'Folder Name', null, true);
						$textField.change(function(event) {
							localInstance.isFormDirty = true;
						});
						
						var $folderName = $('<div>', { "id" : "folderNameContainer"})
						.append($('<div>', { class : "form-group"})
								.append($('<label>', {class : 'control-label '+ localInstance.labelColumnClass}).html('Folder Name&nbsp;(*):'))
								.append($('<div>', {class: localInstance.controlColumnClass})
									.append($textField))
						);
						$("#fileOptionsContainer").append($folderName);
						$("#folderName").val(localInstance.files[0].substring(0, localInstance.files[0].indexOf(".")));
					} else {
						$( "#folderNameContainer" ).remove();
					}
				})
			}
		}
		
		
	}
	
	this._getDataSetType = function(typeCode) {
		for(var i = 0; i < this.dataSetTypes.length; i++) {
			if(this.dataSetTypes[i].code === typeCode) {
				return this.dataSetTypes[i];
			}
		}
		return null;
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
				var $controlLabel = $('<label>', {'class' : "control-label " + this.labelColumnClass}).html(propertyType.label + requiredStar + ":");
				var $controls = $('<div>', {class : this.controlColumnClass});
				
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
	
	//
	// Form Submit
	//
	this._submitDataSet = function() {
		//
		// Check upload is finish
		//
		if(this.files.length === 0) {
			Util.blockUI();
			Util.showError("You should upload at least one file.", function() { Util.unblockUI(); });
			return;
		}
		
		if(Uploader.uploadsInProgress()) {
			Util.blockUI();
			Util.showError("Please wait the upload to finish.", function() { Util.unblockUI(); });
			return;
		}
		
		Util.blockUI();
		var localInstance = this;
		
		//
		// Metadata Submit and Creation (Step 2)
		//
		var metadata = { };
			
		var dataSetType = localInstance._getDataSetType($('#DATASET_TYPE').val());
		for(var i = 0; i < dataSetType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = dataSetType.propertyTypeGroups[i];
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				var value = null;
					
				if (propertyType.dataType === "BOOLEAN") {
					value = $("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')+":checked").val() === "on";
				} else {
					value = Util.getEmptyIfNull($("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')).val());
				}
				
				metadata[propertyType.code] = value;
			}
		}
			
		var isZipDirectoryUpload = this.profile.isZipDirectoryUpload($('#DATASET_TYPE').val());			
		if(isZipDirectoryUpload === null) {
			isZipDirectoryUpload = $("#isZipDirectoryUpload"+":checked").val() === "on";
		}
		
		var folderName = $('#folderName').val();
		if(!folderName) {
			folderName = 'DEFAULT';
		}
		
		var parameters = {
				//API Method
				"method" : "insertDataSet",
				//Identification Info
				"sampleIdentifier" : sample.identifier,
				"dataSetType" : $('#DATASET_TYPE').val(),
				"filenames" : localInstance.files,
				"folderName" : folderName,
				"isZipDirectoryUpload" : isZipDirectoryUpload,
				//Metadata
				"metadata" : metadata,
				//For Moving files
				"sessionID" : localInstance.serverFacade.openbisServer.getSession(),
				"openBISURL" : localInstance.serverFacade.openbisServer._internal.openbisUrl
		};
			
		if(localInstance.profile.allDataStores.length > 0) {
			localInstance.serverFacade.createReportFromAggregationService(localInstance.profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					var isUserFailureException = stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") === 0;
					var startIndex = null;
					var endIndex = null;
					if(isUserFailureException) {
						startIndex = "ch.systemsx.cisd.common.exceptions.UserFailureException".length + 2;
						endIndex = stacktrace.indexOf("at ch.systemsx");
					} else {
						startIndex = 0;
						endIndex = stacktrace.length;
					}
					var errorMessage = stacktrace.substring(startIndex, endIndex).trim();
					Util.showError(errorMessage, function() {Util.unblockUI();});
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var callbackOk = function() {
						localInstance.isFormDirty = false;
						Util.unblockUI();
					}
					Util.showSuccess("DataSet Created.", callbackOk);
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}

}