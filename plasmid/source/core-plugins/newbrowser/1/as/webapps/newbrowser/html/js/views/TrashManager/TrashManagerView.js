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

function TrashManagerView(trashManagerController, trashManagerModel) {
	this._trashManagerController = trashManagerController;
	this._trashManagerModel = trashManagerModel;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		//
		// Form template and title
		//
		var $containerColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $trashIcon = $("<span>", { 'class' : 'glyphicon glyphicon-trash'});
		$containerColumn.append($("<h1>").append($trashIcon).append(" Trashcan"));
		
		//
		// Table
		//
		var columns = [ {
			label : 'Entities',
			property : 'entities',
			sortable : true
		} , {
			label : 'Reason',
			property : 'reason',
			sortable : true
		} , {
			label : 'Total Experiments',
			property : 'totalExperiments',
			sortable : true
		} , {
			label : 'Total Samples',
			property : 'totalSamples',
			sortable : true
		} , {
			label : 'Total Datasets',
			property : 'totalDatasets',
			sortable : true
		}];
		
		var getDataList = function(callback) {
			var dataList = [];
			for(var delIdx = 0; delIdx < _this._trashManagerModel.deletions.length; delIdx++) {
				var deletion = _this._trashManagerModel.deletions[delIdx];
				var entities = null;
				for(var enIdx = 0; enIdx < deletion.deletedEntities.length; enIdx++) {
					if(entities) {
						entities += "<br>";
					} else {
						entities = "";
					}
					entities += deletion.deletedEntities[enIdx].entityKind
							+ " - " + deletion.deletedEntities[enIdx].identifier
							+ " (" + deletion.deletedEntities[enIdx].entityType + ")";
				}
				dataList.push({
					entities : entities,
					reason : deletion.reasonOrNull,
					totalExperiments : deletion.totalExperimentsCount,
					totalSamples : deletion.totalSamplesCount,
					totalDatasets : deletion.totalDatasetsCount
				});
			}
			callback(dataList);
		}
		
		var dataGridContainer = $("<div>");
		var dataGrid = new DataGridController(null, columns, getDataList, null);
		dataGrid.init(dataGridContainer);
		$containerColumn.append(dataGridContainer);
		
		//
		// Empty all button
		//
		var deleteAllBtn = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;"}).append("Empty Trash");
		deleteAllBtn.click(function() {
			_this._trashManagerController.emptyTrash();
		});
		$containerColumn.append(deleteAllBtn);
		//
		$container.append($containerColumn);
	}
}