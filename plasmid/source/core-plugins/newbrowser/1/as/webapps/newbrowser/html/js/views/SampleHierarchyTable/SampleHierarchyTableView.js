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
	
	this.repaint = function($container) {
		$container.empty();
		var $containerColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		$containerColumn.append($("<h1>").append(this._model.title));
		this._showHierarchy();
		$containerColumn.append(this._container);
		$container.append($containerColumn);
	}
	
	this._showHierarchy = function() {
		var _this = this;
		
		var columns = [ {
			label : 'Level',
			property : 'level',
			sortable : true
		} , {
			label : 'Identifier',
			property : 'identifier',
			sortable : true
		} , {
			label : 'Path',
			property : 'path',
			sortable : true
		} , {
			label : 'Name',
			property : 'name',
			sortable : true
		} , {
			label : 'Parent/Annotations',
			property : 'parentAnnotations',
			sortable : true,
			render : function(data) {
				return _this._annotationsRenderer(data.sample.parents, data.sample);
			}
		} , {
			label : 'Children/Annotations',
			property : 'childrenAnnotations',
			sortable : true,
			render : function(data) {
				return _this._annotationsRenderer(data.sample.children, data.sample);
			}
		}];
		
		var getDataList = function(callback) {
			callback(_this._model.getData());
		}
		
		var rowClick = function(e) {
			mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
		}
		
		var dataGrid = new DataGridController(null, columns, getDataList, rowClick);
		dataGrid.init(this._container);
		this._container.prepend($("<legend>").append(" Sample Hierarchy"));
	}
	
	this._annotationsRenderer = function(samples, sample) {
		var annotations = FormUtil.getAnnotationsFromSample(sample);
		var content = "";
		var rowStarted = false;
		AnnotationUtil.buildAnnotations(annotations, samples, {
			startRow : function() {
				if (content !== "") {
					content += "</br>";
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