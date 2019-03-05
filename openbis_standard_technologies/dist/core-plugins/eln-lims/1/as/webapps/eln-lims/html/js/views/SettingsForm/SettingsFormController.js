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
	this._settingsManager = new SettingsManager(this._mainController.serverFacade);

	this.init = function(views) {
		var runningProfile = jQuery.extend(true, {}, profile);
		var profileToEdit = null;
		
		if(settingsSample.properties["$ELN_SETTINGS"]) {
			profileToEdit = JSON.parse(settingsSample.properties["$ELN_SETTINGS"]);
		} else {
			profileToEdit = runningProfile;
		}
		
		this._settingsManager.applySettingsToProfile(profileToEdit, runningProfile);
		this._settingsFormView.repaint(views, runningProfile);
	}

	this.save = function(settings) {
		this._settingsManager.validateAndsave(this._settingsFormModel.settingsSample, settings, (function() {
			this._mainController.changeView("showSettingsPage", this._settingsFormModel.settingsSample.identifier);
		}).bind(this));
	}

	this.getAllDatasetTypeCodeOptions = this._settingsManager.getAllDatasetTypeCodeOptions;
	this.getForcedDisableRTFOptions = this._settingsManager.getForcedDisableRTFOptions;
	this.getForcedMonospaceFontOptions = this._settingsManager.getForcedMonospaceFontOptions;
	this.getInventorySpacesOptions = this._settingsManager.getInventorySpacesOptions;
	this.getSampleTypeOptions = this._settingsManager.getSampleTypeOptions;
	this.getAnnotationPropertyTypeOptions = this._settingsManager.getAnnotationPropertyTypeOptions;

}
