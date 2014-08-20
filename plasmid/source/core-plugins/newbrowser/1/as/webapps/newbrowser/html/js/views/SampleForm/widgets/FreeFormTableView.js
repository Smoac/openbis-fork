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
function FreeFormTableView(freeFormTableController, freeFormTableModel) {
	this._freeFormTableController = freeFormTableController;
	this._freeFormTableModel = freeFormTableModel;
	this._container = null;
	this._tableContainers = [];
	
	this._getSwitchForTable = function(tableIdx, $wrappedTable) {
		var _this = this;
		var $switch = $("<div>", {"class" : "switch-toggle well", "style" : "width:33%; margin-left: auto; margin-right: auto; min-height: 38px !important;"});
		var changeEvent = function(index, $tableContainer) {
			return function(event) {
				var isMini = $(this).children()[0].checked;
				var tableData = _this._freeFormTableModel.tables[index];
				var tableView = null;
				$tableContainer.empty();
				
				if(isMini) {
					tableView = _this._getMiniTable(index, tableData.modelMini);
				} else {
					tableView = _this._getDetailedTable(index, tableData.modelDetailed);
				}
				
				$tableContainer.append(tableView);
			}
		}
		
		$switch.change(changeEvent(tableIdx, $wrappedTable));
		
		$switch
			.append($("<input>", {"value" : "mini", "id" : "tableModeMini_" + tableIdx, "name" : "tableMode_" + tableIdx, "type" : "radio", "checked" : ""}))
			.append($("<label>", {"for" : "tableModeMini_" + tableIdx, "onclick" : "", "style" : "padding-top:3px;"}).append("Mini"))
			.append($("<input>", {"value" : "detailed", "id" : "tableModeDetailed_" + tableIdx,"name" : "tableMode_" + tableIdx, "type" : "radio"}))
			.append($("<label>", {"for" : "tableModeDetailed_" + tableIdx, "onclick" : "", "style" : "padding-top:3px;"}).append("Detailed"));
		
		$switch.append($("<a>", {"class" : "btn btn-primary"}));
		return $switch;
	}
	
	this._getFocusEvent = function(tIdx, rIdx, cIdx) {
		var _this = this;
		return function() {
			_this._freeFormTableModel.selectedField = {
				tableIdx : tIdx,
				rowIdx : rIdx,
				columnIdx : cIdx
			};
		};
	}
	
	this._getBlurEvent = function() {
		var _this = this;
		return function() {
			_this._freeFormTableModel.selectedField = null;
		};
	}
	
	this._getMiniTable = function(tableIdx, modelMini) {
		var _this = this;
		var $colsTitle = $("<h4>").append("Columns");
		var $colsContainer = $("<div>");
		for(var i = 0; i < modelMini.columns.length; i++) {
			if(this._freeFormTableModel.isEnabled) {
				var $textField = FormUtil._getInputField('text', null, "Column " + (i+1), null, false);
				$textField.val(modelMini.columns[i]);
				var keyUpEvent = function(columIdx, modelMini) {
					return function() {
						modelMini.columns[columIdx] = $(this).val();
						_this._freeFormTableController.save();
					};
				}
				$textField.keyup(keyUpEvent(i, modelMini));
				$textField.focus(_this._getFocusEvent(tableIdx, null, i));
				$textField.blur(_this._getBlurEvent());
				$colsContainer.append(FormUtil.getFieldForComponentWithLabel($textField, "Column " + (i+1)));
			} else {
				$colsContainer.append(FormUtil.getFieldForLabelWithText("Column " + (i+1), modelMini.columns[i]));
			}
		}
		var $rowsTitle = $("<h4>").append("Rows");
		var $rowsContainer = $("<div>");
		for(var i = 0; i < modelMini.columns.length; i++) {
			if(this._freeFormTableModel.isEnabled) {
				var $textField = FormUtil._getInputField('text', null, "Row " + (i+1), null, false);
				$textField.val(modelMini.rows[i]);
				var keyUpEvent = function(rowIdx, modelMini) {
					return function() {
						modelMini.rows[rowIdx] = $(this).val();
						_this._freeFormTableController.save();
					};
				}
				$textField.keyup(keyUpEvent(i, modelMini));
				$textField.focus(_this._getFocusEvent(tableIdx, i, null));
				$textField.blur(_this._getBlurEvent());
				$rowsContainer.append(FormUtil.getFieldForComponentWithLabel($textField, "Row " + (i+1)));
			} else {
				$rowsContainer.append(FormUtil.getFieldForLabelWithText("Row " + (i+1), modelMini.rows[i]));
			}
		}
		var $container = $("<div>")
							.append($colsTitle)
							.append($colsContainer)
							.append($rowsTitle)
							.append($rowsContainer);
		
		return $container;
	}
	
	this._getDetailedTable = function(tableIdx, modelDetailed) {
		var _this = this;
		var $table = $("<table>", { 'class' : 'table table-bordered' });
		for(var i = 0; i < modelDetailed.length; i++) {
			var $row = $("<tr>");
			$table.append($row);
			for(var j = 0; j < modelDetailed[i].length; j++) {
				var $column = $("<td>");
				$row.append($column);
				
				if(this._freeFormTableModel.isEnabled) {
					var $textField = FormUtil._getInputField('text', null, "Pos (" + (i+1) + "," + (j+1) + ")", null, false);
					$textField.val(modelDetailed[i][j]);
					
					var keyUpEvent = function(rowIdx, columIdx, modelDetailed) {
						return function() {
							modelDetailed[rowIdx][columIdx] = $(this).val();
							_this._freeFormTableController.save();
						};
					}
					$textField.keyup(keyUpEvent(i, j, modelDetailed));
					$textField.focus(_this._getFocusEvent(tableIdx, i, j));
					$textField.blur(_this._getBlurEvent());
					
					$column.append($textField);
				} else {
					$column.append(modelDetailed[i][j]);
				}
			}
			
		}
		return $table;
	}
	
	this._getTableWithContainer = function(tableData, tableIdx) {
		var _this = this;
		var $tableContainer = $("<div>", {"style" : "margin:5px; border-radius:4px 4px 4px 4px;" });
		$tableContainer.css({
			'background-color' : '#EEEEEE',
			'padding' : '10px'
		});
		
		var $title = null;
		
		if(this._freeFormTableModel.isEnabled) {
			$title = $("<input>", { 'type' : 'text', 'style' : 'width:250px;' });
			$title.val(tableData.name);
			var keyUpEvent = function(tableData) {
				return function() {
					tableData.name = $(this).val();
					_this._freeFormTableController.save();
				};
			}
			$title.keyup(keyUpEvent(tableData));
		} else {
			$title = $("<h3>");
			$title.append(tableData.name);
		}
		
		var $wrappedTable = $("<div>", { 'style' : 'margin-top:10px;' }).append(this._getMiniTable(tableIdx, tableData.modelMini));
		
		var $switch = this._getSwitchForTable(tableIdx, $wrappedTable);
		
		var $toolBar = $("<span>", { 'style' : 'margin-left:150px;' });
		
		var $toolBarBtnUcsv = FormUtil.getButtonWithText('Imp. CSV' ,null).attr('title', 'Import from CSV').tooltipster();
		var $toolBarBtnDcsv = FormUtil.getButtonWithText('Exp. CSV' ,null).attr('title', 'Export to CSV').tooltipster();
		
		var $toolBarBtnTACL = FormUtil.getButtonWithImage('./img/table-add-column-left.png' ,null).attr('title', 'Add Column on the left.').tooltipster();
		var $toolBarBtnTACR = FormUtil.getButtonWithImage('./img/table-add-column-right.png' ,null).attr('title', 'Add Column on the right.').tooltipster();
		var $toolBarBtnTDC = FormUtil.getButtonWithImage('./img/table-delete-column.png' ,null).attr('title', 'Delete Column.').tooltipster();
		
		var $toolBarBtnTARA = FormUtil.getButtonWithImage('./img/table-add-row-above.png' ,null).attr('title', 'Add Row above.').tooltipster();
		var $toolBarBtnTARB = FormUtil.getButtonWithImage('./img/table-add-row-below.png' ,null).attr('title', 'Add Row below.').tooltipster();
		var $toolBarBtnTDR = FormUtil.getButtonWithImage('./img/table-delete-row.png' ,null).attr('title', 'Delete Row.').tooltipster();
		
		var $toolBarBtnAT = FormUtil.getButtonWithText('+ Table' ,null).attr('title', 'Add Table.').tooltipster();
		var addTableFunc = function(tableIdx) {
			return function() { _this._freeFormTableController.addTable(tableIdx); };
		}
		$toolBarBtnAT.click(addTableFunc(tableIdx));
		
		var $toolBarBtnDT = FormUtil.getButtonWithText('- Table' ,null).attr('title', 'Delete Table.').tooltipster();
		var removeTableFunc = function(tableIdx) {
			return function() { _this._freeFormTableController.deleteTable(tableIdx); };
		}
		$toolBarBtnDT.click(removeTableFunc(tableIdx));
		
		if(this._freeFormTableModel.isEnabled) {
			$toolBar
				.append($toolBarBtnUcsv).append(' ')
				.append($toolBarBtnDcsv).append(' ')
				.append($toolBarBtnTACL).append(' ')
				.append($toolBarBtnTACR).append(' ')
				.append($toolBarBtnTDC).append(' ')
				.append($toolBarBtnTARA).append(' ')
				.append($toolBarBtnTARB).append(' ')
				.append($toolBarBtnTDR).append(' ')
				.append($toolBarBtnAT).append(' ')
				.append($toolBarBtnDT);
		}
		
		var $titleAndToolbar = $("<div>")
							.append($switch)
							.append($title)
							.append($toolBar);
		
		$tableContainer
			.append($titleAndToolbar)
			.append($wrappedTable);
		
		return $tableContainer;
	}
	
	this.addTable = function(tableIdx, $tableContainer) {
		if(tableIdx === 0) {
			this._container.append($tableContainer); //Update View adding Table
		} else {
			this._tableContainers[tableIdx-1].after($tableContainer); //Update View adding Table
		}
		this._tableContainers.splice(tableIdx, 0, $tableContainer); //Update View Structure
	}
	
	this.deleteTable = function(tableIdx) {
		this._tableContainers[tableIdx].remove();
		this._tableContainers.splice(tableIdx, 1); //Update View Structure
	}
	
	this.repaint = function($container) {
		this._container = $container;
		var tables = this._freeFormTableModel.tables;
		for(var tableIdx = 0; tableIdx < tables.length; tableIdx++) {
			var tableData = tables[tableIdx];
			var $tableContainer = this._getTableWithContainer(tableData, tableIdx);
			this.addTable(tableIdx, $tableContainer);
		}
	}
}