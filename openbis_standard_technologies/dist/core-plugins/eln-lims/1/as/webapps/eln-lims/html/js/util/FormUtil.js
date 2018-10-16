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
	this.shortformColumClass = 'col-md-9'
	this.formColumClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.shortControlColumnClass = 'col-md-5';
	this.controlColumnClass = 'col-md-9';
	this.controlColumnClassBig = 'col-md-9';
	//
	// Annotations
	//
	this.addAnnotationSlotForSample = function(stateObj, sample) {
		var sampleAnnotations = stateObj[sample.permId];
		if(!sampleAnnotations) {
			sampleAnnotations = {};
			stateObj[sample.permId] = sampleAnnotations;
		}
		
		sampleAnnotations["identifier"] =  sample.identifier; //Adds code to the annotations if not present
		sampleAnnotations["sampleType"] =  sample.sampleTypeCode; //Adds sampleType code to the annotations if not present
		return sampleAnnotations;
	}
	
	this.writeAnnotationForSample = function(stateObj, sample, propertyTypeCode, propertyValue) {
		var sampleAnnotations = this.addAnnotationSlotForSample(stateObj, sample);
		
		if(propertyTypeCode && propertyValue !== null && propertyValue !== undefined) {
			sampleAnnotations[propertyTypeCode] = propertyValue;
		}
	}
	
	this.deleteAnnotationsFromPermId = function(stateObj, permId) {
		delete stateObj[permId];
	}
	
	this.getXMLFromAnnotations = function(stateObj) {
		var rootNode = document.createElementNS("http://www.w3.org/1999/xhtml", "root"); //The namespace should be ignored by both ELN and openBIS parsers
		
		for(var permId in stateObj) {
			var sampleNode	= document.createElementNS("http://www.w3.org/1999/xhtml", "Sample"); //Should not add the namespace since is the same as the root
			sampleNode.setAttributeNS(null, "permId", permId); //Should not add the namespace
			
			for(var propertyTypeCode in stateObj[permId]) {
				var propertyTypeValue = stateObj[permId][propertyTypeCode];
				sampleNode.setAttributeNS(null, propertyTypeCode, propertyTypeValue); //Should not add the namespace
			}
			
			rootNode.appendChild(sampleNode);
		}
		
		var serializer = new XMLSerializer();
		var xmlDoc = serializer.serializeToString(rootNode);
		return xmlDoc;
	}
	
	this.getAnnotationsFromSample = function(sample) {
		var field = sample.properties["ANNOTATIONS_STATE"];
		var stateFieldValue = Util.getEmptyIfNull(field);
		if(stateFieldValue === "") {
			stateFieldValue = undefined;
			sample.properties["ANNOTATIONS_STATE"] = undefined;
		}
		return this.getAnnotationsFromField(stateFieldValue);
	}
	
	this.getAnnotationsFromField = function(field) {
		var stateObj = {};
		var stateFieldValue = Util.getEmptyIfNull(field);

		if(stateFieldValue === "") {
			return stateObj;
		}
		var xmlDoc = new DOMParser().parseFromString(stateFieldValue, 'text/xml');
		var samples = xmlDoc.getElementsByTagName("Sample");
		for(var i = 0; i < samples.length; i++) {
			var sample = samples[i];
			var permId = sample.attributes["permId"].value;
			for(var j = 0; j < sample.attributes.length; j++) {
				var attribute = sample.attributes[j];
				if(attribute.name !== "permId") {
					if(!stateObj[permId]) {
						stateObj[permId] = {};
					}
					stateObj[permId][attribute.name] = attribute.value;
				}
			}
		}
		return stateObj;
	}
	//
	// Standard Form Fields
	//
	
	this.getDropDownToogleWithSelectedFeedback = function(prefixElement, labelWithEvents, isSelectedFeedback, clickCallback) {
		var $dropDownToogle = $('<span>', { class : 'dropdown' });
		if(prefixElement) {
			$dropDownToogle.append(prefixElement);
		}
		$dropDownToogle.append($('<button>', { 'href' : '#', 'data-toggle' : 'dropdown', 'class' : 'dropdown-toggle btn btn-default'}).append($('<b>', { 'class' : 'caret' })));
		
		var $dropDownToogleOptions = $('<ul>', { class : 'dropdown-menu', 'role' : 'menu' });
		$dropDownToogle.append($dropDownToogleOptions);
		
		for(var i = 0; i < labelWithEvents.length; i++) {
			
			var selectedFeedback = $('<span>', { 'id' : 'dropdown-' + labelWithEvents[i].id });
			
			if(isSelectedFeedback && i === 0) {
				selectedFeedback.append("<span class='glyphicon glyphicon-ok'></span>");
			}
			
			var $a = $('<a>', { class : '', 'title' : labelWithEvents[i].title }).append(selectedFeedback).append('&nbsp;').append(labelWithEvents[i].title);
			
			var clickFunction = function(labelWithEvents, selectedIndex, isSelectedFeedback) {
				return function() {
					if(isSelectedFeedback) {
						for(var j = 0; j < labelWithEvents.length; j++) {
							$("#" + 'dropdown-' + labelWithEvents[j].id).empty();
							if(j === selectedIndex) {
								$("#" + 'dropdown-' + labelWithEvents[j].id).append("<span class='glyphicon glyphicon-ok'></span>");
							}
						}
					}
					
					labelWithEvents[selectedIndex].href();
					
					if(clickCallback) {
						clickCallback();
					}
				};
			}
			
			$a.click(clickFunction(labelWithEvents, i, isSelectedFeedback));
			$dropDownToogleOptions.append($('<li>', { 'role' : 'presentation' }).append($a));
		}	
		return $dropDownToogle;
	}
	
	this.getDefaultBenchDropDown = function(id, isRequired, callbackFunction) {
		this.getDefaultStoragesDropDown(id, isRequired, function($storageDropDown) {
			if(!$storageDropDown) {
				return null;
			}
			for(var i = $storageDropDown.children().length -1; i >= 0 ; i--) {
				var isEmpty = $storageDropDown.children()[i].value === "";
				var isBench = $storageDropDown.children()[i].value.indexOf("BENCH") > -1;
				if(!isEmpty && !isBench){
					$storageDropDown.children()[i].remove();
			    }
			}
			callbackFunction($storageDropDown);
		});
		
	}
	
	this.getDefaultStorageBoxSizesDropDown = function(id, isRequired) {
		if(!this.profile.storagesConfiguration["isEnabled"]) {
			return null;
		}
		var storageBoxesVocabularyProp = this.profile.getPropertyType(this.profile.getStoragePropertyGroup().boxSizeProperty);
		if(!storageBoxesVocabularyProp) {
			return null;
		}
		var $storageBoxesDropDown = this.getFieldForPropertyType(storageBoxesVocabularyProp);
		$storageBoxesDropDown.attr('id', id);
		if (isRequired) {
			$storageBoxesDropDown.attr('required', '');
		}
		return $storageBoxesDropDown;
	}
	
	this.getDefaultStoragesDropDown = function(id, isRequired, callbackFunction) {
		if(!this.profile.storagesConfiguration["isEnabled"]) {
			return null;
		}
		
		profile.getStoragesConfiguation(function(storageConfigurations) {
			var $component = $("<select>", {"id" : id, class : 'form-control'});
			if (isRequired) {
				$component.attr('required', '');
			}
			
			$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text("Select a Storage"));
			for(var idx = 0; idx < storageConfigurations.length; idx++) {
				var storageConfiguration = storageConfigurations[idx];
				var label = null;
				if(storageConfiguration.label) {
					label = storageConfiguration.label;
				} else {
					label = storageConfiguration.code;
				}
				
				$component.append($("<option>").attr('value',storageConfiguration.code).text(label));
			}
			callbackFunction($component);
			Select2Manager.add($component);
		});
	}
	
	this.getBoxPositionsDropdown = function(id, isRequired, code) {
		var numRows = null;
		var numCols = null;
		
		if(code) {
			var rowsAndCols = code.split("X");
			numRows = parseInt(rowsAndCols[0]);
			numCols = parseInt(rowsAndCols[1]);
		} else {
			numRows = 0;
			numCols = 0;
		}
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		
		for(var i = 1; i <= numRows; i++) {
			var rowLetter = Util.getLetterForNumber(i);
			for(var j = 1; j <= numCols; j++) {
				$component.append($("<option>").attr('value',rowLetter+j).text(rowLetter+j));
			}
			
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getSampleTypeDropdown = function(id, isRequired, showEvenIfHidden, showOnly) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text("Select an " + ELNDictionary.sample + " type"));
		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
			
			if(showOnly && ($.inArray(sampleType.code, showOnly) !== -1)) {
				//Show 
			} else if(showOnly) {
				continue;
			}
			
			if(showEvenIfHidden && ($.inArray(sampleType.code, showEvenIfHidden) !== -1)) {
				// Show even if hidden
			} else if (profile.isSampleTypeHidden(sampleType.code)) {
				continue;
			}
			
			var label = Util.getDisplayNameFromCode(sampleType.code);
			var description = Util.getEmptyIfNull(sampleType.description);
			if(description !== "") {
				label += " (" + description + ")";
			}
			
			$component.append($("<option>").attr('value',sampleType.code).text(label));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getExperimentTypeDropdown = function(id, isRequired) {
		var experimentTypes = this.profile.allExperimentTypes;
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text("Select an " + ELNDictionary.getExperimentDualName() + " type"));
		for(var i = 0; i < experimentTypes.length; i++) {
			var experimentType = experimentTypes[i];
			if(profile.isExperimentTypeHidden(experimentType.code)) {
				continue;
			}
			
			var label = Util.getDisplayNameFromCode(experimentType.code);
			var description = Util.getEmptyIfNull(experimentType.description);
			if(description !== "") {
				label += " (" + description + ")";
			}
			
			$component.append($("<option>").attr('value',experimentType.code).text(label));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getSpaceDropdown = function(id, isRequired) {
		var spaces = this.profile.allSpaces;
		
		var $component = $("<select>", {"id" : id, class : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < spaces.length; i++) {
			$component.append($("<option>").attr('value', spaces[i]).text(Util.getDisplayNameFromCode(spaces[i])));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getDropdown = function(mapVals, placeHolder) {
		$dropdown = this.getPlainDropdown(mapVals, placeHolder);
		Select2Manager.add($dropdown);
		return $dropdown;
	}
	
	this.getPlainDropdown = function(mapVals, placeHolder) {

		var $component = $("<select>", {class : 'form-control'});
		if(placeHolder) {
			$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(placeHolder));
		}
		for(var mIdx = 0; mIdx < mapVals.length; mIdx++) {
			var $option = $("<option>").attr('value', mapVals[mIdx].value).text(mapVals[mIdx].label);
			if(mapVals[mIdx].disabled) {
				$option.attr('disabled', '');
			}
			if(mapVals[mIdx].selected) {
				$option.attr('selected', '');
			}
			$component.append($option);
		}
		return $component;
	}
	
	this.getDataSetsDropDown = function(code, dataSetTypes) {
		var $component = $("<select>", { class : 'form-control ' });
		$component.attr('id', code);
		
		$component.attr('required', '');
		
		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text('Select a dataset type'));
		
		for(var i = 0; i < dataSetTypes.length; i++) {
			var datasetType = dataSetTypes[i];
			var label = Util.getDisplayNameFromCode(datasetType.code);
			var description = Util.getEmptyIfNull(datasetType.description);
			if(description !== "") {
				label += " (" + description + ")";
			}
			
			$component.append($("<option>").attr('value',datasetType.code).text(label));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getOptionsRadioButtons = function(name, isFirstSelected, values, changeAction) {
		var $component = $("<div>");
		for(var vIdx = 0; vIdx < values.length; vIdx++) {
			if(vIdx !== 0) {
				$component.append(" ");
			}
			var $radio = $("<input>", { type: "radio", name: name, value: values[vIdx]});
			
			if(isFirstSelected && (vIdx === 0)) {
				$radio.attr("checked", "");
			}
			$radio.change(changeAction);
			$component.append($radio);
			$component.append(" " + values[vIdx]);
		}
		return $component;
	}
	
	this.getProjectAndExperimentsDropdown = function(withProjects, withExperiments, isRequired, callbackForComponent) {
		mainController.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
			var spaces = dataWithSpacesAndProjects.result;
            var projectsToUse = [];
            for (var i = 0; i < spaces.length; i++) {
            	var space = spaces[i];
            	for (var j = 0; j < space.projects.length; j++) {
                    var project = space.projects[j];
                    delete project["@id"];
                    delete project["@type"];
                    projectsToUse.push(project);
                }
            }
            
            mainController.serverFacade.listExperiments(projectsToUse, function(experiments) {
            	if(experiments.result) {
            		
            		for (var k = 0; k < experiments.result.length; k++) {
                		var experiment = experiments.result[k];
                		
                		for(var pIdx = 0; pIdx < projectsToUse.length; pIdx++) {
                    		var project = projectsToUse[pIdx];
                    		var projectIdentifier = IdentifierUtil.getProjectIdentifier(project.spaceCode, project.code);
                    		if(experiment.identifier.startsWith(projectIdentifier)) {
                    			if(!project.experiments) {
                    				project.experiments = [];
                    			}
                    			project.experiments.push(experiment);
                    		}
                    	}
                	}
                	//
            		//
            		var $component = $("<select>", { class : 'form-control'});
            		if(isRequired) {
            			$component.attr('required', '');
            		}
            		var placeHolder = "";
            		if(withProjects && withExperiments) {
            			placeHolder = "Select a project or " + ELNDictionary.getExperimentDualName();
            		} else if(withProjects) {
            			placeHolder = "Select a project";
            		} else if(withExperiments) {
            			placeHolder = "Select an " + ELNDictionary.getExperimentDualName();
            		}
            		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(placeHolder));
            		for(var pIdx = 0; pIdx < projectsToUse.length; pIdx++) {
            			var project = projectsToUse[pIdx];
            			var projectIdentifier = IdentifierUtil.getProjectIdentifier(project.spaceCode, project.code);
            			if(withProjects) {
            				$component.append($("<option>").attr('value', projectIdentifier).text(projectIdentifier));
            			}
            			if(project.experiments) {
            				for(var eIdx = 0; eIdx < project.experiments.length; eIdx++) {
                    			var experiment = project.experiments[eIdx];
                    			if(withExperiments) {
                    				var name = null;
                    				if(profile.propertyReplacingCode) {
                    					name = experiment.properties[profile.propertyReplacingCode];
                    				}
                    				if(name) {
                    					name = " (" + name + ")";
                    				} else {
                    					name = "";
                    				}
                    				$component.append($("<option>").attr('value',experiment.identifier).text(experiment.identifier + name));
                    			}
                			}
            			}
            		}
            		Select2Manager.add($component);
            		callbackForComponent($component);
            	}
            });
		});
	}
	
	this.getDeleteButton = function(deleteFunction, includeReason, warningText) {
		var $deleteBtn = $("<a>", { 'class' : 'btn btn-default ' });
		$deleteBtn.append($("<span>", { 'class' : 'glyphicon glyphicon-trash', 'style' : 'width:16px; height:16px;'}));
		$deleteBtn.click(function() {
			var modalView = new DeleteEntityController(deleteFunction, includeReason, warningText);
			modalView.init();
		});
		return $deleteBtn;
	}
	
	this.getButtonWithImage = function(src, clickEvent, text, tooltip) {
		var $btn = $("<a>", { 'class' : 'btn btn-default' });
		$btn.append($("<img>", { 'src' : src, 'style' : 'width:16px; height:16px;'}));
		$btn.click(clickEvent);
		if(text) {
			$btn.append("&nbsp;").append(text);
		}
		if(tooltip) {
			$btn.attr("title", tooltip);
			$btn.tooltipster();
		}
		return $btn;
	}
	
	this.getButtonWithText = function(text, clickEvent, btnClass) {
		var auxBtnClass = "btn-default";
		if(btnClass) {
			auxBtnClass = btnClass;
		}
		var $pinBtn = $("<a>", { 'class' : 'btn ' + auxBtnClass });
		$pinBtn.append(text);
		$pinBtn.click(clickEvent);
		return $pinBtn;
	}
	
	this.getFormAwesomeIcon = function(iconClass) {
		return $("<i>", { 'class' : 'fa ' + iconClass });
	}
	
	this.getButtonWithIcon = function(iconClass, clickEvent, text, tooltip) {
		var $btn = $("<a>", { 'class' : 'btn btn-default' }).append($("<span>", { 'class' : 'glyphicon ' + iconClass }));
		if(text) {
			$btn.append("&nbsp;").append(text);
		}
		if(tooltip) {
			$btn.attr("title", tooltip);
			$btn.tooltipster();
		}
		$btn.click(clickEvent);
		return $btn;
	}
	
	/**
	 * @param {string} settingLoadedCallback Can be used to avoid flickering. Only called if dontRestoreState is not true.
	 * @param {string} dontRestoreState Sets the state to collaped and doesn't load it from server.
	 */
	this.getShowHideButton = function($elementToHide, key, dontRestoreState, settingLoadedCallback) {

		var glyphicon = dontRestoreState ? "glyphicon-chevron-right" : 'glyphicon-chevron-down';

		var $showHideButton = FormUtil.getButtonWithIcon(glyphicon, function() {
			$elementToHide.slideToggle();

			var $thisButton = $($(this).children()[0]);
			
			if($thisButton.hasClass("glyphicon-chevron-right")) {
				$thisButton.removeClass("glyphicon-chevron-right");
				$thisButton.addClass("glyphicon-chevron-down");
				mainController.serverFacade.setSetting(key,"true");
			} else {
				$thisButton.removeClass("glyphicon-chevron-down");
				$thisButton.addClass("glyphicon-chevron-right");
				mainController.serverFacade.setSetting(key,"false");
			}
			
		}, null, "Show/Hide section");
		
		if (dontRestoreState) {
			$elementToHide.hide();
		} else {
			mainController.serverFacade.getSetting(key, function(value) {
				if(value === "false") {
					var $thisButton = $($showHideButton.children()[0]);
					$thisButton.removeClass("glyphicon-chevron-down");
					$thisButton.addClass("glyphicon-chevron-right");
					$elementToHide.toggle();
				}
				if (settingLoadedCallback) {
					settingLoadedCallback();
				}
			});
		}
		
		$showHideButton.addClass("btn-showhide");
		$showHideButton.css({ "border" : "none", "margin-bottom" : "4px", "margin-left" : "-11px" });

		return $showHideButton;
	}
	
	this.getHierarchyButton = function(permId) {
		var $hierarchyButton = $("<a>", { 'class' : 'btn btn-default'} )
									.append($('<img>', { 'src' : './img/hierarchy-icon.png', 'style' : 'width:16px; height:17px;' }))
									.append(' G');
		$hierarchyButton.click(function() {
			mainController.changeView('showSampleHierarchyPage', permId);
		});
		return $hierarchyButton;
	}
	
	//
	// Get Field with container to obtain a correct layout
	//
	this.getFieldForComponentWithLabel = function($component, label, postComponent, isInline) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		var requiredText = '';
		if($component.attr('required')) {
			requiredText = "&nbsp;(*)"
		}
		
		var labelText = "";
		if(label) {
			labelText = label + requiredText + ":";
		}
		var labelColumnClass = ""
		if(!isInline) {
			labelColumnClass = this.labelColumnClass;
		}
		var $controlLabel = $('<label>', { class : 'control-label' }).html(labelText);
		
		var controlColumnClass = ""
		if(!isInline) {
			controlColumnClass = this.controlColumnClass;
		}
		var $controls = $('<div>', { class : 'controls' });
		
		if(label) {
			$controlGroup.append($controlLabel);
		}
		
		if(isInline) {
			$controlGroup.append($component);
		} else {
			$controls.append($component);
			$controlGroup.append($controls);
		}
		
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		if(isInline) {
			return $controlGroup;
		} else {
			return $fieldset;
		}
	}
	
	this.getFieldForLabelWithText = function(label, text, id, postComponent, cssForText) {
		var $fieldset = $('<div>');
		
		var $controlGroup = $('<div>', {class : 'form-group'});
		
		var $controlLabel = $('<label>', {class : 'control-label' });
		
		if(label) {
			$controlLabel.text(label + ":");
		}
		
		var $controls = $('<div>', {class : 'controls' });
		
		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);
		
		var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent; word-wrap: break-word;'}); //white-space: pre-wrap;
		if(cssForText) {
			$component.css(cssForText);
		}
		
		if(text) {
			text = text.replace(/(?:\r\n|\r|\n)/g, '\n'); //Normalise carriage returns
		}
		$component.html(html.sanitize(text));
		
		if(id) {
			$component.attr('id', id);
		}
		$controls.append($component);
		
		return $fieldset;
	}

	//
	// Get Field from property
	//
	this.getVocabularyLabelForTermCode = function(propertyType, termCode) {
		var vocabulary = propertyType.vocabulary;
		if(vocabulary) {
			for(var tIdx = 0; tIdx < vocabulary.terms.length; tIdx++) {
				if(vocabulary.terms[tIdx].code === termCode) {
					return vocabulary.terms[tIdx].label;
				}
			}
		}
		return termCode;
	}
	
	this.getFieldForPropertyType = function(propertyType, timestampValue) {
		var $component = null;
		if (propertyType.dataType === "BOOLEAN") {
			$component = this._getBooleanField(propertyType.code, propertyType.description);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			var vocabulary = profile.getVocabularyByCode(propertyType.vocabulary.code);
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
			$component = this._getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory, timestampValue);
		} else if (propertyType.dataType === "VARCHAR") {
			$component = this._getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "XML") {
			$component = this._getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		}
		
		return $component;
	}
	
	//
	// Read/Write Fields
	//
	this.setFieldValue = function(propertyType, $field, value) {
		if(propertyType.dataType === "BOOLEAN") {
			$($($field.children()[0]).children()[0]).prop('checked', value === "true");
		} else if(propertyType.dataType === "TIMESTAMP") {
			$($($field.children()[0]).children()[0]).val(value);
		} else {
			$field.val(value);
		}
	}
	
	this.getFieldValue = function(propertyType, $field) {
		var propertyTypeValue;
		if (propertyType.dataType === "BOOLEAN") {
			propertyTypeValue = $field.children().is(":checked");
		} else {
			propertyTypeValue = $field.val();
		}
		return propertyTypeValue;
	}
	
	//
	// Form Fields
	//
	this._getBooleanField = function(id, alt) {
		return $('<div>', {'class' : 'checkbox'}).append($('<label>').append($('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt, 'placeholder' : alt })));
	}
	
	this.getDropDownForTerms = function(id, terms, alt, isRequired) {
		return this._getDropDownFieldForVocabulary(id, terms, alt, isRequired);
	}
	
	this._getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
		var $component = $("<select>", {'placeholder' : alt, 'class' : 'form-control'});
		$component.attr('id', code);
		
		if (isRequired) {
			$component.attr('required', '');
		}
		
		$component.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text(alt));
		for(var i = 0; i < terms.length; i++) {
			$component.append($("<option>").attr('value',terms[i].code).text(terms[i].label));
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this.getTextInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, null, isRequired);
	}
	
	this.getRealInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, 0.01, isRequired);
	}
	
	this.getIntegerInputField = function(id, alt, isRequired) {
		return this._getInputField('text', id, alt, 1, isRequired);
	}
	
	this._getInputField = function(type, id, alt, step, isRequired) {
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt, 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (step) {
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
	
	this._getDatePickerField = function(id, alt, isRequired, value) {
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
		
		var date = null;
		if(value) {
			date = Util.parseDate(value);
		}
		
		var datetimepicker = $subComponent.datetimepicker({ 
			format : 'YYYY-MM-DD HH:mm:ss', 
			useCurrent : false,
			defaultDate : date
		});
		
		
		
		$component.append($subComponent);
		
		return $component;
	}
	
	
	//
	// Rich Text Editor Support - (CKEditor)
	//
	CKEDITOR.on( 'instanceReady', function( ev ) {
		ev.editor.config.filebrowserUploadUrl = "/openbis/openbis/file-service/eln-lims?sessionID=" + mainController.serverFacade.getSession();
		ev.editor.dataProcessor.writer.selfClosingEnd = ' />';
		ev.editor.document.on('drop', function (ev) {
		      ev.data.preventDefault(true);
		});
	});
	
	this.activateRichTextProperties = function($component, componentOnChange, propertyType) {
		
		if(profile.isForcedMonospaceFont(propertyType)) {
			$component.css("font-family", "Consolas, Monaco, Lucida Console, Liberation Mono, DejaVu Sans Mono, Bitstream Vera Sans Mono, Courier New, monospace");
		}
		
		if(profile.isForcedDisableRTF(propertyType)) {
			$component.change(function(event) {
				componentOnChange(event, $(this).val());
			});
		} else {
			var editor = $component.ckeditor().editor;
			editor.on('change', function(event) {
				var value = event.editor.getData();
				componentOnChange(event, value);
			});
		}
		
		return $component;
	}
	
	this.fixStringPropertiesForForm = function(propertyType, entity) {
		var originalValue = entity.properties[propertyType.code];
		if (propertyType.dataType !== "XML") {
			entity.properties[propertyType.code] = this.sanitizeRichHTMLText(originalValue);
		}
	}
	
	this.sanitizeRichHTMLText = function(originalValue) {
		if(typeof originalValue === "string") {
			//Take envelope out if pressent
			var bodyStart = originalValue.indexOf("<body>");
			var bodyEnd = originalValue.indexOf("</body>");
			if(bodyStart !== -1 && bodyEnd !== -1) {
				originalValue = originalValue.substring(bodyStart + 6, bodyEnd);
			}
			//Clean the contents
			originalValue = html.sanitize(originalValue);
		}
		return originalValue;
	}
	
	this.getToolbar = function(toolbarModel) {
		var $toolbarContainer = $("<div>", { class : 'toolBox', style : "width: 100%;" });
		
		for(var tbIdx = 0; tbIdx < toolbarModel.length; tbIdx++) {
			var $toolbarComponent = toolbarModel[tbIdx].component;
			var toolbarComponentTooltip = toolbarModel[tbIdx].tooltip;
			var $toolbarComponentTooltip = toolbarModel[tbIdx].$tooltip;
			if(toolbarComponentTooltip) {
				$toolbarComponent.attr("title", toolbarComponentTooltip);
				$toolbarComponent.tooltipster();
			} else if ($toolbarComponentTooltip) {
				$toolbarComponent.tooltipster({
					content: $toolbarComponentTooltip
				});
			}
			$toolbarContainer.append($toolbarComponent);
			$toolbarContainer.append("&nbsp;");
		}
		
		return $toolbarContainer;
	}
	
	this.getOperationsMenu = function(items) {
		var $dropDownMenu = $("<span>", { class : 'dropdown' });
		var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
		var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
		$dropDownMenu.append($caret);
		$dropDownMenu.append($list);
		
		for(var iIdx = 0; iIdx < items.length; iIdx++) {
			var item = items[iIdx];
			var $item = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : item.label}).append(item.label));
			$item.click(item.event);
			$list.append($item);
		}
		return $dropDownMenu;
	}
	
	this.getFormLink = function(displayName, entityKind, permIdOrIdentifier, paginationInfo) {
		var view = null;
		switch(entityKind) {
			case "Space":
				view = "showSpacePage";
				break;
			case "Project":
				view = "showProjectPageFromIdentifier";
				break;
			case "Experiment":
				view = "showExperimentPageFromIdentifier";
				break;
			case "Sample":
				if(permIdOrIdentifier.lastIndexOf("/") !== -1) {
					view = "showViewSamplePageFromIdentifier";
				} else {
					view = "showViewSamplePageFromPermId";
				}
				break;
			case "DataSet":
				view = "showViewDataSetPageFromPermId";
				break;
		}
		
		var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), view, permIdOrIdentifier);
		var click = function() {
			var arg = null;
			if(paginationInfo) {
				arg = {
						permIdOrIdentifier : permIdOrIdentifier,
						paginationInfo : paginationInfo
				}
			} else {
				arg = permIdOrIdentifier;
			}
			mainController.changeView(view, arg, true);
		}
		displayName = String(displayName).replace(/<(?:.|\n)*?>/gm, ''); //Clean any HTML tags
		var link = $("<a>", { "href" : href, "class" : "browser-compatible-javascript-link" }).text(displayName);
		link.click(click);
		return link;
	}
	
	this.getFormPath = function(spaceCode, projectCode, experimentCode, containerSampleCode, containerSampleIdentifierOrPermId, sampleCode, sampleIdentifierOrPermId, datasetCodeAndPermId) {
		var entityPath = $("<span>");
		if(spaceCode) {
			entityPath.append("/").append(this.getFormLink(spaceCode, 'Space', spaceCode));
		}
		if(projectCode) {
			entityPath.append("/").append(this.getFormLink(projectCode, 'Project', IdentifierUtil.getProjectIdentifier(spaceCode, projectCode)));
		}
		if(experimentCode) {
			entityPath.append("/").append(this.getFormLink(experimentCode, 'Experiment', IdentifierUtil.getExperimentIdentifier(spaceCode, projectCode, experimentCode)));
		}
		if(sampleCode && sampleIdentifierOrPermId) {
			entityPath.append("/");
			if(containerSampleCode && containerSampleIdentifierOrPermId) {
				entityPath.append(this.getFormLink(containerSampleCode, 'Sample', containerSampleIdentifierOrPermId)).append(":");;
			}
			entityPath.append(this.getFormLink(sampleCode, 'Sample', sampleIdentifierOrPermId));
		}
		if(datasetCodeAndPermId) {
			entityPath.append("/").append(this.getFormLink(datasetCodeAndPermId, 'DataSet', datasetCodeAndPermId));
		}
		return entityPath;
	}
	
	this.getBox = function() {
		var $box = $("<div>", { style : "background-color:#f8f8f8; padding:10px; border-color: #e7e7e7; border-style: solid; border-width: 1px;"});
		return $box;
	}
	
	this.getInfoBox = function(title, lines) {
		var $infoBox = this.getBox();
		
		$infoBox.append($("<span>", { class : 'glyphicon glyphicon-info-sign' })).append(" " + title);
		for(var lIdx = 0; lIdx < lines.length; lIdx++) {
			$infoBox.append($("<br>"));
			$infoBox.append(lines[lIdx]);
		}
		return $infoBox;
	}
	
	this.isInteger = function(str) {
    	var n = ~~Number(str);
    	return String(n) === str;
	}
	
	this.isNumber = function(str) {
    	var n = Number(str);
    	return String(n) === str;
	}

	//
	// errors
	//

	// errors: array of strings
	this._getSanitizedErrorString = function(title, errors) {
		var $container = $("<div>");
        $container.append($("<h3>").text(title));
		var $ul = $("<ul>");
		for (var error of errors) {
			$ul.append($("<li>").text(error));
		}
		$container.append($ul);
		return $container.html();
	}

	//
	// Dropbox folder name dialog
	//

	/**
	 * @param {string[]} nameElements - First elements of the folder name.
	 * @param {string} nodeType - "Sample" or "Experiment".
	 */
	this.showDropboxFolderNameDialog = function(nameElements) {

		var $dialog = $("<div>");
		$dialog
			.append($("<div>")
				.append($("<legend>").text("Helper tool for Dataset upload using the eln-lims dropbox:")));

		mainController.serverFacade.listDataSetTypes((function(data) {

			var dataSetTypes = data.result;

			var $formFieldContainer = $("<div>");
			$dialog.append($formFieldContainer);

			// info text
			$formFieldContainer.append(FormUtil.getInfoText("Example and usage instructions: "))
								.append("<center><img src='./img/eln-lims-dropbox-example.png' width='80%' ></center>")
								.append("<center><b>Screenshot example showing the eln-lims dropbox network folder and how the results will be visualized in the ELN after upload</b></center>")
								.append("The eln-lims dropbox requires a root folder with a specific name. This name contains information on where the data should be uploaded.").append("<br>")
								.append("1. Generate the name of the root folder with this helper tool using the form below.").append("<br>")
								.append("2. The root folder should contain another folder, with a name of your choice, with the data to upload. This can have as many layers as needed.").append("<br>")
								.append("3. The upload will be triggered automatically and the data will become visible in the object/experiment to which it was uploaded.").append("<br>");
								

			// dataset type dropdown
			var $dataSetTypeSelector = FormUtil.getDataSetsDropDown('DATASET_TYPE', dataSetTypes);
			$dataSetTypeSelector.attr("id", "dataSetTypeSelector");
			$formFieldContainer
				.append($("<div>", { class : "form-group" })
					.append($("<label>", { class : "control-label" }).text("Dataset type:")))
					.append($dataSetTypeSelector);

			// name
			var $nameInput = $("<input>", { type : "text", id : "nameInput", class : "form-control", disabled : true });
			$formFieldContainer
				.append($("<div>", { class : "form-group" })
					.append($("<label>", { class : "control-label" }).text("Dataset name:")))
					.append($nameInput);

			var ownerHint = "to upload data to the current ";
			if(nameElements[0] === "O") {
				ownerHint += ELNDictionary.Sample;
			} else if(nameElements[0] === "E") {
				ownerHint += ELNDictionary.ExperimentELN;
			}
			ownerHint += " " + nameElements[nameElements.length-1];
			
			// dropbox folder name ouput
			var $dropboxFolderName = $("<input>", {
				class : "form-control",
				id : "dropboxFolderName",
				readonly : "readonly",
				type : "text",
				value :  this._getDropboxFolderName(nameElements),
				onClick : "this.setSelectionRange(0, this.value.length)"
			});
			// copy to clipboard button
			var $copyToClipboardButton = FormUtil.getButtonWithIcon("glyphicon-copy");
			$copyToClipboardButton.attr("id", "copyToClipboardButton");
			$formFieldContainer
				.append($("<div>", { class : "form-group" })
					.append($("<label>", { class : "control-label" })
						.text("Generated root folder name for the dropbox " + ownerHint + ":"))
					.append($("<div>", { class : "input-group" })
						.append($dropboxFolderName)
						.append($("<span>", { class : "input-group-btn"})
							.append($copyToClipboardButton))));

			// close button
			var $cancelButton = $("<a>", {
				class : "btn btn-default",
				id : "dropboxFolderNameClose"
			}).text("Close").css("margin-top", "15px");
			$dialog.append($cancelButton);
			Util.blockUI($dialog.html(), this.getDialogCss());

			// attach events
			$dataSetTypeSelector = $("#dataSetTypeSelector");
			Select2Manager.add($dataSetTypeSelector);
			$nameInput = $("#nameInput");
			$dropboxFolderName = $("#dropboxFolderName");
			$copyToClipboardButton = $("#copyToClipboardButton");
			$("#dropboxFolderNameClose").on("click", function(event) {
				Util.unblockUI();
			});
			// update folder name on type selector / name change
			$dataSetTypeSelector.change((function() {
				var dataSetTypeCode = $dataSetTypeSelector.val();
				var name = null;
				// dataset type code UNKNOWN has no name
				if (dataSetTypeCode === "UNKNOWN") {
					$nameInput.val("");
					$nameInput.attr("disabled", "true");
				} else {
					var name = $nameInput.val();
					$nameInput.removeAttr("disabled");					
				}
				var folderName = this._getDropboxFolderName(nameElements, dataSetTypeCode, name);
				$dropboxFolderName.val(folderName);
			}).bind(this));
			$nameInput.on("input", (function() {
				var dataSetTypeCode = $dataSetTypeSelector.val();
				var name = $nameInput.val();
				var folderName = this._getDropboxFolderName(nameElements, dataSetTypeCode, name);
				$dropboxFolderName.val(folderName);
			}).bind(this));
			// copy to clipboard
			$copyToClipboardButton.on("click", function() {
				$dropboxFolderName.select();
				document.execCommand("copy");
			});
			$copyToClipboardButton.attr("title", "copy to clipboard");
			$copyToClipboardButton.tooltipster();

		}).bind(this));
	}

	this._getDropboxFolderName = function(nameElements, dataSetTypeCode, name) {
		var folderName = nameElements.join("+");
		for (var optionalPart of [dataSetTypeCode, name]) {
			if (optionalPart) {
				folderName += "+" + optionalPart;				
			}
		}
		return folderName;
	}

	this.getDialogCss = function() {
		return {
				'text-align' : 'left',
				'top' : '5%',
				'width' : '90%',
				'left' : '5%',
				'right' : '5%',
				'overflow' : 'auto'
		};
	}

	this.getInfoText = function(infoText) {
		return $("<p>")
			.append($("<div>", { class : "glyphicon glyphicon-info-sign" })
				.css("margin-right", "3px"))
			.append($("<span>").text(infoText));
	}

    //
    // DSS disk space usage dialog
    //

	this.showDiskSpaceDialog = function() {
		var _this = this;

		Util.blockUI(null, null, true);

		mainController.serverFacade.customELNApi({
			"method" : "getDiskSpace",
			"diskMountPoints" : _this.profile.diskMountPoints,
		}, function(error, result){
			if (error) {
				Util.showError("Could not get disk space information.");
			} else {

				var $dialog = $("<div>");
				$dialog
					.append($("<div>")
						.append($("<legend>").text("Available storage space:")));

				var $formFieldContainer = $("<div>");
				$dialog.append($formFieldContainer);

				// close button
				var $closeButton = $("<a>", {
					class : "btn btn-default",
					id : "dropboxFolderNameClose"
				}).text("Close").css("margin-top", "15px");
				$dialog.append($closeButton);

				// add disk space
				var rowHeight = "50px";
				var barHeight = "30px";

				var $table  = $("<table>");
				$table
					.append($("<thead>")
						.append($("<tr>").css("height", rowHeight)
							.append($("<th>").text("Mount point").css("width", "30%"))
							.append($("<th>").text("Size").css("width", "10%"))
							.append($("<th>").text("Used").css("width", "10%"))
							.append($("<th>").text("Available").css("width", "10%"))
							.append($("<th>").text("Percentage").css("width", "40%"))
					));
				$table.css({
					width : "90%",
					"margin-top" : "25px",
					"margin-bottom" : "25px",
					"margin-left" : "auto",
					"margin-right" : "auto",
				});
				$tbody = $("<tbody>");
				$table.append($tbody);
				$formFieldContainer.append($table);

				var diskSpaceValues = result.data;
				for (var i=0; i<diskSpaceValues.length; i++) {
					var filesystem = diskSpaceValues[i]["Mounted_on"]
					var size = diskSpaceValues[i]["Size"]
					var used = diskSpaceValues[i]["Used"]
					var avail = diskSpaceValues[i]["Avail"]
					var usedPercentage = diskSpaceValues[i]["UsedPercentage"]

					var $diskSpaceSection = $("<div>");
					var $total = $("<div>").css({
						height : barHeight,
						width : "100%",
						"background-color" : "lightgray",
						"border-radius" : "7px",
						"text-align" : "center",
						"vertical-align" : "middle",
						"line-height" : barHeight,
					});
					$total.text(usedPercentage);
					var $used = $("<div>").css({
						height: barHeight,
						width : usedPercentage,
						"background-color" : "lightblue",
						"border-radius" : "7px",
						"margin-top" : "-" + barHeight
					});
					$diskSpaceSection.append($total).append($used);

					$tbody
						.append($("<tr>").css("height", rowHeight)
							.append($("<td>").text(filesystem))
							.append($("<td>").text(size))
							.append($("<td>").text(used))
							.append($("<td>").text(avail))
							.append($("<td>").append($diskSpaceSection))
					);
				}

				Util.blockUI($dialog.html(), _this.getDialogCss());

				// events
				$("#dropboxFolderNameClose").on("click", function(event) {
					Util.unblockUI();
				});

			}
		});
	}
	
	this.getPermId = function(entity) {
		var permId = null;
		if(entity["@type"].startsWith("as.dto")) { //v3
			permId = entity.permId.permId;
		} else { // v1
			permId = entity.permId;
		}
		return permId;
	}
	
	this.getType = function(entity) {
		var type = null;
		if(entity["@type"].startsWith("as.dto")) { //v3
			type = entity.type.code;
		} else if(entity.sampleTypeCode) { // v1 sample
			type = entity.sampleTypeCode;
		}
		return type;
	}

	// share project or space with user or group
	// params.title
	// params.components: array of compoments (info, input fields etc.)
	// params.focusedComponent: component which gains focus
	// params.buttons: array of buttons
	// params.css: css as a map
	// params.callback: function to be called on submit
	this.showDialog = function(params) {

		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		$window.submit(params.callback);

		$window.append($('<legend>').append(params.title));

		for (var i=0; i<params.components.length; i++) {
			$window.append($('<p>').append(params.components[i]));
		}
		var $buttons = $('<p>');
		for (var i=0; i<params.buttons.length; i++) {
			$buttons.append(params.buttons[i]);
			$buttons.append('&nbsp;');
		}
		$window.append($buttons);

		Util.blockUI($window, params.css, false, function() {
			if (params.focuseComponent) {
				params.focuseComponent.focus();
			}
		});
	}

	// params.space: space code (set this or project)
	// params.project: project code (set this or space)
	// params.acceptCallback: function to be called with (shareWith, groupOrUser)
	this.showAuthorizationDialog = function(params) {

		var _this = this;
		Util.blockUI();

		mainController.serverFacade.searchRoleAssignments({
			space: params.space ? params.space.code : null,
			project: params.project ? params.project.code : null,
		}, function(roleAssignments) {

			Util.unblockUI();

			// components
			var $roleAssignmentTable = _this._getRoleAssignmentTable(roleAssignments, _this._revokeRoleAssignment.bind(_this, params));
			var spaceOrProjectLabel = params.space ? params.space.code : params.project.code;
			var $roleDropdown = FormUtil.getDropdown([
				{ label: 'Observer', value: 'OBSERVER', selected: true },
				{ label: 'User', value: 'USER' },
				{ label: 'Admin', value: 'ADMIN' },
			]);
			var $role = FormUtil.getFieldForComponentWithLabel($roleDropdown, 'Role');
			var $grantToDropdown = FormUtil.getDropdown([
				{ label: 'Group', value: 'Group', selected: true },
				{ label: 'User', value: 'User', selected: true },
			]);
			var $shareWith = FormUtil.getFieldForComponentWithLabel($grantToDropdown, 'grant to');
			var $groupOrUser = FormUtil.getTextInputField('id', 'Group or User');
			// buttons
			var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Grant access' });
			var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Close');
			$btnCancel.click(function() {
			    Util.unblockUI();
			});
			// dialog
			_this.showDialog({
				title: 'Manage access to ' + spaceOrProjectLabel,
				components: [$roleAssignmentTable, $role, $shareWith, $groupOrUser],
				focuseComponent: $groupOrUser,
				buttons: [$btnAccept, $btnCancel],
				css: {'text-align': 'left', 'top': '15%'},
				callback: function() {
					if ($groupOrUser.val() == null || $groupOrUser.val().length == 0) {
						alert("Please enter a user or group name.");
					} else {
						_this._grantRoleAssignment(params, $roleDropdown.val(), $grantToDropdown.val(), $groupOrUser.val());
					}
				},
			});

		});
	}

	this._getRoleAssignmentTable = function(roleAssignments, revokeAction) {
		if (roleAssignments.length == 0) {
			return $('<span>');
		}
		var $table = $('<table>').css({'margin-top': '20px'});
		var $thead = $('<thead>')
			.append($('<tr>')
				.append($('<th>').text('User'))
				.append($('<th>').text('Group'))
				.append($('<th>').text('Role'))
				.append($('<th>')
			));
		var $tbody = $('<tbody>');
		for (var i=0; i<roleAssignments.length; i++) {
			var user = roleAssignments[i].user ? roleAssignments[i].user.userId : '';
			var group = roleAssignments[i].authorizationGroup ? roleAssignments[i].authorizationGroup.code : '';
			var role = roleAssignments[i].role;
			var roleAssignmentTechId = roleAssignments[i].id;
			var $revokeButton = this.getButtonWithIcon('glyphicon-remove', revokeAction.bind(this, roleAssignmentTechId), null, 'revoke');
			$revokeButton.css({'margin-top': '5px'});
			$tbody.append($('<tr>')
				.append($('<td>').text(user))
				.append($('<td>').text(group))
				.append($('<td>').text(role))
				.append($('<td>').append($revokeButton)));
		}
		$table.append($thead).append($tbody);
		$table.css({ width: '100%' });
		return $table;
	}

	this._grantRoleAssignment = function(dialogParams, role, grantTo, groupOrUser) {
		var _this = this;
		mainController.authorizeUserOrGroup({
			user: grantTo == "User" ? groupOrUser : null,
			group: grantTo == "Group" ? groupOrUser.toUpperCase() : null,
			role: role,
			space: dialogParams.space ? dialogParams.space.code : null,
			project: dialogParams.project ? dialogParams.project.permId : null,
		}, function(success, result) {
			if (success) {
				Util.showSuccess("Access granted.");
				_this.showAuthorizationDialog(dialogParams);
		} else {
				Util.showUserError(result, function() {}, true);
			}
		});
	}

	this._revokeRoleAssignment = function(dialogParams, roleAssignmentTechId) {
		var _this = this;
		mainController.deleteRoleAssignment(roleAssignmentTechId, function(success, result) {
			if (success) {
				Util.showSuccess("Access revoked.");
				_this.showAuthorizationDialog(dialogParams);
			} else {
				Util.showUserError(result, function() {}, true);
			}
		});
	}

	this.getExportButton = function(exportConfig, metadataOnly, includeRoot) {
			$export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
					Util.blockUI();
					var facade = mainController.serverFacade;
					facade.exportAll(exportConfig, (includeRoot)?true:false, metadataOnly, function(error, result) {
						if(error) {
							Util.showError(error);
						} else {
							Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
						}
					});
			});
			if(metadataOnly) {
				$export.append(" M");
			}
			return $export;
	};
}
