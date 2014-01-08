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
		var $wrapper = $('<form>', { class : 'form-horizontal'});
		$wrapper.append($('<h2>').text("Create Data Set"));
		
		//Drop Down DataSetType Field Set
		var $dataSetTypeFieldSet = $('<fieldset>');
		$dataSetTypeFieldSet.append($('<legend>').text("Type Info"));
		
		var $dataSetTypeDropDownObj = this._getDropDownForField('DATASET_TYPE', this.dataSetTypes);
		$dataSetTypeDropDownObj.change(function() { 
			localInstance._repaintMetadata(
					localInstance._getDataSetType($('#DATASET_TYPE').val())
			);
		});
		
		var $dataSetTypeDropDown = $('<div>', { class : "control-group"});
		$dataSetTypeDropDown.append($('<label>', {class: 'control-label'}).text('Data Set Type:'));
		$dataSetTypeDropDown.append(
			$('<div>', {class: 'controls'})
				.append($dataSetTypeDropDownObj).append(' (Required)')
		);
		$dataSetTypeFieldSet.append($dataSetTypeDropDown);
		$wrapper.append($dataSetTypeFieldSet);
		
		//Metadata Container
		$wrapper.append($('<div>', {'id' : 'metadataContainer'}));
		
		//Attach File
		var $fileFieldSet = $('<fieldset>')
										.append($('<legend>').text("File to upload"))
										.append($('<div>', { class : "control-group"}))
										.append($('<div>', {class: 'controls'})
														.append($('<input>', {'type' : 'file', class : "filestyle", 'data-buttonText' : 'Find file'})).append(' (Required)'));
		
		$wrapper.append($fileFieldSet);
		
		//Submit Button
		var $submitButton = $('<fieldset>')
			.append($('<div>', { class : "control-group"}))
			.append($('<div>', {class: 'controls'})
						.append($('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : 'Create Dataset'})));
		
		$wrapper.append($submitButton);
		
		//Attach to main form
		container.append($wrapper);
		
		//Initialize file chooser
		$(":file").filestyle({buttonText: "Find file"});
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
		
		//Jquery evaluate javascript instead of leaving the browser to do it when is inserted.
		//We need this executed after the elements are inserted into the DOM, that's why we have a timeout of 1s.
		//This opens the door to a race condition, if the elements take more time to be inserted into the DOM it will not work.
		var $script = $('<script>', {'type' : 'text/javascript'})
				.text("$(setTimeout(function() { $('#datetimepicker_" + id + "').datetimepicker({ language: 'en' });  }) , 1000);");
		
		$subComponent.append($input);
		$subComponent.append($spanAddOn);
		$component.append($subComponent);
		$component.append($script);
		
		return $component;
	}
}