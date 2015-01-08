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
	
	this.repaint = function($container) {
		var _this = this;

		//
		// Data Grid
		//
		var dataGridContainer = $("<div>");
		
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
					dataList.push({
						groupDisplayName : storagePropertyGroup.groupDisplayName,
						nameProperty : sample.properties[storagePropertyGroup.nameProperty],
						rowProperty : sample.properties[storagePropertyGroup.rowProperty],
						columnProperty : sample.properties[storagePropertyGroup.columnProperty],
						boxProperty : sample.properties[storagePropertyGroup.boxProperty],
						positionProperty : sample.properties[storagePropertyGroup.positionProperty],
						userProperty : sample.properties[storagePropertyGroup.userProperty]
					});
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
		
		var dataGrid = new DataGridController(null, columns, getDataList, rowClick);
		dataGrid.init($container);
	}
	
	this.showStorageWidget = function(e) {
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '70%',
				'left' : '15%',
				'right' : '20%',
				'overflow' : 'auto'
		};
		
		var container = "<div class='col-md-12 form-horizontal' id='storage-pop-up-container'></div><br><a class='btn btn-default' id='storage-close'>Close</a>"
		Util.blockUI(container, css);
		$("#storage-close").on("click", function(event) { 
			Util.unblockUI();
		});
		
		var storageController = new StorageController({
			title : e.data.groupDisplayName,
			storagePropertyGroupSelector : "off",
			storageSelector : "on",
			userSelector : "off",
			boxSelector: "on",
			rackSelector: "on",
			contentsSelector: "off",
			positionSelector: "on"
		});
		storageController.getModel().storagePropertyGroup = profile.getStoragePropertyGroup(e.data.groupDisplayName);
		storageController.bindSample(this._storageListModel.sample, this._storageListModel.isDisabled);
		storageController.getView().repaint($("#storage-pop-up-container"));
	}
}