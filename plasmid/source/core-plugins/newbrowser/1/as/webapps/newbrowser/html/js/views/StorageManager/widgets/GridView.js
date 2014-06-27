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
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		if(this._gridModel.isValid()) {
			
			this._gridTable = $("<table>", { "class" : "storageTable" });
			
			var $headerRow = $("<tr>");
			var $emptyCell = $("<th>");
			$headerRow.append($emptyCell);
			
			for(var j = 0; j < this._gridModel.numColumns; j++) {
				var $numberCell = $("<th>").append(j+1);
				$headerRow.append($numberCell);
			}
			this._gridTable.append($headerRow);
			
			for(var i = 0; i < this._gridModel.numRows; i++) {
				var $newRow = $("<tr>");
				var $numberCell = $("<th>").append(i+1);
				$newRow.append($numberCell);
				
				for(var j = 0; j < this._gridModel.numColumns; j++) {
					var $newColumn = $("<td>");
					
					var clickEvent = function(i, j) {
						return function() {
							_this._posClicked(i + 1, j + 1);
						}
					}
					
					$newColumn.click(clickEvent(i, j));
					$newRow.append($newColumn);
				}
				this._gridTable.append($newRow);
			} 
			
			$container.append(this._gridTable);
			
		} else {
			$container.append("Grid can't be displayed because of missing configuration.");
		}
		
	}
	
	//
	//
	//
	
	this._posClicked = function(posX, posY) {
		alert(posX + " " + posY);
	}
	
	this._labelClicked = function(posX, posY, label) {
		
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._gridModel;
	}
}