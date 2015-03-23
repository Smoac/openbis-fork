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

function DataGridController(title, columns, data, rowClickEventHandler, showAllColumns) {
	this._grid = null;
	this._dataGridModel = null;
	this._dataGridView = null;
	var _this = this;
	
	this.init = function($container) {
		var webAppId = "ELN-LIMS";
		mainController.serverFacade.openbisServer.getWebAppSettings(webAppId, function(response) {
			var settings = response.result.settings;
			var onColumnsChange = function(columnsModel) {
				console.log("SAVING START");
				if(!settings) {
					settings = {};
				}
				settings[_this._dataGridModel.title] = JSON.stringify(columnsModel);
				
				var webAppSettings = {
						"@type" : "WebAppSettings",
						"webAppId" : webAppId,
						"settings" : settings
				}
				
				console.log("SAVING: " + JSON.stringify(settings));
				mainController.serverFacade.openbisServer.setWebAppSettings(webAppSettings, function(result) {});
			}
			console.log("LOAD: " + JSON.stringify(settings));
			var tableSettings = null;
			if(settings[title]) {
				tableSettings = JSON.parse(settings[title]);
			}
			_this._grid = new Grid(columns, data, showAllColumns, tableSettings, onColumnsChange);
			_this._grid.addRowClickListener(rowClickEventHandler);
			_this._dataGridModel = new DataGridModel(title, columns, data, rowClickEventHandler, _this._grid.render());
			_this._dataGridView = new DataGridView(this, _this._dataGridModel);
			
			_this._dataGridView.repaint($container);
		});	
	}
	
	this.refresh = function() {
		this._dataGridModel.datagrid.repeater('render');
	}
}