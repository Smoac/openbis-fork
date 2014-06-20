var FormMode = {
    CREATE : 0,
    EDIT : 1,
    VIEW : 2
}

var FormUtil = new function() {
	this.profile = null;
	
	//
	// Form css classes
	//
	this.formColumClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.controlColumnClass = 'col-md-6';
		
	//
	// Standard Form Fields
	//
	
	this.getSampleTypeDropdown = function(id, isRequired) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
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
	
	//
	// Get Field with container to obtain a correct layout
	//
	this.getFieldForComponentWithLabel = function($component, label) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		var requiredText = '';
		if($component.attr('required')) {
			requiredText = "&nbsp;(*)"
		}
		
		var $controlLabel = $('<label>', { class : 'control-label ' + this.labelColumnClass }).html(label + requiredText + ":");
		var $controls = $('<div>', { class : 'controls ' + this.controlColumnClass });
			
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		$fieldset.append($controlGroup);
		
		$controls.append($component);
		
		
		return $fieldset;
	}
	
	this.getFieldForLabelWithText = function(label, text) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		var $controlLabel = $('<label>', {class : 'control-label ' + this.labelColumnClass}).text(label + ":");
		var $controls = $('<div>', {class : 'controls ' + this.controlColumnClass });
			
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		$fieldset.append($controlGroup);
		
		var $component = $("<span>", {'class' : 'form-control', 'style' : 'border:none; box-shadow:none;'});
		$component.append(text);
		$controls.append($component);
		
		return $fieldset;
	}

	//
	// Get Field from property
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
			var codeWithoutSimbols = propertyType.code.replace('$','\\$').replace(/\./g,'\\.');
			$component = this._getDropDownFieldForVocabulary(codeWithoutSimbols, vocabulary.terms, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "HYPERLINK") {
			$component = this._getInputField("url", codeWithoutSimbols, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "INTEGER") {
			$component = this._getInputField("number", codeWithoutSimbols, propertyType.description, '1', propertyType.mandatory);
		} else if (propertyType.dataType === "MATERIAL") {
			$component = this._getInputField("text", codeWithoutSimbols, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "MULTILINE_VARCHAR") {
			$component = this._getTextBox(codeWithoutSimbols, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "REAL") {
			$component = this._getInputField("number", codeWithoutSimbols, propertyType.description, 'any', propertyType.mandatory);
		} else if (propertyType.dataType === "TIMESTAMP") {
			$component = this._getDatePickerField(codeWithoutSimbols, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "VARCHAR") {
			$component = this._getInputField("text", codeWithoutSimbols, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "XML") {
			$component = this._getTextBox(codeWithoutSimbols, propertyType.description, propertyType.mandatory);
		}
		
		return $component;
	}
	
	//
	// Form Fields
	//
	this._getBooleanField = function(id, alt) {
		return $('<div>', {'class' : 'checkbox'}).append($('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt, 'placeholder' : alt }));
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
		var $component = $("<select>", {'placeholder' : alt, 'class' : 'form-control'});
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
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt, 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (isRequired) {
			$component.attr('step', step);
		}
		return $component;
	}
	
	this._getTextBox = function(id, alt, isRequired) {
		var $component = $('<textarea>', {'id' : id, 'alt' : alt, 'style' : 'height: 80px; width: 450px;', 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		return $component;
	}
	
	this._getDatePickerField = function(id, alt, isRequired) {
		var $component = $('<div>', {'class' : 'form-group', 'style' : 'margin-left: 0px;', 'placeholder' : alt });
		var $subComponent = $('<div>', {'class' : 'input-group date', 'id' : 'datetimepicker_' + id });
		var $input = $('<input>', {'class' : 'form-control', 'type' : 'text', 'id' : id, 'data-format' : 'yyyy-MM-dd HH:mm:ss'});
		if (isRequired) {
			$input.attr('required', '');
		}
		var $spanAddOn = $('<span>', {'class' : 'input-group-addon'})
							.append($('<span>', {'class' : 'glyphicon glyphicon-calendar' }));
		
		$subComponent.append($input);
		$subComponent.append($spanAddOn);
		$subComponent.datetimepicker({ language: 'en' });
		$component.append($subComponent);
		
		return $component;
	}
}