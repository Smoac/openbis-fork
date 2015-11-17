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
						newStoragePropertyGroup,
						newStorageName,
						newRow,
						newColumn,
						newBoxName,
						newBoxSize,
						newUserId,
						newBoxPosition) {
			
			var isMultiplePosition = data.properties[newStoragePropertyGroup.positionProperty].split(" ").length > 1;
			if(isMultiplePosition) {
				var errorMsg = "Multiple position support is not implemented on the manager, please use the sample form for this.";
				Util.showError(errorMsg);
				throw errorMsg;
			} else {
				var propertiesValues = {};
				propertiesValues[newStoragePropertyGroup.nameProperty] = newStorageName;
				propertiesValues[newStoragePropertyGroup.rowProperty] = newRow;
				propertiesValues[newStoragePropertyGroup.columnProperty] = newColumn;
				propertiesValues[newStoragePropertyGroup.boxProperty] = newBoxName;
				propertiesValues[newStoragePropertyGroup.boxSizeProperty] = newBoxSize;
				propertiesValues[newStoragePropertyGroup.userProperty] = newUserId;
				propertiesValues[newStoragePropertyGroup.positionProperty] = newBoxPosition;
				
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
		rackBoxDragAndDropEnabled: "off",
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
		rackBoxDragAndDropEnabled: "off",
		positionSelector: "on",
		positionDropEventHandler: getPositionDropEventHandler(this._storageManagerModel.changeLog),
		boxPositionMultiple: "off",
		positionDragAndDropEnabled: "on"
	});
	
	this._storageManagerView = new StorageManagerView(this._storageManagerModel, this._storageFromController.getView(), this._storageToController.getView());
	
	
	this._updateChangeLog = function(newChange) {
		_this._storageManagerModel.updateChangeLog(newChange);
		_this._storageManagerView.updateChangeLogView();
	}
	
	this._storageManagerView.getMoveButton().click(function() {
		alert("TO-DO Update Logic!");
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