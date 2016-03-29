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

function PlateModel(sample, isDisabled) {
	this.sample = sample;
	
	this.numHeatmapColors = 10;
	this.lastUsedScaleMin = null;
	this.lastUsedScaleMax = null;
	
	if(!this.sample.featureVectorsCache) {
		this.sample.featureVectorsCache = {};
		this.sample.featureVectorsCache.featureVectorDatasets = [];
		this.sample.featureVectorsCache.featureVectorDatasetsFeatures = {};
		this.sample.featureVectorsCache.featureVectorDatasetsFeaturesData = {};
	}
	
	this.isDisabled = isDisabled;
	this.changesToDo = [];
	
	var getRowsAndColsFromPlateSample = function(sample) {
		try {
			var geometryProperty = sample.properties["PLATE_GEOMETRY"];
			if(!geometryProperty) {
				geometryProperty = sample.properties["$PLATE_GEOMETRY"];
			}
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
		//NOTE: We are currently handling two naming conventions ":A01" and ":A1"
		var wellIdentifier = this.sample.identifier + ":" + Util.getLetterForNumber(rowNum) + ((colNum < 10)?"0":"") + colNum;
		var wellIdentifier2 = this.sample.identifier + ":" + Util.getLetterForNumber(rowNum) + colNum;
		var toReturn = null;
		for(var wellIdx = 0; wellIdx < this.sample.contained.length; wellIdx++) {
			if(	this.sample.contained[wellIdx].identifier === wellIdentifier ||
				this.sample.contained[wellIdx].identifier === wellIdentifier2) {
				toReturn = this.sample.contained[wellIdx];
			}
		}
		
		if(!toReturn) {
			toReturn = {};
			toReturn.isEmpty = true;
			toReturn.permId = null;
			toReturn.code = this.sample.code + ":"+ Util.getLetterForNumber(rowNum) + colNum
			toReturn.sampleTypeCode = null;
			toReturn.properties = {};
		}
		
		return toReturn;
	}
	

	this.getPlaceHolderId = function() {
		return "PLATE_TEMPLATE_"+this.sample.permId;
	}

}