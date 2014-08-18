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
		this._freeFormTableView.repaint($container);
	}
	
	//
	// API
	//
	this.addTable = function(tableIdx) {
		
	}
	
	this.setTableSize = function(tableIdx, numRow, numCols) {
		
	}
	
	this.addRow = function(tableIdx, rowIdx) {
		
	}
	
	this.delRow = function(tableIdx, rowIdx) {
		
	}
	
	this.addColumn = function(tableIdx, rowIdx) {
		
	}
	
	this.delColumn = function(tableIdx, rowIdx) {
		
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