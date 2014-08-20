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

	this.addTable = function(tableIdx) {
		var newTableIndex = tableIdx + 1;
		this._freeFormTableModel.tables.splice(newTableIndex, 0, this._freeFormTableModel.getDefaultTableToAdd()); //Adds to model
		var newTableModel = this._freeFormTableModel.tables[newTableIndex]; //New model
		var $newTableContainer = this._freeFormTableView._getTableWithContainer(newTableModel, newTableIndex); //Creates Table from model
		this._freeFormTableView.addTable(newTableIndex, $newTableContainer);
		
		this.save();
	}
	
	this.deleteTable = function(tableIdx) {
		this._freeFormTableModel.tables.splice(tableIdx, 1); //Removes from model
		this._freeFormTableView.deleteTable(tableIdx); //Removes from view
		
		this.save();
	}
	
	this._updateChangesOnDOMandView = function(tableIdx) {
		this.save();
		var tableModel = this._freeFormTableModel.tables[tableIdx];
		var $tableContainer = $(this._freeFormTableView._tableContainers[tableIdx].children()[1]);
		$tableContainer.empty();
		var isMini = $('#' + 'SwitchFreeFormTable_' + tableIdx).children()[0].checked;
		if(isMini) {
			tableView = this._freeFormTableView._getMiniTable(tableIdx, tableModel.modelMini);
		} else {
			tableView = this._freeFormTableView._getDetailedTable(tableIdx, tableModel.modelDetailed);
		}
		$tableContainer.append(tableView);
	}
	
	this.addRow = function(tableIdx, rowIdx) {
		var tableModel = this._freeFormTableModel.tables[tableIdx];
		
		//Detailed Model
		var numColumns = tableModel.modelDetailed[0].length;
		tableModel.modelDetailed.splice(rowIdx, 0, new Array(numColumns));
		//Mini Model
		tableModel.modelMini.rows.splice(rowIdx, 0, '');
		
		this._updateChangesOnDOMandView(tableIdx);
	}
	
	this.delRow = function(tableIdx, rowIdx) {
		tableModel.modelDetailed.splice(rowIdx, 1);
		
		this._updateChangesOnDOMandView(tableIdx);
	}
	
	this.addColumn = function(tableIdx, colIdx) {
		
		this._updateChangesOnDOMandView(tableIdx);
	}
	
	this.delColumn = function(tableIdx, colIdx) {
		
		this._updateChangesOnDOMandView(tableIdx);
	}
	
	this.importCSV = function(tableIdx) {
		
	}
	
	this.exportCSV = function(tableIdx) {
		
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