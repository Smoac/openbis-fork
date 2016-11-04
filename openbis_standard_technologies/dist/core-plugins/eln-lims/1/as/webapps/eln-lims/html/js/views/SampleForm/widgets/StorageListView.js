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
function StorageListView(storageListController, storageListModel) {
	this._storageListController = storageListController;
	this._storageListModel = storageListModel;
	this._dataGrid = null; //TO-DO This is a controller, should not be here
	
	this.repaint = function($container) {
		var _this = this;

		//
		// Data Grid
		//
		var columns = [ {
			label : 'Group',
			property : 'groupDisplayName',
			sortable : true
		} , {
			label : 'Name',
			property : 'nameProperty',
			sortable : true
		} , {
			label : 'Row',
			property : 'rowProperty',
			sortable : true
		}, {
			label : 'Column',
			property : 'columnProperty',
			sortable : true
		}, {
			label : 'Box',
			property : 'boxProperty',
			sortable : true
		}, {
			label : 'Box Size',
			property : 'boxSizeProperty',
			sortable : true
		}, {
			label : 'Position',
			property : 'positionProperty',
			sortable : true
		}, {
			label : 'User',
			property : 'userProperty',
			sortable : true
		}];
		
		var getDataList = function(callback) {
			var dataList = [];
			var sample = _this._storageListModel.sample;
			var sampleTypeCode = sample.sampleTypeCode;
			var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
			
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				var storagePropertyGroup = profile.getStoragePropertyGroup(propertyTypeGroup.name);
				
				if(storagePropertyGroup) {
					var userProperty = sample.properties[storagePropertyGroup.userProperty];
					var nameProperty = sample.properties[storagePropertyGroup.nameProperty];
					
					if(	(userProperty && userProperty !== "") ||
						(nameProperty && nameProperty !== "")) {
						dataList.push({
							groupDisplayName : storagePropertyGroup.groupDisplayName,
							nameProperty : nameProperty,
							rowProperty : sample.properties[storagePropertyGroup.rowProperty],
							columnProperty : sample.properties[storagePropertyGroup.columnProperty],
							boxProperty : sample.properties[storagePropertyGroup.boxProperty],
							boxSizeProperty : sample.properties[storagePropertyGroup.boxSizeProperty],
							positionProperty : sample.properties[storagePropertyGroup.positionProperty],
							userProperty : userProperty
						});
					}
				}
			}
			callback(dataList);
		}
		
		var rowClick = null;
		if(!this._storageListModel.isDisabled) {
			rowClick = function(e) {
				_this.showStorageWidget(e)
			}
		}
		
		this._dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, false, "STORAGE_WIDGET");
		
		var $dataGridContainer = $("<div>");
		this._dataGrid.init($dataGridContainer);
		$container.append($dataGridContainer);
		
		var $storageAddButton = $("<a>", { class : 'btn btn-default', style : "float: right; background-color:#f9f9f9;" }).append($("<i>", { class : "glyphicon glyphicon-plus" } ));
		
		
		$storageAddButton.on("click", function(event) {
			var sample = _this._storageListModel.sample;
			var sampleTypeCode = sample.sampleTypeCode;
			var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
			
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				var storagePropertyGroup = profile.getStoragePropertyGroup(propertyTypeGroup.name);
				
				if(storagePropertyGroup) {
					var userProperty = sample.properties[storagePropertyGroup.userProperty];
					if(!userProperty || userProperty === "") { //Not Used
						sample.properties[storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0]; //Mark to show
					}
				}
			}
			
			if(_this.getUsedStorages() === _this.getMaxStorages()) {
				$storageAddButton.attr("disabled", "");
			}
			
			_this._dataGrid.refresh();
		});
		
		if(this._storageListModel.isDisabled || this.getUsedStorages() === this.getMaxStorages()) {
			$storageAddButton.attr("disabled", "");
		}
		
		$container.append($storageAddButton);
		$container.append("NOTE: Storages limited to " + this.getMaxStorages() + " for this type.");
	}
	
	this.getUsedStorages = function() {
		var count = 0;
		var sample = this._storageListModel.sample;
		var sampleTypeCode = sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			var storagePropertyGroup = profile.getStoragePropertyGroup(propertyTypeGroup.name);
			
			if(storagePropertyGroup) {
				var userProperty = sample.properties[storagePropertyGroup.userProperty];
				if(userProperty) {
					count++;
				}
			}
		}
		return count;
	}
	
	this.getMaxStorages = function() {
		var count = 0;
		var sample = this._storageListModel.sample;
		var sampleTypeCode = sample.sampleTypeCode;
		var sampleType = mainController.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			var storagePropertyGroup = profile.getStoragePropertyGroup(propertyTypeGroup.name);
			
			if(storagePropertyGroup) {
				count++;
			}
		}
		return count;
	}
	
	this.showStorageWidget = function(e) {
		var _this = this;
		var css = {
				'text-align' : 'left',
				'top' : '5%',
				'width' : '80%',
				'left' : '10%',
				'right' : '10%',
				'overflow' : 'auto',
				'height' : '90%'
		};
		
		var container = "<div class='row col-md-12 form-horizontal' id='storage-pop-up-container'></div><br><a class='btn btn-default' id='storage-accept'>Accept</a> <a class='btn btn-default' id='storage-cancel'>Cancel</a>"
		Util.blockUI(container, css);
		
		
		var storageController = new StorageController({
			title : e.data.groupDisplayName,
			storagePropertyGroupSelector : "off",
			storageSelector : "on",
			userSelector : "off",
			boxSelector: "on",
			boxSizeSelector: "on",
			rackSelector: "on",
			rackPositionMultiple: "off",
			rackBoxDragAndDropEnabled: "off",
			rackBoxDropEventHandler : null,
			positionSelector: "on",
			positionDropEventHandler: null,
			boxPositionMultiple: "on",
			positionDragAndDropEnabled: "off"
		});
		var storagePropGroup = profile.getStoragePropertyGroup(e.data.groupDisplayName);
		storageController.getModel().storagePropertyGroup = storagePropGroup;
		this._storageListController._saveState(storagePropGroup);
		storageController.bindSample(this._storageListModel.sample, this._storageListModel.isDisabled);
		storageController.getView().repaint($("#storage-pop-up-container"));
		
		$("#storage-accept").on("click", function(event) {
			storageController.isValid(function(isValid) {
				if(isValid) {
					Util.unblockUI();
					_this._dataGrid.refresh();
				}
			});
		});
		
		$("#storage-cancel").on("click", function(event) {
			_this._storageListController._restoreState();
			Util.unblockUI();
			_this._dataGrid.refresh();
		});
	}
}