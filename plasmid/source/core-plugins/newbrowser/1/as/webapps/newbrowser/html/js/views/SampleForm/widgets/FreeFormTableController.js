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

function FreeFormTableController(sample, isEnabled) {
	this._freeFormTableModel = new FreeFormTableModel(sample, isEnabled);
	this._freeFormTableView = new FreeFormTableView(this, this._freeFormTableModel);
	
	this.init = function($container) {
		this.load();
		this._freeFormTableView.repaint($container);
	}
	
	//
	// API
	//
	this.save = function() {
		sample.properties[this._freeFormTableModel.samplePropertyCode] = JSON.stringify(this._freeFormTableModel.tables);
	}
	
	this.load = function() {
		$("#" + this._freeFormTableModel.samplePropertyCode).parent().parent().hide(); //Hide State Field
		var state = sample.properties[this._freeFormTableModel.samplePropertyCode];
		if(state && state !== "") {
			this._freeFormTableModel.tables = JSON.parse(state);
		}
	}

	this.addTable = function(tableBefore, $tableBefore) {
		if(tableBefore && $tableBefore)  {
			var newTableModel = this._freeFormTableModel.addTableAfter(tableBefore); //Add new table after
			var $newTableContainer = this._freeFormTableView._getTableWithContainer(newTableModel); //Creates Table from model
			this._freeFormTableView.addTable($newTableContainer, $tableBefore);
		} else {
			var newTableModel = this._freeFormTableModel.addTableAtEnd(); //Add new table after
			var $newTableContainer = this._freeFormTableView._getTableWithContainer(newTableModel); //Creates Table from model
			this._freeFormTableView.addTable($newTableContainer, null);
		}
		
		this.save();
	}
	
	this.deleteTable = function(tableData, $tableContainer) {
		this._freeFormTableModel.removeTable(tableData); //Removes from model
		this._freeFormTableView.deleteTable($tableContainer); //Removes from view
		
		this.save();
	}
	
	this._updateChangesOnDOMandView = function(tableModel, $wrappedTable) {
		//Update DOM
		this.save();
		
		//Update View
		var isMini = $($wrappedTable.parent().children()[0]).children()[0].childNodes[0].checked;
		$wrappedTable.empty();
		if(isMini) {
			tableView = this._freeFormTableView._getMiniTable(tableModel);
		} else {
			tableView = this._freeFormTableView._getDetailedTable(tableModel);
		}
		$wrappedTable.append(tableView);
	}
	
	this.addRow = function(tableModel, $wrappedTable, rowIdx) {
		//Detailed Model
		var numColumns = tableModel.modelDetailed[0].length;
		tableModel.modelDetailed.splice(rowIdx, 0, new Array(numColumns));
		//Mini Model
		tableModel.modelMini.rows.splice(rowIdx, 0, '');
		//Trigger Update
		this._updateChangesOnDOMandView(tableModel, $wrappedTable);
	}
	
	this.delRow = function(tableModel, $wrappedTable, rowIdx) {
		//Detailed Model
		tableModel.modelDetailed.splice(rowIdx, 1);
		//Mini Model
		tableModel.modelMini.rows.splice(rowIdx, 1);
		//Trigger Update
		this._updateChangesOnDOMandView(tableModel, $wrappedTable);
	}
	
	this.addColumn = function(tableModel, $wrappedTable, colIdx) {
		//Detailed Model
		for(var i = 0; i < tableModel.modelDetailed.length; i++) {
			tableModel.modelDetailed[i].splice(colIdx, 0, '');
		}
		//Mini Model
		tableModel.modelMini.columns.splice(colIdx, 0, '');
		//Trigger Update
		this._updateChangesOnDOMandView(tableModel, $wrappedTable);
	}
	
	this.delColumn = function(tableModel, $wrappedTable, colIdx) {
		//Detailed Model
		for(var i = 0; i < tableModel.modelDetailed.length; i++) {
			tableModel.modelDetailed[i].splice(colIdx, 1);
		}
		//Mini Model
		tableModel.modelMini.columns.splice(colIdx, 1);
		//Trigger Update
		this._updateChangesOnDOMandView(tableModel, $wrappedTable);
	}
	
	this.importCSV = function(tableModel, $wrappedTable) {
		
	}
	
	this.exportCSV = function(tableModel, $wrappedTable) {
		var csv = "";
		
		//Name Conversion
		csv += "#Name;\n";
		csv += tableModel.name + ";\n";
		
		//Mini Conversion
		csv += "#Mini Rows;\n";
		for(var i = 0; i < tableModel.modelMini.rows.length; i++) {
			csv += "\"" + tableModel.modelMini.rows[i] + "\"" + ";\n";
		}
		csv += "#Mini Columns;\n";
		for(var i = 0; i < tableModel.modelMini.columns.length; i++) {
			csv += "\"" + tableModel.modelMini.columns[i] + "\"" + ";\n";
		}
		
		//Detailed Conversion
		csv += "#Detailed;\n";
		for(var i = 0; i < tableModel.modelDetailed.length; i++) {
			if(i != 0) {
				csv += "\n";
			}
			for(var j = 0; j < tableModel.modelDetailed[i].length; j++) {
				if(j != 0) {
					csv += ";";
				}
				csv += "\"" + tableModel.modelDetailed[i][j] + "\"";
			}
		}
		
		//Download file
		var blob = new Blob([csv], {type: 'text'});
		saveAs(blob,'exportedTable.csv');
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._freeFormTableModel;
	}
	
	this.getView = function() {
		return this._freeFormTableView;
	}
}