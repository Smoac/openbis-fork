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
	this._dilutionTableController = freeFormTableController;
	this._freeFormTableModel = freeFormTableModel;

	this._getSwitchForTable = function(tableIdx) {
		var $switch = $("<div>", {"class" : "switch-toggle well", "style" : "width:33%; margin-left: auto; margin-right: auto; min-height: 38px !important;"});
		$switch.change(function(event) {
			var isMini = $(this).children()[0].checked;
			if(isMini) {
				alert('Mini!');
			} else {
				alert('Detailed!');
			}
		});
		
		$switch
			.append($("<input>", {"value" : "mini", "id" : "tableModeMini", "name" : "tableMode", "type" : "radio", "checked" : ""}))
			.append($("<label>", {"for" : "tableModeMini", "onclick" : "", "style" : "padding-top:3px;"}).append("Mini"))
			.append($("<input>", {"value" : "detailed", "id" : "tableModeDetailed","name" : "tableMode", "type" : "radio"}))
			.append($("<label>", {"for" : "tableModeDetailed", "onclick" : "", "style" : "padding-top:3px;"}).append("Detailed"));
		
		$switch.append($("<a>", {"class" : "btn btn-primary"}));
		return $switch;
	}
	
	this._getMiniTable = function(modelMini) {
		var $colsTitle = $("<h4>").append("Columns");
		var $colsContainer = $("<div>");
		for(var i = 0; i < modelMini.columns.length; i++) {
			if(this._freeFormTableModel.isEnabled) {
				var $textField = FormUtil._getInputField('text', null, "Column " + (i+1), null, false);
				$textField.val(modelMini.columns[i]);
				$colsContainer.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
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
				$rowsContainer.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
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
	
	this._getDetailedTable = function(modelDetailed) {
		var $table = $("<table>");
		for(var i = 0; i < modelDetailed.length; i++) {
			var $row = $("<tr>");
			$table.append($row);
			for(var j = 0; j < modelDetailed[i].length; j++) {
				var $column = $("<td>");
				$row.append($column);
				
				if(this._freeFormTableModel.isEnabled) {
					var $textField = FormUtil._getInputField('text', null, "Pos (" + (i+1) + "," + (j+1) + ")", null, false);
					$textField.val(modelDetailed[i][j]);
					$column.append($textField);
				} else {
					$column.append(modelDetailed[i][j]);
				}
			}
			
		}
		return $table;
	}
	
	this.repaint = function($container) {
		$container.empty();
		for(var tableIdx = 0; tableIdx < this._freeFormTableModel.tables.length; tableIdx++) {
			var tableData = this._freeFormTableModel.tables[tableIdx];
			var $tableContainer = $("<div>");
			$tableContainer.css({
				//'background-color' : 'lightgrey',
				'padding' : '10px'
			});
			
			var $title = null;
			
			if(this._freeFormTableModel.isEnabled) {
				$title = $("<input>", { 'type' : 'text', 'style' : 'width:250px;' });
				$title.val(tableData.name);
			} else {
				$title = $("<h3>");
				$title.append(tableData.name);
			}
			
			var $switch = this._getSwitchForTable(tableIdx);
			
			var $toolBar = $("<span>", { 'style' : 'margin-left:150px;' });
			
			var $toolBarBtnTACL = FormUtil.getButtonWithImage('./img/table-add-column-left.png' ,null);
			var $toolBarBtnTACR = FormUtil.getButtonWithImage('./img/table-add-column-right.png' ,null);
			var $toolBarBtnTDC = FormUtil.getButtonWithImage('./img/table-delete-column.png' ,null);
			
			var $toolBarBtnTARA = FormUtil.getButtonWithImage('./img/table-add-row-above.png' ,null);
			var $toolBarBtnTARB = FormUtil.getButtonWithImage('./img/table-add-row-below.png' ,null);
			var $toolBarBtnTDR = FormUtil.getButtonWithImage('./img/table-delete-row.png' ,null);
			
			var $toolBarBtnAT = FormUtil.getButtonWithText('Add Table' ,null);
			
			if(this._freeFormTableModel.isEnabled) {
				$toolBar
					.append($toolBarBtnTACL).append(' ')
					.append($toolBarBtnTACR).append(' ')
					.append($toolBarBtnTDC).append(' ')
					.append($toolBarBtnTARA).append(' ')
					.append($toolBarBtnTARB).append(' ')
					.append($toolBarBtnTDR).append(' ')
					.append($toolBarBtnAT);
			}
			
			var $titleAndToolbar = $("<div>")
								.append($switch)
								.append($title)
								.append($toolBar);
			
			$tableContainer.append($titleAndToolbar);
			//$tableContainer.append(this._getMiniTable(tableData.modelMini));
			$tableContainer.append(this._getDetailedTable(tableData.modelDetailed));
			
			$container.append($tableContainer);
		}
	}
	
	
}