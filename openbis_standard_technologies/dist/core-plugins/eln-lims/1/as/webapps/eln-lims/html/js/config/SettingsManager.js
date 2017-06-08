function SettingsManager(serverFacade) {

    this._serverFacade = serverFacade;

	this.validateAndsave = function(settingsSample, settings, doneCallback) {
		var _this = this;
		var errors = this._validateSettings(settings);
		if (errors.length > 0) {
			Util.showError(FormUtil._getSanitizedErrorString("Settings validation errors:", errors));
		} else {
			settingsSample.properties = { "ELN_SETTINGS" : JSON.stringify(settings) };
			this._serverFacade.updateSample(settingsSample, function(ok) {
				if(ok) {
					_this.applySettingsToProfile(settings, profile);
					doneCallback();
				}
			});
		}
	}

	this.loadSettings = function(callback) {
		this._serverFacade.searchSamples({ "sampleIdentifier" : "/ELN_SETTINGS/GENERAL_ELN_SETTINGS", "withProperties" : true }, (function(data) {
			if (data && data[0] && data[0].properties && data[0].properties.ELN_SETTINGS) {
				var settings = JSON.parse(data[0].properties.ELN_SETTINGS);
				callback(settings);
			} else {
				callback();
			}
		}).bind(this))
	}


	// Loads settings and logs validation errors to console.
	// Applies the settings to the profile even if they are invalid.
    this.loadSettingsAndApplyToProfile = function(doneCallback) {
		this.loadSettings((function(settings) {
			if (settings) {
				var errors = this._validateSettings(settings);
				if (errors.length > 0) {
					console.log("The settings contain the following errors:");
					console.log(errors);
				}
				this.applySettingsToProfile(settings, profile);
			}
			doneCallback();
		}).bind(this));
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

	this.applySettingsToProfile = function(settings, targetProfile) {
		// fields to be overwritten
		var fields = [
			"dataSetTypeForFileNameMap",
			"forcedDisableRTF",
			"forceMonospaceFont",
			"inventorySpaces",
			"sampleTypeProtocols",
		];
		for (var field of fields) {
			if (settings[field]) {
				targetProfile[field] = settings[field];
			}
		}
		// main menu
		for (var menuItem of Object.keys(targetProfile.mainMenu)) {
			if (settings.mainMenu[menuItem] != undefined) {
				targetProfile.mainMenu[menuItem] = settings.mainMenu[menuItem];
			}
		}
		// sampleTypeDefinitionsExtension
		for (var sampleType of Object.keys(settings.sampleTypeDefinitionsExtension)) {
			profile.sampleTypeDefinitionsExtension[sampleType] = settings.sampleTypeDefinitionsExtension[sampleType];
		}
	}

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
				var errorPrefix = "Error in definitions extension for sample type " + sampleType + ": ";
				for (var hintType of Object.keys(hints)) {
					if (hintType == "undefined") {
						errors.push(errorPrefix + "Hint type (children/parents) is undefined");
					}
					if (hintType === "SAMPLE_CHILDREN_HINT" || hintType === "SAMPLE_PARENTS_HINT") {
						var hintArray = hints[hintType];
						for (var hint of hintArray) {
							if (hint.LABEL == null || hint.LABEL.length === 0) {
								errors.push(errorPrefix + "Sample type definitions hint labels can't be empty.");
							}
							if (typeof(hint.TYPE) !== "string") {
								errors.push(errorPrefix + "Sample type definitions hint type (" +
											hint.TYPE +
											 ") must be a string.");
							} else if (sampleTypeExists && this.getSampleTypeOptions().indexOf(hint.TYPE) === -1) {
								errors.push(errorPrefix + "Sample type definitions hint type (" +
											hint.TYPE +
											 ") must be an existing property type.");
							}
							if (hint.MIN_COUNT != null && (typeof(hint.MIN_COUNT) !== "number" || isNaN(hint.MIN_COUNT))) {
								errors.push(errorPrefix + "Sample type definitions hint MIN_COUNT must be a number.");
							}
							if (hint.MAX_COUNT != null && (typeof(hint.MAX_COUNT) !== "number" || isNaN(hint.MAX_COUNT))) {
								errors.push(errorPrefix + "Sample type definitions hint MAX_COUNT must be a number.");
							}
							if (hint.ANNOTATION_PROPERTIES) {
								var propertyTypeOptions = this.getAnnotationPropertyTypeOptions();
								for (var annotationProperty of hint.ANNOTATION_PROPERTIES) {
									// debugger;
									if (sampleTypeExists && (annotationProperty.TYPE == null || propertyTypeOptions.indexOf(annotationProperty.TYPE) === -1)) {
										errors.push(errorPrefix + "Annotation properties must have an existing property type but is: " +
													annotationProperty.TYPE);
									}
									if (annotationProperty.MANDATORY == null || typeof(annotationProperty.MANDATORY) !== "boolean") {
										errors.push(errorPrefix + "Annotation properties must have a boolean MANDATORY field but is: "
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
