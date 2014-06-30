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

function StorageManagerController(mainController) {
	this._mainController = mainController;
	
	//Sub Views Setup
	this._storageFromController = new StorageController({
		title : "From Storage",
		storagePropertyGroupSelector : "on",
		storageSelector : "on",
		userSelector : "on",
		boxSelector: "on",
		rackSelector: "off",
		contentsSelector: "on"
	});
	
	this._storageToController = new StorageController({
		title : "To Storage",
		storagePropertyGroupSelector : "on",
		storageSelector : "on",
		userSelector : "on",
		boxSelector: "on",
		rackSelector: "on",
		contentsSelector: "off"
	});
	
	
	//Main View Setup
	this._storageManagerModel = new StorageManagerModel();
	this._storageManagerView = new StorageManagerView(this._storageManagerModel, this._storageFromController.getView(), this._storageToController.getView());
	
	this._storageManagerView.getMoveButton().click(function() {
		alert("Cool");
	});
	
	this.init = function($container) {
		if(!FormUtil.getDefaultStoragesDropDown("", true)) {
			Util.showError("You need to configure the storage options to manage them. :-)");
		} else {
			this._storageManagerView.repaint($container);
		}
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageManagerModel;
	}
	
	this.getView = function() {
		return this._storageManagerView;
	}
}