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

function GridView(gridModel) {
	this._gridModel = gridModel;
	this._gridTable = null;
	this._posClickedEventHandler = null;
	this._labelClickedEventHandler = null;
	
	this.repaint = function($container) {
		$container.empty();
		if(this._gridModel.isValid()) {
			this._gridTable = this._getGridTable();
			$container.append(this._gridTable);
		} else {
			$container.append("Grid can't be displayed because of missing configuration.");
		}
		
	}
	
	this._getGridTable = function() {
		var _this = this;
		var gridTable = $("<table>", { "class" : "table table-bordered gridTable" });
		var $headerRow = $("<tr>");
		var $emptyCell = $("<th>");
		$headerRow.append($emptyCell);
		
		for(var j = 0; j < this._gridModel.numColumns; j++) {
			var $numberCell = $("<th>").append(j+1);
			$headerRow.append($numberCell);
		}
		
		gridTable.append($headerRow);
		
		for(var i = 0; i < this._gridModel.numRows; i++) {
			var $newRow = $("<tr>");
			var $numberCell = $("<th>").append(i+1);
			$newRow.append($numberCell);
			
			for(var j = 0; j < this._gridModel.numColumns; j++) {
				var $newColumn = $("<td>");
				
				var clickEvent = function(i, j) {
					return function(event) {
						event.stopPropagation();
						_this._posClicked(i + 1, j + 1);
					}
				}
				
				if(!this._gridModel.isDisabled) {
					$newColumn.click(clickEvent(i, j));
				}
				
				this._addLabels($newColumn, i + 1, j + 1);
				
				$newRow.append($newColumn);
			}
			gridTable.append($newRow);
		}
		return gridTable;
	}
	
	this._addLabels = function($component, posX, posY) {
		var _this = this;
		var labels = this._gridModel.getLabels(posX, posY);
		if(labels) {
			for(var i = 0; i < labels.length; i++) {
				var labelContainer = $("<div>", { class: "storageBox" }).append(labels[i].displayName);
				
				var clickEvent = function(posX, posY, label, data) {
					return function(event) {
						event.stopPropagation();
						_this._labelClicked(posX, posY, label, data);
					}
				}
				
				if(!this._gridModel.isDisabled) {
					labelContainer.click(clickEvent(posX, posY, labels[i].displayName, labels[i].data));
				}
				
				$component.append(labelContainer);
			}
		}
	}
	
	//
	// Event Handlers
	//
	this._posClicked = function(posX, posY) {
		if(this._posClickedEventHandler) {
			this._posClickedEventHandler(posX, posY);
		}
	}
	
	this._labelClicked = function(posX, posY, label, data) {
		if(this._labelClickedEventHandler) {
			this._labelClickedEventHandler(posX, posY, label, data);
		}
	}
	
	//
	// Setters
	//
	this.setPosSelectedEventHandler = function(posClickedEventHandler) {
		this._posClickedEventHandler = posClickedEventHandler;
	}
	
	this.setLabelSelectedEventHandler = function(labelClickedEventHandler) {
		this._labelClickedEventHandler = labelClickedEventHandler;
	}
}