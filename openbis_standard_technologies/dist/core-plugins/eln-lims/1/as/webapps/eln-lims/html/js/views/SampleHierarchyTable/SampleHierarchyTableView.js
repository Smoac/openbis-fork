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

function SampleHierarchyTableView(controller, model) {
	this._model = model;
	this._controller = controller;
	this._container = $("<div>");
	this._dataGrid;
	this._hierarchyFilterController;
	
	this.repaint = function(views) {
		var _this = this;
		
		var $containerColumn = $("<form>", { 
			"class" : "form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		$containerColumn.append(this._container);
		views.content.append($containerColumn);
		
		views.header.append($("<h1>").append("" + ELNDictionary.Sample + " Hierarchy Table for " + this._model.sample.identifier));
		this._hierarchyFilterController = new HierarchyFilterController(this._model.sample, function() { _this._dataGrid.refresh(); });
		this._hierarchyFilterController.init(views.header);
		this._showHierarchy();
		
	}
	
	this._showHierarchy = function() {
		var _this = this;
		
		var columns = [ {
			label : 'Level',
			property : 'level',
			sortable : true
		} , {
			label : "" + ELNDictionary.Sample + " Type",
			property : 'sampleType',
			sortable : true,
			render : function(data) {
				return data.sample.sampleTypeCode;
			}
		} , {
			label : 'Identifier',
			property : 'identifier',
			sortable : true
		} , {
			label : 'Name',
			property : 'name',
			sortable : true
		} , {
			label : 'Path',
			property : 'path',
			sortable : true
		} , {
			label : 'Parent/Annotations',
			property : 'parentAnnotations',
			sortable : true,
			render : function(data) {
				return _this._annotationsRenderer(_this._model.relationShipsMap[data.identifier].parents, data.sample);
			}
		} , {
			label : 'Children/Annotations',
			property : 'childrenAnnotations',
			sortable : true,
			render : function(data) {
				return _this._annotationsRenderer(_this._model.relationShipsMap[data.identifier].children, data.sample);
			}
		}];
		
		var getDataList = function(callback) {
			var data = _this._model.getData();
			var parentsLimit = _this._hierarchyFilterController.getParentsLimit();
			var childrenLimit = _this._hierarchyFilterController.getChildrenLimit();
			var sampleTypes = _this._hierarchyFilterController.getSelectedSampleTypes();
			var filteredData = [];
			for (var i = 0; i < data.length; i++) {
				var row = data[i];
				if (row.level == 0 || ($.inArray(row.sampleType, sampleTypes) >= 0 
						&& row.level <= childrenLimit && row.level >= -parentsLimit)) {
					filteredData.push(row);
				}
			}
			callback(filteredData);
		}
		
		var rowClick = function(e) {
			mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
		}
		
		this._dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, false, "SAMPLE_HIERARCHY_TABLE");
		this._dataGrid.init(this._container);
		this._container.prepend($("<legend>").append(" " + ELNDictionary.Sample + " Hierarchy"));
	}
	
	this._annotationsRenderer = function(samples, sample) {
		var annotations = FormUtil.getAnnotationsFromSample(sample);
		var content = "";
		var rowStarted = false;
		AnnotationUtil.buildAnnotations(annotations, samples, {
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
				content += "<b>" + label + "</b>:" + value;
				rowStarted = false;
			}
		})
		return content;
	}
}