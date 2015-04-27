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
function PlateView(plateController, plateModel) {
	this._plateController = plateController;
	this._plateModel = plateModel;
	
	this.getPlaceHolder = function() {
		var container = $("<div>", { "id" : this._plateModel.getPlaceHolderId() });
		var gridTable = $("<table>", { "class" : "table table-bordered gridTable" });
		
		for(var i = 0; i <= this._plateModel.numRows; i++) {
			var $row = $("<tr>");
			for(var j = 0; j <= this._plateModel.numColumns; j++) {
				var $cell = null;
				if(i === 0 && j === 0) { //Empty cell at the top left
					$cell = $("<th>");
				} else if (i === 0 && j !== 0){ //header with column numbers
					$cell = $("<th>").append(j);
				} else if (j === 0){ //header with row letter
					$cell = $("<th>").append(this._plateModel.getAlphabetLabel(i-1));
				} else {
					$cell = $("<td>").append("&nbsp;");
				}
				$row.append($cell);
			}
			gridTable.append($row);
		}
		container.append(gridTable);
		return container[0].outerHTML
	}
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		var gridTable = $("<table>", { "class" : "table table-bordered gridTable" });
		
		for(var i = 0; i <= this._plateModel.numRows; i++) {
			var $row = $("<tr>");
			for(var j = 0; j <= this._plateModel.numColumns; j++) {
				var $cell = null;
				if(i === 0 && j === 0) { //Empty cell at the top left
					$cell = $("<th>");
				} else if (i === 0 && j !== 0){ //header with column numbers
					$cell = $("<th>").append(j);
				} else if (j === 0){ //header with row letter
					$cell = $("<th>").append(this._plateModel.getAlphabetLabel(i-1));
				} else {
					var well = this._plateModel.getWell(i-1,j);
					$cell = $("<td>").append("&nbsp;");
					if(well) {
						$cell.addClass('well');
						var tooltip = PrintUtil.getTable(well, false, false, false, well.code, true, 'inspectorWhiteFont');
						$cell.tooltipster({
			                content: $(tooltip),
			                interactive: true
			            });
					}
				}
				$row.append($cell);
			}
			gridTable.append($row);
		}
		
		$container.append(gridTable);
	}
}