var FormUtil = new function() {
	this.profile = null;
	
	//
	// Standard Form Fields
	//
	
	this.getSampleTypeDropdown = function(id, isRequired) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var $component = $("<select>", {"id" : id});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
			var label = Util.getEmptyIfNull(sampleType.description);
			if(label === "") {
				label = sampleType.code;
			}
			
			$component.append($("<option>").attr('value',sampleType.code).text(label));
		}
		
		return $component;
	}
	
	this.getFieldForComponentWithLabel = function($component, label) {
		var $fieldset = $('<fieldset>');
		
		var $controlGroup = $('<div>', {class : 'control-group'});
		var $controlLabel = $('<label>', {class : 'control-label'}).text(label + ":");
		var $controls = $('<div>', {class : 'controls'});
			
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		$fieldset.append($controlGroup);
		
		$controls.append($component);
		if($component.attr('required')) {
			$controls.append(' (Required)')
		}
		
		return $fieldset;
	}
	
	this.getFieldForPropertyTypeWithLabel = function(propertyType) {
		var $fieldset = $('<fieldset>');
		
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
		
		return $fieldset;
	}
	
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
			$component = this._getDropDownFieldForVocabulary(propertyType.code, vocabulary.terms, propertyType.description, propertyType.mandatory);
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
			$component = this._getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "VARCHAR") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "XML") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		}
		
		return $component;
	}
	
	this._getBooleanField = function(id, alt) {
		return $('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt, 'placeholder' : alt });
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
		var $component = $("<select>", {'placeholder' : alt});
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
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt, 'placeholder' : alt });
		if (isRequired) {
			$component.attr('required', '');
		}
		if (isRequired) {
			$component.attr('step', step);
		}
		return $component;
	}
	
	this._getTextBox = function(id, alt, isRequired) {
		var $component = $('<textarea>', {'id' : id, 'alt' : alt, 'style' : 'height: 80px; width: 450px;', 'placeholder' : alt });
		if (isRequired) {
			$component.attr('required', '');
		}
		return $component;
	}
	
	this._getDatePickerField = function(id, alt, isRequired) {
		var $component = $('<div>', {'class' : 'well', 'style' : 'width: 250px;', 'placeholder' : alt });
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
}