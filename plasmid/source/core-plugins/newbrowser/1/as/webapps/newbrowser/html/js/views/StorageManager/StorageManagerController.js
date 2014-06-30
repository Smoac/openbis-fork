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
	
	var _this = this;
	this._storageManagerView.getMoveButton().click(function() {
		var fromModel = _this._storageFromController.getModel();
		if(!fromModel.boxContents) {
			Util.showError("Please select something to move.");
			return;
		}
		
		var toModel = _this._storageToController.getModel();
		if(!toModel.boxName) {
			Util.showError("Please select a box where to move.");
			return;
		}
		
		for(var i = 0; i < fromModel.boxContents.length; i++) {
			//alert(fromModel.boxContents[i].code + " to " + toModel.boxName+"(" + toModel.row + "," + toModel.column + ")");
			
			fromModel.boxContents[i].properties[fromModel.storagePropertyGroup.nameProperty] = null;
			fromModel.boxContents[i].properties[fromModel.storagePropertyGroup.rowProperty] = null;
			fromModel.boxContents[i].properties[fromModel.storagePropertyGroup.columnProperty] = null;
			fromModel.boxContents[i].properties[fromModel.storagePropertyGroup.boxProperty] = null;
			fromModel.boxContents[i].properties[fromModel.storagePropertyGroup.userProperty] = null;
			
			fromModel.boxContents[i].properties[toModel.storagePropertyGroup.nameProperty] = toModel.storageCode;
			fromModel.boxContents[i].properties[toModel.storagePropertyGroup.rowProperty] = toModel.row;
			fromModel.boxContents[i].properties[toModel.storagePropertyGroup.columnProperty] = toModel.column;
			fromModel.boxContents[i].properties[toModel.storagePropertyGroup.boxProperty] = toModel.boxName;
			fromModel.boxContents[i].properties[toModel.storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
			
			var sampleSpace = fromModel.boxContents[i].spaceCode;
			var sampleProject = null;
			var sampleExperiment = null;
			
			var experimentIdentifier = fromModel.boxContents[i].experimentIdentifierOrNull;
			if(experimentIdentifier) {
				sampleProject = experimentIdentifier.split("/")[2];
				sampleExperiment = experimentIdentifier.split("/")[3];
			}
			
			var parameters = {
					//API Method
					"method" : "updateSample",
					//Identification Info
					"sampleSpace" : sampleSpace,
					"sampleProject" : sampleProject,
					"sampleExperiment" : sampleExperiment,
					"sampleCode" : fromModel.boxContents[i].code,
					"sampleType" : fromModel.boxContents[i].sampleTypeCode,
					//Other Properties
					"sampleProperties" : fromModel.boxContents[i].properties,
					//Parent links
					"sampleParents": fromModel.boxContents[i].parents
			};
			
			if(profile.allDataStores.length > 0) {
				Util.blockUI();
				mainController.serverFacade.createReportFromAggregationService(profile.allDataStores[0].code, parameters, function(response) {
					if(response.error) { //Error Case 1
						Util.showError(response.error.message, function() {Util.unblockUI();});
					} else if (response.result.columns[1].title === "Error") { //Error Case 2
						var stacktrace = response.result.rows[0][1].value;
						Util.showStacktraceAsError(stacktrace);
					} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
						Util.showSuccess("Entities moved.");
					} else { //This should never happen
						Util.showError("Unknown Error.", function() {Util.unblockUI();});
					}
				});
			} else {
				Util.showError("No DSS available.", function() {Util.unblockUI();});
			}
		}
		
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