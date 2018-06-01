/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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

function HierarchyTableView(controller, model) {
	this._model = model;
	this._controller = controller;
	this._container = $("<div>");
	this._dataGrid;
	this._hierarchyFilterController;
	
	this.repaint = function(views) {
		var _this = this;
		
		var $containerColumn = $("<form>", {
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		$containerColumn.append(this._container);
		views.content.append($containerColumn);
		
		switch(this._model.entity["@type"]) {
				case "as.dto.dataset.DataSet":
					views.header.append($("<h1>").append("Dataset Hierarchy Table for " + this._model.entity.code));
					break;
				case "as.dto.sample.Sample":
					views.header.append($("<h1>").append("" + ELNDictionary.Sample + " Hierarchy Table for " + this._model.entity.identifier));
					break;
		}
		
		this._hierarchyFilterController = new HierarchyFilterController(this._model.entity, function() { _this._dataGrid.refresh(); });
		this._hierarchyFilterController.init(views.header);
		this._showHierarchy();
		
	}
	
	this._showHierarchy = function() {
		var _this = this;
		
		var columns = [];
		columns.push({
			label : 'Level',
			property : 'level',
			sortable : true
		});
		if(this._model.entity["@type"] === "as.dto.dataset.DataSet") {
			columns.push({
				label : "Registration Date",
				property : 'registrationDate',
				sortable : true
			});
			columns.push({
				label : "History Id",
				property : 'historyId',
				sortable : true
			});
			columns.push({
				label : "Repository Id",
				property : 'repositoryId',
				sortable : true
			});
		}
		columns.push({
			label : "Type",
			property : 'type',
			sortable : true
		});
		if(this._model.entity["@type"] === "as.dto.sample.Sample") {
			columns.push({
				label : 'Identifier',
				property : 'identifier',
				sortable : true
			});
		}
		if(this._model.entity["@type"] === "as.dto.dataset.DataSet") {
			columns.push({
				label : 'Code',
				property : 'code',
				sortable : true
			});
		}
		columns.push({
			label : 'Name',
			property : 'name',
			sortable : true
		});
		columns.push({
			label : 'Path',
			property : 'path',
			sortable : true
		});
		if(this._model.entity["@type"] === "as.dto.sample.Sample") {
			columns.push({
				label : 'Parent/Annotations',
				property : 'parentAnnotations',
				sortable : true,
				render : function(data) {
					return _this._annotationsRenderer(_this._model.relationShipsMap[data.permId].parents, data.entity);
				}
			});
			columns.push({
				label : 'Children/Annotations',
				property : 'childrenAnnotations',
				sortable : true,
				render : function(data) {
					return _this._annotationsRenderer(_this._model.relationShipsMap[data.permId].children, data.entity);
				}
			});
		}
		var getDataList = function(callback) {
			var data = _this._model.getData();
			var parentsLimit = _this._hierarchyFilterController.getParentsLimit();
			var childrenLimit = _this._hierarchyFilterController.getChildrenLimit();
			var types = _this._hierarchyFilterController.getSelectedEntityTypes();
			var filteredData = [];
			for (var i = 0; i < data.length; i++) {
				var row = data[i];
				if (row.level == 0 || ($.inArray(row.type, types) >= 0 
						&& row.level <= childrenLimit && row.level >= -parentsLimit)) {
					filteredData.push(row);
				}
			}
			callback(filteredData);
		}
		
		var rowClick = function(e) {
			switch(e.data.entity["@type"]) {
				case "as.dto.dataset.DataSet":
					mainController.changeView('showViewDataSetPageFromPermId', e.data.permId);
					break;
				case "as.dto.sample.Sample":
					mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
					break;
			}
		}
		
		this._dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, false, this._model.entity["@type"] + "_HIERARCHY_TABLE");
		this._dataGrid.init(this._container);
		this._container.prepend($("<legend>").append(" " + ELNDictionary.Sample + " Hierarchy"));
	}
	
    /*
	 * Only samples have annotations, if they are not samples, they will simply not be found
	 */
	this._annotationsRenderer = function(entities, entity) {
		var annotations = FormUtil.getAnnotationsFromSample(entity);
		var content = "";
		var rowStarted = false;
		AnnotationUtil.buildAnnotations(annotations, entities, {
			startRow : function() {
				if (content !== "") {
					content += "<br /><br />";
				}
				rowStarted = true;
			},
			addKeyValue : function(key, value) {
				if (rowStarted === false) {
					content += ", ";
				}
				var label = key;
				if (key === "CODE") {
					label = "Code";
				} else {
					var propertyType = profile.getPropertyType(key);
					if (propertyType) {
						label = propertyType.label;
					}
				}
				content += "<b>" + label + "</b>: " + value;
				rowStarted = false;
			}
		})
		return content;
	}
}