/* Copyright 2014 ETH Zuerich, Scientific IT Services
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

function ExperimentTableView(experimentTableController, experimentTableModel) {
	this._experimentTableController = experimentTableController;
	this._experimentTableModel = experimentTableModel;
	this._tableContainer = $("<div>");
	this.typeSelector = null;
	this._$container = null;
	
	this.repaint = function($container) {
		var _this = this;
		this._$container = $container;
		$container.empty();
		this._tableContainer.empty();
		if(this._experimentTableModel.title) {
			var $title = $("<h1>").append(this._experimentTableModel.title);
			$container.append($title);
		}
		
		var toolbarModel = [];
		toolbarModel.push({ component : this._getProjectExperimentTypesDropdown(), tooltip: "Select an experiment type to visualize on the table" });
		toolbarModel.push({ component : this._showExperimentFromOverviewDropdown(), tooltip: "Select if showing all experiments or only overview ones" });

		
		$container.append(FormUtil.getToolbar(toolbarModel));
		$container.append(this._tableContainer);
	}
	
	this.getTableContainer = function() {
		return this._tableContainer;
	}
	
	//
	// Components
	//
	this.getTypeSelector = function() {
		return this.typeSelector;
	}
	
	this._showExperimentFromOverviewDropdown = function() {
		var _this = this;
		var expDropModel = [{value : "OVERVIEW", label : "Show only overview experiments", selected : this._experimentTableModel.showInProjectOverview },
		                          {value : "ALL", label : "Show all experiments", selected : !this._experimentTableModel.showInProjectOverview }];
		
		var $experimentDropdown = FormUtil.getDropdown(expDropModel, "Select what experiments to show");
		
		$experimentDropdown.change(function() {
			switch($(this).val()){
				case "OVERVIEW":
						_this._experimentTableModel.showInProjectOverview = true;
					break;
				case "ALL":
						_this._experimentTableModel.showInProjectOverview = false;
					break;
			}
			_this._experimentTableController.init(_this._$container);
		});
		return $("<span>").append($experimentDropdown);
	}
	
	this._getProjectExperimentTypesDropdown = function() {
		var _this = this;
		var	$typesSelector = $('<select>', { class : 'form-control' });
		$typesSelector.append($("<option>").attr('value', '').attr('selected', '').attr('disabled', '').text("Select an experiment type"));
		for(typeCode in this._experimentTableModel.types) {
			$typesSelector.append($('<option>', { 'value' : typeCode }).text(typeCode));
		}
		
		$typesSelector.change(function(event) {
			var typeToShow = $(this).val();
			_this._experimentTableController._reloadTableWithType(typeToShow);
		});
		this.typeSelector = $typesSelector;
		return $("<span>").append($typesSelector);
	}
	
}