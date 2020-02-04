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

function DataGridController(title, columnsFirst, columnsLast, columnsDynamicFunc, data, rowClickEventHandler, showAllColumns,configKey, isMultiselectable, heightPercentage) {
	this._grid = null;
	this._dataGridModel = null;
	this._dataGridView = null;
	this.lastSavedSettings = null;
	var _this = this;
	
	if(!configKey) {
		window.alert("[TO-DELETE] Empty configKey during the table init, this should never happen, tell the developers.");
	}
	
	this.init = function($container, extraOptions) {
		var webAppId = "ELN-LIMS";
		mainController.serverFacade.getSetting(configKey, function(tableConfig) {
			var onColumnsChange = function(tableState) {
				var newSettingsToStore = JSON.stringify(tableState);
				if(_this.lastSavedSettings !== newSettingsToStore) {
					_this.lastSavedSettings = newSettingsToStore;
					mainController.serverFacade.setSetting(configKey, _this.lastSavedSettings);
				}
			}
			
			if(tableConfig) {
				tableConfig = JSON.parse(tableConfig);
				if(!tableConfig.pageSize) { //Clean old settings
					tableConfig = null;
				}
			}
			_this._grid = new Grid(columnsFirst, columnsLast, columnsDynamicFunc, data, showAllColumns, tableConfig, 
					onColumnsChange, isMultiselectable, null, heightPercentage, mainController.getScrollbarWidth());
			if(rowClickEventHandler) {
				_this._grid.addRowClickListener(rowClickEventHandler);
			}
			if(extraOptions) {
				_this._grid.addExtraOptions(extraOptions);
			}
			
			_this._dataGridModel = new DataGridModel(title, columnsFirst, columnsLast, columnsDynamicFunc, data, rowClickEventHandler, _this._grid.render(), isMultiselectable, heightPercentage);
			_this._dataGridView = new DataGridView(this, _this._dataGridModel);
			_this._dataGridView.repaint($container);
		});	
	}
	
	this.refresh = function() {
		this._dataGridModel.datagrid.repeater('render');
	}
}