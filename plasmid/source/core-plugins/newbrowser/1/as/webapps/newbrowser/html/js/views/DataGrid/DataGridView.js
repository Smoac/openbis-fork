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
function DataGridView(dataGridController, dataGridModel) {
	this._dataGridController = dataGridController;
	this._dataGridModel = dataGridModel;
	
	this.repaint = function($container) {
		$container.empty();
		
		var columns = [ {
			label : 'Name',
			property : 'name',
			sortable : true
		}, {
			label : 'Age',
			property : 'age',
			sortable : true
		}, {
			label : 'Surname',
			property : 'surname',
			sortable : true
		}, {
			label : "Full Name",
			property : 'fullname',
			sortable : true,
			render : function(data) {
				var fullName = data.name + " " + data.surname;
				return $("<button>").text(fullName).click(function() {
					alert("My name is: " + fullName);
				});
			},
			filter : function(data, filter) {
				var fullName = data.name + " " + data.surname;
				return fullName.toLowerCase().indexOf(filter) != -1;
			},
			sort : function(data1, data2, asc) {
				return naturalSort(data1.surname, data2.surname);
			}
		} ];
		var getDataList = function(callback) {
			callback([ {
				name : "Name A",
				surname : "Surname 1",
				age : 21
			}, {
				name : "Name B",
				surname : "Surname 2",
				age : 34
			}, {
				name : "Name C",
				surname : "Surname 10",
				age : 61
			} ]);
		};
		var grid = new Grid(columns, getDataList);

		grid.addRowClickListener(function(e) {
			console.log("Row clicked - index: " + e.index);
		});
		
		$container.append($("<h1>").append(this._dataGridModel.title));
		$container.append(grid.render());
	}
}