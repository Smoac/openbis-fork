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
		boxSizeSelector: "off",
		rackSelector: "off",
		contentsSelector: "on",
		positionSelector: "off"
	});
	
	this._storageToController = new StorageController({
		title : "To Storage",
		storagePropertyGroupSelector : "on",
		storageSelector : "on",
		userSelector : "on",
		boxSelector: "on",
		boxSizeSelector: "on",
		rackSelector: "on",
		contentsSelector: "off",
		positionSelector: "off"
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
		
		if(!toModel.boxSize) {
			Util.showError("Please choose a box size.");
			return;
		}
		
		if(toModel.boxSize) {
			var size = parseInt(toModel.boxSize.split("X")[0]) * parseInt(toModel.boxSize.split("X")[1]);
			if(size < fromModel.boxContents.length) {
				Util.showError("Please choose a box size that is big enough for everything you are moving.");
				return;
			}
		}
		
		var samplesToUpdateParams = [];
		for(var i = 0; i < fromModel.boxContents.length; i++) {
			var sample = fromModel.boxContents[i];
			
			//alert(fromModel.boxContents[i].code + " to " + toModel.boxName+"(" + toModel.row + "," + toModel.column + ")");
			
			//TODO: Delete property with the API, it gives error when putting a null on a int instead of deleting it
			sample.properties[fromModel.storagePropertyGroup.nameProperty] = "";
			sample.properties[fromModel.storagePropertyGroup.rowProperty] = "";
			sample.properties[fromModel.storagePropertyGroup.columnProperty] = "";
			sample.properties[fromModel.storagePropertyGroup.boxProperty] = "";
			sample.properties[fromModel.storagePropertyGroup.boxSizeProperty] = "";
			sample.properties[fromModel.storagePropertyGroup.userProperty] = "";
			sample.properties[fromModel.storagePropertyGroup.positionProperty] = "";
			
			//TODO: Assign position
			sample.properties[toModel.storagePropertyGroup.nameProperty] = toModel.storageCode;
			sample.properties[toModel.storagePropertyGroup.rowProperty] = toModel.row;
			sample.properties[toModel.storagePropertyGroup.columnProperty] = toModel.column;
			sample.properties[toModel.storagePropertyGroup.boxProperty] = toModel.boxName;
			sample.properties[toModel.storagePropertyGroup.boxSizeProperty] = toModel.boxSize
			sample.properties[toModel.storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
			sample.properties[toModel.storagePropertyGroup.positionProperty] = "";
			
			var sampleSpace = sample.spaceCode;
			var sampleProject = null;
			var sampleExperiment = null;
			
			var experimentIdentifier = sample.experimentIdentifierOrNull;
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
					"sampleCode" : sample.code,
					"sampleType" : sample.sampleTypeCode,
					//Other Properties
					"sampleProperties" : sample.properties,
					//Parent links
					"sampleParents": sample.parents
			};
			
			samplesToUpdateParams.push(parameters);
		}
		
		var updateCall = function(parameters) {
			if(profile.allDataStores.length > 0) {
				mainController.serverFacade.createReportFromAggregationService(profile.allDataStores[0].code, parameters, function(response) {
					if(response.error) { //Error Case 1
						Util.showError(response.error.message, function() {Util.unblockUI();});
					} else if (response.result.columns[1].title === "Error") { //Error Case 2
						var stacktrace = response.result.rows[0][1].value;
						Util.showStacktraceAsError(stacktrace);
					} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
						if(samplesToUpdateParams.length > 0) {
							updateCall(samplesToUpdateParams.pop());
						} else {
							Util.showSuccess("Entities moved.", function() {
								mainController.changeView("showStorageManager", null);
								Util.unblockUI();
							});
						}
					} else { //This should never happen
						Util.showError("Unknown Error.", function() {Util.unblockUI();});
					}
				});
			} else {
				Util.showError("No DSS available.", function() {Util.unblockUI();});
			}
		}
		
		if(samplesToUpdateParams.length > 0) {
			Util.blockUI();
			updateCall(samplesToUpdateParams.pop());
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