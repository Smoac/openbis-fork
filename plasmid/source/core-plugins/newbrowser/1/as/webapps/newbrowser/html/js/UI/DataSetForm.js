/*
 * Copyright 2013 ETH Zuerich, CISD
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
		var $component = $("<select>");
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
		
		var $wrapper = $('<form>', { class : 'form-horizontal', 'id' : 'mainDataSetForm'});
		$wrapper.submit(function(event) {localInstance._submitDataSet(); event.preventDefault();});
		
		$wrapper.append($('<h2>').text('Create Data Set'));
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<fieldset>');
		$dataSetTypeFieldSet.append($('<legend>').text('Type Info'));
		
		var $dataSetTypeDropDownObj = this._getDropDownForField('DATASET_TYPE', this.dataSetTypes);
		$dataSetTypeDropDownObj.change(function() { 
			localInstance._repaintMetadata(
					localInstance._getDataSetType($('#DATASET_TYPE').val())
			);
		});
		
		var $dataSetTypeDropDown = $('<div>', { class : 'control-group'});
		$dataSetTypeDropDown.append($('<label>', {class: 'control-label'}).text('Data Set Type:'));
		$dataSetTypeDropDown.append(
			$('<div>', {class: 'controls'})
				.append($dataSetTypeDropDownObj).append(' (Required)')
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
			.append($('<div>', { class : "control-group"}))
			.append($('<div>', {class: 'controls'})
						.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : 'Create Dataset'})));
		$wrapper.append($submitButton);
		
		//Attach to main form
		container.append($wrapper);
		
		//Initialize file chooser
		var onComplete = function(data) {
			localInstance.files.push(data.name);
			localInstance._updateFileOptions();
		}
		this.serverFacade.openbisServer.createSessionWorkspaceUploader($("#APIUploader"), onComplete);
		
		var something = 0;
	}
	
	this._updateFileOptions = function() {
		$wrapper = $("#fileOptionsContainer"); //Clean existing
		$wrapper.empty();
		
		if( this.files.length > 1
			||
			this.files.length === 1 && this.files[0].indexOf('zip', this.files[0].length - 3) !== -1) {
			var $leyend = $('<fieldset>').append($('<legend>').text('Files Options'));
			$wrapper.append($leyend);
		}
		
		if(this.files.length > 1) {
			var $folderName = $('<fieldset>')
			.append($('<div>', { class : "control-group"})
					.append($('<label>', {class : 'control-label'}).text('Folder Name:'))
					.append($('<div>', {class: 'controls'})
						.append(this._getInputField('text', 'folderName', 'Folder Name', null, true))
						.append(' (Required)'))
			);
			$wrapper.append($folderName);
		}
		
		if(this.files.length === 1 && 
				this.files[0].indexOf('zip', this.files[0].length - 3) !== -1) {
			var isZipDirectoryUpload = this.profile.isZipDirectoryUpload($('#DATASET_TYPE').val());
			if(isZipDirectoryUpload === null) {
				var $fileFieldSetIsDirectory = $('<fieldset>')
				.append($('<div>', { class : "control-group"})
							.append($('<label>', {class : 'control-label'}).text('Uncompress before import:'))
							.append($('<div>', {class: 'controls'})
								.append(this._getBooleanField('isZipDirectoryUpload', 'Uncompress before import:')))
				);
				$wrapper.append($fileFieldSetIsDirectory);
				
				var localInstance = this;
				$("#isZipDirectoryUpload").change(function() {
					if($("#isZipDirectoryUpload"+":checked").val() === "on") {
						var $folderName = $('<fieldset>', { "id" : "folderNameContainer"})
						.append($('<div>', { class : "control-group"})
								.append($('<label>', {class : 'control-label'}).text('Folder Name:'))
								.append($('<div>', {class: 'controls'})
									.append(localInstance._getInputField('text', 'folderName', 'Folder Name', null, true))
									.append(' (Required)'))
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
		$("#metadataContainer").empty();
		var $wrapper = $("<div>");
		
		for(var i = 0; i < dataSetType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = dataSetType.propertyTypeGroups[i];
			
			var $fieldset = $('<fieldset>');
			var $legend = $('<legend>'); 
			$fieldset.append($legend);
			
			if(propertyTypeGroup.name) {
				$legend.text(propertyTypeGroup.name);
			} else if(dataSetType.propertyTypeGroups.length === 1) { //Only when there is only one group without name to render it with a default title.
				$legend.text("Metadata Fields");
			}
			
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
			
				var $controlGroup = $('<div>', {class : 'control-group'});
				var $controlLabel = $('<label>', {class : 'control-label'}).text(propertyType.label + ":");
				var $controls = $('<div>', {class : 'controls'});
				
				$controlGroup.append($controlLabel);
				$controlGroup.append($controls);
				$fieldset.append($controlGroup);
				
				var $component = this.getFieldForPropertyType(propertyType);
				$controls.append($component);
				if(propertyType.mandatory) {
					$controls.append(' (Required)')
				}
			}
			
			$wrapper.append($fieldset);
		}
		
		$("#metadataContainer").append($wrapper);
	}
	
	//
	// Standard Form Fields
	//
	this.getFieldForPropertyType = function(propertyType) {
		var $component = null;
		if (propertyType.dataType === "BOOLEAN") {
			$component = this._getBooleanField(propertyType.code, propertyType.description);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			var vocabulary = null;
			if(isNaN(propertyType.vocabulary)) {
				vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
				if(vocabulary === null) { //This should not happen, but can save the day.
					vocabulary = propertyType.vocabulary;
					vocabulary.terms = propertyType.terms;
				}
			} else {
				vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
			}
			$component = this._getDropDownFieldForVocabulary(propertyType.code, vocabulary.terms, propertyType.mandatory);
		} else if (propertyType.dataType === "HYPERLINK") {
			$component = this._getInputField("url", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "INTEGER") {
			$component = this._getInputField("number", propertyType.code, propertyType.description, '1', propertyType.mandatory);
		} else if (propertyType.dataType === "MATERIAL") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "MULTILINE_VARCHAR") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "REAL") {
			$component = this._getInputField("number", propertyType.code, propertyType.description, 'any', propertyType.mandatory);
		} else if (propertyType.dataType === "TIMESTAMP") {
			$component = this._getDatePickerField(propertyType.code, propertyType.mandatory);
		} else if (propertyType.dataType === "VARCHAR") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "XML") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		}
		
		return $component;
	}
	
	this._getBooleanField = function(id, alt) {
		return $('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt});
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, isRequired) {
		var $component = $("<select>");
		$component.attr('id', code);
		
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < terms.length; i++) {
			$component.append($("<option>").attr('value',terms[i].code).text(terms[i].label));
		}
		
		return $component;
	}
	
	this._getInputField = function(type, id, alt, step, isRequired) {
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (isRequired) {
			$component.attr('step', step);
		}
		return $component;
	}
	
	this._getTextBox = function(id, alt, isRequired) {
		var $component = $('<textarea>', {'id' : id, 'alt' : alt, 'style' : 'height: 80px; width: 450px;'});
		if (isRequired) {
			$component.attr('required', '');
		}
		return $component;
	}
	
	this._getDatePickerField = function(id, isRequired) {
		var $component = $('<div>', {'class' : 'well', 'style' : 'width: 250px;'});
		var $subComponent = $('<div>', {'class' : 'input-append date', 'id' : 'datetimepicker_' + id });
		var $input = $('<input>', {'type' : 'text', 'id' : id, 'data-format' : 'yyyy-MM-dd HH:mm:ss'});
		if (isRequired) {
			$input.attr('required', '');
		}
		var $spanAddOn = $('<span>', {'class' : 'add-on'})
							.append($('<i>', {'data-date-icon' : 'icon-calendar' , 'data-time-icon' : 'icon-time' }));
		
		$subComponent.append($input);
		$subComponent.append($spanAddOn);
		$subComponent.datetimepicker({ language: 'en' });
		$component.append($subComponent);
		
		return $component;
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
		
		var uploadingFiles = $("#filelist").children();
		if(uploadingFiles.length > this.files.length) {
			Util.blockUI();
			Util.showError("Only " + this.files.length + " from " + uploadingFiles.length + " files have finish. Please wait the upload to finish.", function() { Util.unblockUI(); });
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
						Util.unblockUI();
						mainController.navigationBar.backButton(null);
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