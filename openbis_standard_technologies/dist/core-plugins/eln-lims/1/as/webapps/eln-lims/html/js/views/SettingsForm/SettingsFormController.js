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
		console.log("settings:");
		console.log(settings);
		var errors = this._validateSettings(settings);
		if (errors.length > 0) {
			// TODO handle errors
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
		return profile.allPropertyTypes.map(function(propertyType) {
			return propertyType.code;
		});
	}

	this.getForcedMonospaceFontOptions = function() {
		return profile.allPropertyTypes.map(function(propertyType) {
			return propertyType.code;
		});
	}

	this.getSampleTypeProtocolsOptions = function() {
		return profile.allSampleTypes.map((sampleType) => {return sampleType.code})
	}

	this.getInventorySpacesOptions = function() {
		return profile.allSpaces; // TODO is this correct? what about "STOCK_CATALOG"?
	}

	this._applySettingsToProfile = function(settings) {
		var fields = [
			"dataSetTypeForFileNameMap",
			"mainMenu",
			"forcedDisableRTF",
			"forceMonospaceFont",
			"inventorySpaces",
			"sampleTypeProtocols",
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
		return errors;
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
				if ( ! dataSetTypeForFileName.dataSetType) {
					errors.push("dataSetTypeForFileNameMap must contain a field named dataSetType.");
				} else if (this.getAllDatasetTypeCodeOptions().indexOf(dataSetTypeForFileName.dataSetType) === -1) {
					errors.push("Dataset type " + 
								dataSetTypeForFileName.dataSetType + 
								" is not allowed. Available types: " +
								this.getAllDatasetTypeCodeOptions().join(", ") +
								".");
				}
				if ( ! dataSetTypeForFileName.fileNameExtension) {
					errors.push("dataSetTypeForFileNameMap must contain a field named fileNameExtension.");
				} else if (dataSetTypeForFileName.fileNameExtension.length == 0) {
					errors.push("Filename extension can't be empty.");
				}
			}
		}
	}

}
