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

function SettingsFormController(mainController, settingsSample, mode) {
	this._mainController = mainController;
	this._settingsFormModel = new SettingsFormModel(settingsSample, mode);
	this._settingsFormView = new SettingsFormView(this, this._settingsFormModel);
	
	this.init = function(views) {
		this._settingsFormView.repaint(views);
	}

	this.save = function(settings) {
		var errors = this._validateSettings(settings);
		if (errors.length > 0) {
			alert(errors.join("\n\n"));
		} else {
			// TODO store settings
			this._applySettingsToProfile(settings);
			this._mainController.changeView("showSettingsPage");
		}
	}

	this.getAllDatasetTypeCodeOptions = function() {
		return profile.allDatasetTypeCodes;
	}

	this.getForcedDisableRTFOptions = function() {
		return profile.allPropertyTypes.map(function(_) { return _.code; });
	}

	this.getForcedMonospaceFontOptions = function() {
		return profile.allPropertyTypes.map(function(_) { return _.code; });
	}

	this.getSampleTypeProtocolsOptions = function() {
		return profile.allSampleTypes.map((sampleType) => {return sampleType.code})
	}

	this.getInventorySpacesOptions = function() {
		return profile.allSpaces;
	}

	this.getSampleTypeOptions = function() {
		return profile.getAllSampleTypes().map( function(_) { return _.code; } )
	}

	this.getAnnotationPropertyTypeOptions = function() {
		return profile.allPropertyTypes.map( function(_) { return _.code; } );
	}

	this._applySettingsToProfile = function(settings) {
		var fields = [
			"dataSetTypeForFileNameMap",
			"mainMenu",
			"forcedDisableRTF",
			"forceMonospaceFont",
			"inventorySpaces",
			"sampleTypeProtocols",
			"sampleTypeDefinitionsExtension",
		];
		for (var field of fields) {
			if (settings[field]) {
				profile[field] = settings[field];
			}
		}
	}

	// TODO move validation to some place where it can be called on application startup as well
	this._validateSettings = function(settings) {
		var errors = [];
		this._validateMainMenu(settings, errors);
		this._validateForcedDisableRTF(settings, errors);
		this._validateForcedMonospaceFont(settings, errors);
		this._validateInventorySpaces(settings, errors);
		this._validateDataSetTypeForFileNameMap(settings, errors);
		this._validateSampleTypeProtocols(settings, errors);
		this._validateSampleTypeDefinitionsExtension(settings, errors);
		return errors;
	}

	this._validateSampleTypeDefinitionsExtension = function(settings, errors) {
		if (settings.sampleTypeDefinitionsExtension) {
			for (var sampleType of Object.keys(settings.sampleTypeDefinitionsExtension)) {
				// not checking for valid sample type
				// config can contain entries which are not yet defined
				// but if sample type exists, more strict validation is used
				var sampleTypeExists = this.getSampleTypeOptions().indexOf(sampleType) !== -1;
				var hints = settings.sampleTypeDefinitionsExtension[sampleType];
				for (var hintType of Object.keys(hints)) {
					if (hintType === "SAMPLE_CHILDREN_HINT" || hintType === "SAMPLE_PARENTS_HINT") {
						var hintArray = hints[hintType];
						for (var hint of hintArray) {
							if (hint.LABEL == null || hint.LABEL.length === 0) {
								errors.push("Sample type definitions hint labels can't be empty.");
							}
							if (typeof(hint.TYPE) !== "string") {
								errors.push("Sample type definitions hint type (" +
											hint.TYPE +
											 ") must be a string.");
							}
							if (sampleTypeExists && this.getSampleTypeOptions().indexOf(hint.TYPE) === -1) {
								errors.push("Sample type definitions hint type (" +
											hint.TYPE +
											 ") must be an existing property type.");
							}
							if (hint.MIN_COUNT && typeof(hint.MIN_COUNT) !== "number") {
								errors.push("Sample type definitions hint MIN_COUNT must be a number but is: " +
											hint.MIN_COUNT);
							}
							if (hint.MAX_COUNT && typeof(hint.MAX_COUNT) !== "number") {
								errors.push("Sample type definitions hint MAX_COUNT must be a number but is: " +
											hint.MAX_COUNT);
							}
							if (hint.ANNOTATION_PROPERTIES) {
								var propertyTypeOptions = this.getAnnotationPropertyTypeOptions();
								for (var annotationProperty of hint.ANNOTATION_PROPERTIES) {
									// debugger;
									if (sampleTypeExists && (annotationProperty.TYPE == null || propertyTypeOptions.indexOf(annotationProperty.TYPE) === -1)) {
										errors.push("Annotation properties must have an existing property type but is: " +
													annotationProperty.TYPE);
									}
									if (annotationProperty.MANDATORY == null || typeof(annotationProperty.MANDATORY) !== "boolean") {
										errors.push("Annotation properties must have a boolean MANDATORY field but is: "
													+ annotationProperty.MANDATORY);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	this._validateMainMenu = function(settings, errors) {
		if (settings.mainMenu) {
			var availableMenuItems = Object.keys(profile.mainMenu);
			for (var menuItem of Object.keys(settings.mainMenu)) {
				if (availableMenuItems.indexOf(menuItem) === -1) {
					errors.push(menuItem + 
								" is not a Main Menu Item. Available items: " +
								availableMenuItems.join(", " +
								"."));
				}
				if (typeof(settings.mainMenu[menuItem]) !== "boolean") {
					errors.push(menuItem + ": " + settings.mainMenu[menuItem] + " is not of type 'boolean'.");
				}
			}
		}
	}

	this._validateForcedDisableRTF = function(settings, errors) {
		if (settings.forcedDisableRTF) {
			for (var item of settings.forcedDisableRTF) {
				if (this.getForcedDisableRTFOptions().indexOf(item) === -1) {
					errors.push(item + " is not a property type.");
				}
			}
		}
	}

	this._validateForcedMonospaceFont = function(settings, errors) {
		if (settings.forceMonospaceFont) {
			for (var item of settings.forceMonospaceFont) {
				if (this.getForcedMonospaceFontOptions().indexOf(item) === -1) {
					errors.push(item + " is not a property type.");
				}
			}
		}
	}

	this._validateInventorySpaces = function(settings, errors) {
		if (settings.inventorySpaces) {
			for (var item of settings.inventorySpaces) {
				if (this.getInventorySpacesOptions().indexOf(item) === -1) {
					errors.push(item + " is not space.");
				}
			}
		}
	}

	this._validateSampleTypeProtocols = function(settings, errors) {
		if (settings.sampleTypeProtocols) {
			for (var item of settings.sampleTypeProtocols) {
				if (this.getSampleTypeProtocolsOptions().indexOf(item) === -1) {
					errors.push(item + " is not a sample type protocol.");
				}
			}
		}		
	}

	this._validateDataSetTypeForFileNameMap = function(settings, errors) {
		if (settings.dataSetTypeForFileNameMap) {
			for (var dataSetTypeForFileName of settings.dataSetTypeForFileNameMap) {
				if (dataSetTypeForFileName.dataSetType == null) {
					errors.push("dataSetTypeForFileNameMap must contain a field named dataSetType.");
				} else if (this.getAllDatasetTypeCodeOptions().indexOf(dataSetTypeForFileName.dataSetType) === -1) {
					errors.push("Dataset type " + 
								dataSetTypeForFileName.dataSetType + 
								" is not allowed. Available types: " +
								this.getAllDatasetTypeCodeOptions().join(", ") +
								".");
				}
				if (dataSetTypeForFileName.fileNameExtension == null) {
					errors.push("dataSetTypeForFileNameMap must contain a field named fileNameExtension.");
				} else if (dataSetTypeForFileName.fileNameExtension.length == 0) {
					errors.push("Filename extension can't be empty.");
				}
			}
		}
	}

}
