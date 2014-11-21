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

function VocabularyManagerView(vocabularyManagerController, vocabularyManagerModel) {
	this._vocabularyManagerController = vocabularyManagerController;
	this._vocabularyManagerModel = vocabularyManagerModel;
	this._dataGridContainer = $("<div>");
		
	this.repaint = function($container) {
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
		
		$containerColumn.append($("<h1>").append(" Vocabulary Manager"));
		this._showVocabularies();
		$containerColumn.append(this._dataGridContainer);
		$container.append($containerColumn);
	}
	
	this._showVocabularies = function() {
		var _this = this;
		
		var columns = [ {
			label : 'Code',
			property : 'code',
			sortable : true
		} , {
			label : 'Description',
			property : 'description',
			sortable : true
		}];
		
		var getDataList = function(callback) {
			var dataList = [];
			for(var idx = 0; idx < _this._vocabularyManagerModel.vocabularies.length; idx++) {
				var vocabulary =  _this._vocabularyManagerModel.vocabularies[idx];
				dataList.push({
					code : vocabulary.code,
					description : vocabulary.description
				});
			}
			callback(dataList);
		}
		
		var rowClick = function(e) {
			var a = 0;
		}
		
		var dataGrid = new DataGridController(null, columns, getDataList, rowClick);
		dataGrid.init(this._dataGridContainer);
	}
}