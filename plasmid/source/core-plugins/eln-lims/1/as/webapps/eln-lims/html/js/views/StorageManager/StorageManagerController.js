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
	//Upgraded storage model detection
	if(profile.storagesConfiguration["isEnabled"]) {
		var groups = profile.getStoragePropertyGroups();
		for(var i = 0; i < groups.length; i++) {
			if(!groups[i].boxSizeProperty || !groups[i].positionProperty) {
				Util.showError("Your Storage Model does not work properly with the current ELN version: Storage group '" + groups[i].groupDisplayName + "' is missing the boxSizeProperty or positionProperty.");
				return;
			}
		}
	}
	
	//Main View Setup
	var _this = this;
	this._mainController = mainController;
	this._storageManagerModel = new StorageManagerModel();
	
	var getPositionDropEventHandler = function() {
		return function(data,
						oldStoragePropertyGroup,
						newStoragePropertyGroup,
						newStorageName,
						newRow,
						newColumn,
						newBoxName,
						newBoxSize,
						newUserId,
						oldBoxPosition,
						newBoxPosition,
						newDataHolder) {
			
			var isBox = data.samples !== undefined;
			if(isBox) {
				var errorMsg = "Boxes can't be put inside other boxes.";
				Util.showError(errorMsg);
				throw errorMsg;
			}
			
			var isMultiplePosition = data.properties[newStoragePropertyGroup.positionProperty].split(" ").length > 1;
			var isNewDataHolderEmpty = $(newDataHolder).children().length === 0;
			if(isMultiplePosition) {
				//var oldBoxName === data.properties[newStoragePropertyGroup.boxProperty];
				var errorMsg = "Multiple position Samples are not supported by the manager, please use the sample form for this.";
				Util.showError(errorMsg);
				throw errorMsg;
			} else if(!isNewDataHolderEmpty) {
				var errorMsg = "That position on the box is already used by another Sample.";
				Util.showError(errorMsg);
				throw errorMsg;
			} else {
				var propertiesValues = {};
				
				//Set New Storage Group
				propertiesValues[newStoragePropertyGroup.nameProperty] = newStorageName;
				propertiesValues[newStoragePropertyGroup.rowProperty] = newRow;
				propertiesValues[newStoragePropertyGroup.columnProperty] = newColumn;
				propertiesValues[newStoragePropertyGroup.boxProperty] = newBoxName;
				propertiesValues[newStoragePropertyGroup.boxSizeProperty] = newBoxSize;
				propertiesValues[newStoragePropertyGroup.userProperty] = newUserId;
				propertiesValues[newStoragePropertyGroup.positionProperty] = newBoxPosition;
				
				//If old Storage group is different, delete it
				if(newStoragePropertyGroup.groupDisplayName !== oldStoragePropertyGroup.groupDisplayName) {
					propertiesValues[oldStoragePropertyGroup.nameProperty] = "";
					propertiesValues[oldStoragePropertyGroup.rowProperty] = "";
					propertiesValues[oldStoragePropertyGroup.columnProperty] = "";
					propertiesValues[oldStoragePropertyGroup.boxProperty] = "";
					propertiesValues[oldStoragePropertyGroup.boxSizeProperty] = "";
					propertiesValues[oldStoragePropertyGroup.userProperty] = "";
					propertiesValues[oldStoragePropertyGroup.positionProperty] = "";
				}
				
				
				
				_this._updateChangeLog({
					type: ChangeLogType.Sample,
					permId: data.permId,
					data: data,
					storagePropertyGroup : newStoragePropertyGroup,
					newProperties: propertiesValues
				});
			}
		}
	}
	
	var getBoxDropEventHandler = function() {
		return function(data,
						oldStoragePropertyGroup,
						newStoragePropertyGroup,
						newStorageName,
						newUserId,
						oldRackPosition,
						newRackPosition,
						newDataHolder) {
			var isBox = data.samples !== undefined;
			if(!isBox) {
				var errorMsg = "Samples can't be put inside a rack without a Box.";
				Util.showError(errorMsg);
				throw errorMsg;
			}
			
			for(var sIdx = 0; sIdx < data.samples.length; sIdx++) {

			}
			
		}
	}
	
	//Sub Views Setup
	this._storageFromController = new StorageController({
		title : "Storage A",
		storagePropertyGroupSelector : "on",
		storageSelector : "on",
		userSelector : "on",
		boxSelector: "on",
		boxSizeSelector: "on",
		rackSelector: "on",
		rackPositionMultiple: "off",
		rackBoxDragAndDropEnabled: "on",
		rackBoxDropEventHandler : getBoxDropEventHandler(),
		positionSelector: "on",
		positionDropEventHandler: getPositionDropEventHandler(),
		boxPositionMultiple: "off",
		positionDragAndDropEnabled: "on"
	});
	
	this._storageToController = new StorageController({
		title : "Storage B",
		storagePropertyGroupSelector : "on",
		storageSelector : "on",
		userSelector : "on",
		boxSelector: "on",
		boxSizeSelector: "on",
		rackSelector: "on",
		rackPositionMultiple: "off",
		rackBoxDragAndDropEnabled: "on",
		rackBoxDropEventHandler : getBoxDropEventHandler(),
		positionSelector: "on",
		positionDropEventHandler: getPositionDropEventHandler(),
		boxPositionMultiple: "off",
		positionDragAndDropEnabled: "on"
	});
	
	this._storageManagerView = new StorageManagerView(this, this._storageManagerModel, this._storageFromController.getView(), this._storageToController.getView());
	
	
	this._updateChangeLog = function(newChange) {
		_this._storageManagerModel.updateChangeLog(newChange);
		_this._storageManagerView.updateChangeLogView();
	}
	
	this._storageManagerView.getMoveButton().click(function() {
		Util.blockUI();
		
		var parameters = {
				"method" : "batchOperation",
				"operations" : []
		}
		
		for(var lIdx = 0; lIdx < _this._storageManagerModel.changeLog.length; lIdx++) {
			var item = _this._storageManagerModel.changeLog[lIdx];
			
			if(item.type === ChangeLogType.Sample) {
				var sample = item.data;
				var sampleSpace = sample.spaceCode;
				var sampleProject = null;
				var sampleExperiment = null;
				var sampleCode = sample.code;
				
				var experimentIdentifier = sample.experimentIdentifierOrNull;
				
				if(experimentIdentifier) { //If there is a experiment detected, the sample should be attached to the experiment completely.
					sampleSpace = experimentIdentifier.split("/")[1];
					sampleProject = experimentIdentifier.split("/")[2];
					sampleExperiment = experimentIdentifier.split("/")[3];
				}
				
				var operation = {
						//API Method
						"method" : "updateSample",
						//Identification Info
						"sampleSpace" : sampleSpace,
						"sampleProject" : sampleProject,
						"sampleExperiment" : sampleExperiment,
						"sampleCode" : sampleCode,
						"sampleType" : sample.sampleTypeCode,
						//Other Properties
						"sampleProperties" : item.newProperties,
						//Parent links
						"sampleParents": null,
						//Children links
						"sampleChildren": null,
						"sampleChildrenNew": null,
						"sampleChildrenRemoved": null,
						//Other Samples
						"changesToDo" : null
				}
				
				parameters["operations"].push(operation);
			}
		}
		
		if(profile.getDefaultDataStoreCode()) {
			mainController.serverFacade.createReportFromAggregationService(profile.getDefaultDataStoreCode(), parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
						Util.showSuccess("Entities successfully updated.", function() {
							mainController.changeView("showStorageManager");
							Util.unblockUI();
						});
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
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