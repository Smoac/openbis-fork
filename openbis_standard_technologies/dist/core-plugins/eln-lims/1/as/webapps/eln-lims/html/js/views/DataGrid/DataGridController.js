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

function DataGridController(title, columns, data, rowClickEventHandler, showAllColumns,configKey, isMultiselectable) {
	this._grid = null;
	this._dataGridModel = null;
	this._dataGridView = null;
	var _this = this;
	
	if(!configKey) {
		window.alert("[TO-DELETE] Empty configKey during the table init, this should never happen, tell the developers.");
	}
	
	this.init = function($container, extraOptions) {
		var webAppId = "ELN-LIMS";
		mainController.serverFacade.openbisServer.getWebAppSettings(webAppId, function(response) {
			var settings = response.result.settings;
			var onColumnsChange = function(tableState) {
				if(!settings) {
					settings = {};
				}
				settings[configKey] = JSON.stringify(tableState);
				
				var webAppSettings = {
						"@type" : "WebAppSettings",
						"webAppId" : webAppId,
						"settings" : settings
				}
				
				mainController.serverFacade.openbisServer.setWebAppSettings(webAppSettings, function(result) {});
			}
			var tableSettings = null;
			if(configKey && settings[configKey]) {
				tableSettings = JSON.parse(settings[configKey]);
				if(Object.keys(tableSettings).length > 3) { //Clean old settings
					tableSettings = null;
				}
			}
			_this._grid = new Grid(columns, data, showAllColumns, tableSettings, onColumnsChange, isMultiselectable);
			if(rowClickEventHandler) {
				_this._grid.addRowClickListener(rowClickEventHandler);
			}
			if(extraOptions) {
				_this._grid.addExtraOptions(extraOptions);
			}
			
			_this._dataGridModel = new DataGridModel(title, columns, data, rowClickEventHandler, _this._grid.render(), isMultiselectable);
			_this._dataGridView = new DataGridView(this, _this._dataGridModel);
			_this._dataGridView.repaint($container);
		});	
	}
	
	this.refresh = function() {
		this._dataGridModel.datagrid.repeater('render');
	}
}