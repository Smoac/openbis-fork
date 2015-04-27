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

function PlateModel(sample) {
	this.sample = sample;
	this.wells = null;
	
	var getRowsAndColsFromPlateSample = function(sample) {
		try {
			var geometryProperty = sample.properties["PLATE_GEOMETRY"];
			var dimension = geometryProperty.substring(geometryProperty.lastIndexOf("_") + 1);
			var rowsAndCols = dimension.split("X");
			rowsAndCols[0] = parseInt(rowsAndCols[0]);
			rowsAndCols[1] = parseInt(rowsAndCols[1]);
			return rowsAndCols;
		} catch(err) {
			alert("Property PLATE_GEOMETRY can't be parsed for " + sample.identifier);
		}
		return null;
	}
	
	this.numRows = getRowsAndColsFromPlateSample(sample)[0];
	this.numColumns = getRowsAndColsFromPlateSample(sample)[1];
	
	this.getWell = function(rowNum, colNum) {
		var wellIdentifier = this.sample.identifier + ":" + this.getAlphabetLabel(rowNum) + colNum;
		for(var wellIdx = 0; wellIdx < this.wells.length; wellIdx++) {
			if(this.wells[wellIdx].identifier === wellIdentifier) {
				var toReturn = this.wells[wellIdx];
				return toReturn;
			}
		}
		return null;
	}
	

	this.getPlaceHolderId = function() {
		return "PLATE_TEMPLATE_"+this.sample.permId;
	}
	
	this.getAlphabetLabel = function(number) {
		var alphabet = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
		return alphabet[number];
	}

}