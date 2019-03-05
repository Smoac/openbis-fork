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
	this._$featureVectorDatasetFeaturesDropdown = null;
	this._$featureVectorDatasetsDropdown = null;
	this._$scaleDropdown = null;
	this._$scaleMax = null;
	this._$scaleMin = null;
	this._$exportHighlighted = null;
	this._$scaleDropdownContainer = null;
	this._$gridTable = null;
	this._$gridTableContainer = $("<div>");
	this._gridTableCells = null;
	this._$scale = $("<span>");
	this._$toolbar = $("<div>");
	
	this.getPlaceHolder = function() {
		var minHeight = 29 * this._plateModel.numRows + 400;
		var container = $("<div>", { "id" : this._plateModel.getPlaceHolderId(), "style" : "min-height: " + minHeight + "px;" });
//		var gridTable = this.getGridTable(false);
//		container.append(gridTable);
		return container[0].outerHTML
	}
	
	this._setScaleFields = function(min, max) {
		this._$scaleMin.val(min);
		this._$scaleMax.val(max);
		this._plateModel.lastUsedScaleMin = min;
		this._plateModel.lastUsedScaleMax = max;
	}
	
	this._repaintScaleDropDown = function(isNew, isEmpty) {
		var _this = this;
		
		if(isNew) {
			this._$scaleDropdownContainer = $("<span>");
			
			//Scale
			this._$scaleDropdown = FormUtil.getDropDownForTerms(
					"scaleDropdown-" + this._plateModel.sample.permId,
					[],
					"Scaling options",
					false
					);
			this._$scaleDropdown.addClass("featureToolbarOptionSmall");
			
			this._$scaleDropdown.change(function(event) {
				var selectedFeatureVector = _this._$featureVectorDatasetsDropdown.val();
				var selectedFeature = _this._$featureVectorDatasetFeaturesDropdown.val();
				var selectedScale = _this._$scaleDropdown.val();
				var scale = null;
				
				if(selectedScale === 'Min/Max') {
					scale = _this._getMaxMinScale(selectedFeatureVector, selectedFeature);
				} else if(selectedScale === '10/90') {
					scale = _this._getMaxMinScale(selectedFeatureVector, selectedFeature);
					var scale10Percent = Math.abs(scale.max / 10);
					scale.max = scale.max - scale10Percent;
					scale.min = scale.min + scale10Percent;
				}
				
				_this._repaintGridToFeatureVectorColors(
						selectedFeatureVector,
						selectedFeature,
						scale);
			});
			
			//Max and Min
			this._$scaleMax = FormUtil.getTextInputField("scaleMax-" + this._plateModel.sample.permId, "Max Scale Value", false);
			this._$scaleMax.addClass("featureToolbarOptionSmall");
			this._$scaleMax.change(function() {
				var newMax = null;
				try {
					newMax = parseFloat(_this._$scaleMax.val());
				} catch(err) {
					
				}
				
				if(	newMax && 
					newMax !== _this._plateModel.lastUsedScaleMax && 
					newMax > _this._plateModel.lastUsedScaleMin) {	
					
					var selectedFeatureVector = _this._$featureVectorDatasetsDropdown.val();
					var selectedFeature = _this._$featureVectorDatasetFeaturesDropdown.val();
					
					var scale = {
							min : _this._plateModel.lastUsedScaleMin,
							max : newMax
					}
					
					_this._repaintGridToFeatureVectorColors(
							selectedFeatureVector,
							selectedFeature,
							scale);
				} else {
					_this._$scaleMax.val(_this._plateModel.lastUsedScaleMax);
				}
			});
			this._$scaleMin = FormUtil.getTextInputField("scaleMin-" + this._plateModel.sample.permId, "Min Scale Value", false);
			this._$scaleMin.addClass("featureToolbarOptionSmall");
			this._$scaleMin.change(function() {
				var newMin = null;
				try {
					newMin = parseFloat(_this._$scaleMin.val());
				} catch(err) {
					
				}
				
				if(	newMin && 
					newMin !== _this._plateModel.lastUsedScaleMin && 
					newMin < _this._plateModel.lastUsedScaleMax) {
					
					var selectedFeatureVector = _this._$featureVectorDatasetsDropdown.val();
					var selectedFeature = _this._$featureVectorDatasetFeaturesDropdown.val();
					
					var scale = {
							min : newMin,
							max : _this._plateModel.lastUsedScaleMax
					}
					
					_this._repaintGridToFeatureVectorColors(
							selectedFeatureVector,
							selectedFeature,
							scale);
				} else {
					_this._$scaleMin.val(_this._plateModel.lastUsedScaleMin);
				}
			});
			
			//Build Set
			
			this._$exportHighlighted = FormUtil.getButtonWithText("Export Highlighted");
			this._$exportHighlighted.hide();
			this._$exportHighlighted.css({ "vertical-align" : "baseline" });
			this._$exportHighlighted.click(function() {
				var selectedFeatureVector = _this._$featureVectorDatasetsDropdown.val();
				var selectedFeature = _this._$featureVectorDatasetFeaturesDropdown.val();
				var allFeatures = _this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[selectedFeatureVector];
				
				if(selectedFeatureVector && selectedFeature) {
					var scale = {
							min : _this._plateModel.lastUsedScaleMin,
							max : _this._plateModel.lastUsedScaleMax
					}
					
					var dataToExport = _this._getSelectedWells( selectedFeatureVector, 
																selectedFeature,
																scale);
					
					var headers = [];
						
					var getIdx = function(header) {
						var idx = $.inArray(header, headers);
						if(idx === -1) {
							headers.push(header);
							idx = $.inArray(header, headers);
						}
						return idx;
					}
					
					var rows = [];
					rows.push(headers);
					for(var dIdx = 0; dIdx < dataToExport.length; dIdx++) {
						var item = dataToExport[dIdx];
						var itemRow = [];
						
						var sample = item.sample;
						if(sample) {
							itemRow[getIdx("permId")] = sample.permId;
							itemRow[getIdx("sampleTypeCode")] = sample.sampleTypeCode;
							itemRow[getIdx("code")] = sample.code;
							itemRow[getIdx("identifier")] = sample.identifier;
							itemRow[getIdx("spaceCode")] = sample.spaceCode;
						}
						
						if(sample.properties) {
							for(key in sample.properties) {
								itemRow[getIdx(key)] = sample.properties[key];
							}
						}
						var sampleFeatures = item.sampleFeatures;
						
						if(sampleFeatures) {
							if(sampleFeatures.wellPosition) {
								itemRow[getIdx("wellRow")] = sampleFeatures.wellPosition.wellRow;
								itemRow[getIdx("wellColumn")] = sampleFeatures.wellPosition.wellColumn;
							} 
							
							for(var fIdx = 0; fIdx < allFeatures.featureCodes.length; fIdx++) {
								var fCode = allFeatures.featureCodes[fIdx];
								if(sampleFeatures.vocabularyFeatureFlags[fIdx]) {
									itemRow[getIdx(fCode)] = sampleFeatures.vocabularyTerms[fIdx];
								} else {
									itemRow[getIdx(fCode)] = sampleFeatures.values[fIdx];
								}
							}
						}
						rows.push(itemRow);
					}
					Util.downloadTSV(rows, "plate-" + _this._plateModel.sample.code + ".tsv");
				}
			});
			
			this._$scaleDropdownContainer
				.append(this._$scaleDropdown)
				.append(" ")
				.append(this._$scaleMin)
				.append(" ")
				.append(this._$scaleMax)
				.append(" ")
				.append(this._$exportHighlighted);
			this._$scaleDropdown.select2({ width: '100%', placeholder: "Select a scale option", theme: "bootstrap" });
			
		}
		
		this._$scaleDropdown.empty();
		if(isEmpty) {
			this._$scaleMax.hide();
			this._$scaleMin.hide();
			this._$exportHighlighted.hide();
		} else {
			this._$scaleDropdown.append($("<option>").attr('value', 'Min/Max').text('Min/Max'));
			this._$scaleDropdown.append($("<option>").attr('value', '10/90').text('10/90'));
			this._$scaleMax.show();
			this._$scaleMin.show();
			this._$exportHighlighted.show();
		}
	}
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		//Paint Toolbar
		if( this._plateModel.isDisabled &&
			this._plateModel.sample.featureVectorsCache.featureVectorDatasets.length > 0) {
			
			//Scale
			this._repaintScaleDropDown(true, true);
			
			//Selected Feature Vector Dataset Features
			this._$featureVectorDatasetFeaturesDropdown = FormUtil.getDropDownForTerms(
					"featureVectorDatasetFeaturesDropdown-" + this._plateModel.sample.permId,
					[],
					"Select a feature",
					false
					);
			this._$featureVectorDatasetFeaturesDropdown.addClass("featureToolbarOption");
			this._$featureVectorDatasetFeaturesDropdown.change(function(event) {
				var selectedFeature = $(this).val();
				if(selectedFeature) {
					//Update Scale Dropdown
					_this._repaintScaleDropDown(false, false);
					
					//Update Feature
					var featureVectorDatasetCode = _this._$featureVectorDatasetsDropdown.val();
					
					if(_this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode]) {
						_this._repaintGridToFeatureVectorColors(featureVectorDatasetCode, selectedFeature);
					} else {
						Util.blockUI();
						var featuresCodesFromFeatureVector = [];
						for(code in _this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode]) {
							featuresCodesFromFeatureVector.push(code);
						}
						
						mainController.serverFacade.customELNApi({
							"method" : "getFeaturesFromFeatureVector",
							"samplePlatePermId" : _this._plateModel.sample.permId,
							"featureVectorDatasetPermId" : featureVectorDatasetCode,
							"featuresCodesFromFeatureVector" : featuresCodesFromFeatureVector
						}, function(error, result){
							//Get features data
							var receivedData = result.data[0];
							var dataByPosition = [];
							for(var wellIdx = 0; wellIdx < receivedData.featureVectors.length; wellIdx++) {
								var wellData = receivedData.featureVectors[wellIdx];
								var wellRow = wellData.wellPosition.wellRow;
								var wellColumn = wellData.wellPosition.wellColumn;
								if(!dataByPosition[wellRow]) {
									dataByPosition[wellRow] = [];
								}
								for(var vIdx = 0; vIdx < wellData.values.length; vIdx++) {
									var value = wellData.values[vIdx];
									if(value.toPrecision) {
										wellData.values[vIdx] = parseFloat(value.toPrecision(9));
									}
								}
								dataByPosition[wellRow][wellColumn] = wellData;
							}
							receivedData.featureVectors = dataByPosition;
							
							_this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode] = result.data[0];
							_this._repaintGridToFeatureVectorColors(featureVectorDatasetCode, selectedFeature);
							Util.unblockUI();
						});
					}
				} else {
					_this._repaintScaleDropDown(false, true);
					_this._repaintGridToAnnotationsColors();
				}
			});
			
			//Feature Vector Dataset Dropdown
			var featureVectorDatasets = this._plateModel.sample.featureVectorsCache.featureVectorDatasets;
			var featureVectorDatasetsDropdowTerms = [];
			for(var fvdIdx = 0; fvdIdx < featureVectorDatasets.length; fvdIdx++) {
				featureVectorDatasetsDropdowTerms.push({
					code : featureVectorDatasets[fvdIdx],
					label : featureVectorDatasets[fvdIdx]
				})
			}
			
			this._$featureVectorDatasetsDropdown = FormUtil.getDropDownForTerms(
												"featureVectorDatasetsDropdow-" + this._plateModel.sample.permId,
												featureVectorDatasetsDropdowTerms,
												"Select a Feature Vector Dataset",
												false
												);
			this._$featureVectorDatasetsDropdown.addClass("featureToolbarOption");
			
			this._$featureVectorDatasetsDropdown.change(function(event) {
				var featureVectorDatasetCode = $(this).val();
				if(!featureVectorDatasetCode) {
					_this._$featureVectorDatasetFeaturesDropdown.empty();
					_this._$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', '').text("Choose a Feature Vector Dataset first"));
					_this._repaintGridToAnnotationsColors();
					
					_this._repaintScaleDropDown(false, true);
				} else {
					var featureVectorDatasetFeatures = _this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode];
					
					var loadFeatureVectorDatasetFeatures = function() {
						var featureVectorDatasetFeatures = _this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode];
						_this._$featureVectorDatasetFeaturesDropdown.empty();
						_this._$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', '').attr('selected', '').text(''));
						for(featureVectorDatasetFeatureCode in featureVectorDatasetFeatures) {
							_this._$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', featureVectorDatasetFeatureCode).text(featureVectorDatasetFeatures[featureVectorDatasetFeatureCode]));
						}
					}
					
					if(!featureVectorDatasetFeatures) {
						Util.blockUI();
						mainController.serverFacade.customELNApi({
							"method" : "listAvailableFeatures",
							"samplePlatePermId" : _this._plateModel.sample.permId,
							"featureVectorDatasetPermId" : featureVectorDatasetCode
						}, function(error, result){
							if(error) {
								Util.showError(error);
							} else {
								_this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[featureVectorDatasetCode] = result.data;
								loadFeatureVectorDatasetFeatures();
							}
							Util.unblockUI();
						});
					} else {
						loadFeatureVectorDatasetFeatures();
					}
				}
			});
			
			//Build Toolbar
			this._$toolbar.append(this._$featureVectorDatasetsDropdown)
					.append(this._$featureVectorDatasetFeaturesDropdown)
					.append(this._$scaleDropdownContainer);
			this._$featureVectorDatasetsDropdown.select2({ width: '100%', theme: "bootstrap" });
			this._$featureVectorDatasetFeaturesDropdown.select2({ width: '100%', placeholder: "Select a Feature", theme: "bootstrap" });
		}
		
		//Paint grid
		this._$gridTable = this.getGridTable(true);
		this._$gridTableContainer.append(this._$gridTable);
		$container.append(this._$scale).append(this._$toolbar).append(this._$gridTableContainer);
		
		//Painting default colors before appending the table, increased performance
		this._repaintGridToAnnotationsColors();
	}
	
	this.getGridTable = function(withWells) {
		var $gridTable = $("<table>", { "class" : "table table-bordered gridTable", "style" : "table-layout: fixed;" });
		$gridTable.css('background-image', 'url("./img/empty-pattern.png")');
		$gridTable.css('background-repeat', 'repeat');
		
		for(var i = 0; i <= this._plateModel.numRows; i++) {
			var $row = $("<tr>");
			for(var j = 0; j <= this._plateModel.numColumns; j++) {
				var $cell = null;
				if(i === 0 && j === 0) { //Empty cell at the top left
					$cell = $("<th>");
					$cell.css('background-color', '#ffffff');
				} else if (i === 0 && j !== 0){ //header with column numbers
					$cell = $("<th>").append(j);
					$cell.css('background-color', '#ffffff');
				} else if (j === 0){ //header with row letter
					$cell = $("<th>").append(Util.getLetterForNumber(i));
					$cell.css('background-color', '#ffffff');
				} else {
					$cell = $("<td>").append("&nbsp;");
					$cell.addClass('well');
					
					if(withWells) {
						if(!this._gridTableCells) {
							this._gridTableCells = [];
						}
						if(!this._gridTableCells[i]) {
							this._gridTableCells[i] = [];
						}
						
						this._gridTableCells[i][j] = { cell : $cell, tooltip : null };
						
						this._setToolTip($cell, i, j);
					}
				}
				
				$cell.css('width', Math.floor(80/this._plateModel.numColumns+1) +'%');
				$row.append($cell);
			}
			$gridTable.append($row);
		}	
		return $gridTable;
	}
	
	//
	// Utility methods to handle table cells
	//
	
	this._textToColor = function(text) {
		if(text) {
			var hash = this._djb2Code(text);
			var rgb = {
				r: (hash & 0xFF0000) >> 16,
				g: (hash & 0x00FF00) >> 8,
				b: hash & 0x0000FF
			};
			
			return "rgb(" + rgb.r + "," + rgb.g + "," + rgb.b + ")";
		} else {
			return null;
		}
	}
	
	this._djb2Code = function(str) {
        var hash = 5381;
        for (i = 0; i < str.length; i++) {
            char = str.charCodeAt(i);
            hash = ((hash << 5) + hash) + char; /* hash * 33 + c */
        }
        return hash;
    }
	
	this._repaintWellToColor = function(row, column, rgbColor, txt, isDisabled) {
		var $cell = this._gridTableCells[row][column].cell;
		var currentTooltip = this._gridTableCells[row][column].tooltip;
		var newToolTip = (this._$featureVectorDatasetsDropdown)?this._$featureVectorDatasetsDropdown.val():null;
		
		if(currentTooltip !== newToolTip) {
			this._setToolTip($cell, row, column);
			this._gridTableCells[row][column].tooltip = newToolTip;
		}
		
		//Opacity
		var opacity = 1;
		if(isDisabled) {
			opacity = 0.3;
		}
		
		//Single Cell CSS Update
		$cell.css({ "background-color" : rgbColor, "opacity" : opacity });
	}
	
	this._cleanGrid = function() {
		//Delete Scale
		this._$scale.empty();
		//Clean colors
		for(var i = 1; i <= this._plateModel.numRows; i++) {
			for(var j = 1; j <= this._plateModel.numColumns; j++) {
				this._repaintWellToColor(i, j, "inherit");
				
			}
		}
	}
	
	//
	// Utility methods to handle tool tips
	//
	this._setToolTip = function($cell, row, column) {
		var well = this._plateModel.getWell(row, column);
		var colorEncodedWellAnnotationsSelector = "";
		
		if(!well.isEmpty) {
			//Color Annotations only enabled for wells with a type having $WELL.COLOR_ENCODED_ANNOTATION property
			var isAnnotableWell = jQuery.inArray("$WELL.COLOR_ENCODED_ANNOTATION", profile.getAllPropertiCodesForTypeCode(well.sampleTypeCode) ) !== -1;
			if(isAnnotableWell) {
				colorEncodedWellAnnotationsSelector = this._getWellColorAnnotationGroups($cell, well);
			}							
		}
		
		var featureProperties = this._getSelectedFeatureVectorsProperties(row, column);
		
		var tooltip = PrintUtil.getTable(well, false, null, 'inspectorWhiteFont',
				'colorEncodedWellAnnotations-holder-' + well.permId,
				colorEncodedWellAnnotationsSelector, featureProperties);
		
		if($cell.hasClass("tooltipstered")) {
			$cell.tooltipster('destroy');
		}
		
		$cell.tooltipster({
			content: $(tooltip),
			interactive: true,
			position : 'right'
		});
	}
	
	this._getSelectedFeatureVectorsProperties = function(row, column) {
		var selectedDatasetCode = null;
		var features = null;
		var featuresData = null;
		var properties = {};
		if(this._$featureVectorDatasetsDropdown) {
			selectedDatasetCode = this._$featureVectorDatasetsDropdown.val();
			features = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeatures[selectedDatasetCode];
			featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[selectedDatasetCode];
		}
		
		if( selectedDatasetCode && 
			features && 
			featuresData) {
			var wellData = featuresData.featureVectors[row][column];
			for(var vIdx = 0; vIdx < featuresData.featureCodes.length; vIdx++) {
				var property = {
						code : featuresData.featureCodes[vIdx],
						label : featuresData.featureLabels[vIdx],
						dataType : null,
						value : null
				};
				
				if(wellData.vocabularyFeatureFlags[vIdx]) {
					property.dataType = "CONTROLLEDVOCABULARY";
					property.value = wellData.vocabularyTerms[vIdx];
				} else {
					property.dataType = "REAL";
					property.value = wellData.values[vIdx];
				}
				
				properties[property.code] = property;
			}
		}
		
		return properties;
	}
	
	//
	// Utility methods to handle feature vectors
	//
	this._getFeatureIndex = function(featureVectorDatasetCode, featureCode) {
		var featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode];
		for(var fIdx = 0; fIdx < featuresData.featureCodes.length; fIdx++) {
			if(featuresData.featureCodes[fIdx] === featureCode) {
				return fIdx;
			}
		}
	}
	
	this._isFeatureVocabulary = function(featureVectorDatasetCode, featureCode) {
		//1. Obtain feature Index
		var featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode];
		var featureIndex = this._getFeatureIndex(featureVectorDatasetCode, featureCode);
		
		//2. Check is Vocabulary
		for(var rowsIdx = 1; rowsIdx < featuresData.featureVectors.length; rowsIdx++) {
			for(var colsIdx = 1; colsIdx < featuresData.featureVectors.length; colsIdx++) {
				var wellData = featuresData.featureVectors[rowsIdx][colsIdx];
				return wellData.vocabularyFeatureFlags[featureIndex];
			}
		}
	}
	
	this._getMaxMinScale = function(featureVectorDatasetCode, featureCode) {
		//1. Obtain feature Index
		var featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode];
		var featureIndex = this._getFeatureIndex(featureVectorDatasetCode, featureCode);
		
		//2. Obtain Max/Min scale
		var minValue = null;
		var maxValue = null;
		var isVocabulary = this._isFeatureVocabulary(featureVectorDatasetCode, featureCode);
		
		for(var rowsIdx = 1; rowsIdx < featuresData.featureVectors.length; rowsIdx++) {
			for(var colsIdx = 1; colsIdx < featuresData.featureVectors[rowsIdx].length; colsIdx++) {
				var wellData = featuresData.featureVectors[rowsIdx][colsIdx];
				if(!isVocabulary) { //Don't support vocabularies on Max/Min Scale
					var value = wellData.values[featureIndex];
					if(value !== "NaN") {
						if(!minValue || value < minValue) {
							minValue = value;
						}
						
						if(!maxValue || value > maxValue) {
							maxValue = value;
						}
					}
				}
			}
		}
		
		return {
			max : maxValue,
			min : minValue
		};
	}
	
	this._getSelectedWells = function(featureVectorDatasetCode, featureCode, visibleScale) {
		//1. Obtain feature
		var featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode];
		var featureIndex = this._getFeatureIndex(featureVectorDatasetCode, featureCode);
		var isVocabulary = this._isFeatureVocabulary(featureVectorDatasetCode, featureCode);
		
		if(isVocabulary) { //Don't paint vocabularies
			return;
		}
		
		//2. Obtain wells inside of the scale
		var wellsWithFeatureVectors = [];
		for(var rowsIdx = 1; rowsIdx < featuresData.featureVectors.length; rowsIdx++) {
			for(var colsIdx = 1; colsIdx < featuresData.featureVectors[rowsIdx].length; colsIdx++) {
				var wellData = featuresData.featureVectors[rowsIdx][colsIdx];
				if(wellData.values[featureIndex] !== "NaN") {
					var isOutOfScaleRange = wellData.values[featureIndex] < visibleScale.min || wellData.values[featureIndex] > visibleScale.max;
					if(!isOutOfScaleRange) {
						wellsWithFeatureVectors.push({
							sample: this._plateModel.getWell(rowsIdx, colsIdx),
							sampleFeatures : wellData
						});
					}
				}
			}
		}
		return wellsWithFeatureVectors;
	}
	
	this._repaintGridToFeatureVectorColors = function(featureVectorDatasetCode, featureCode, visibleScale) {
		//1. Clean grid and scale
		this._cleanGrid();
		this._$scale.empty();
		
		//2. Obtain feature
		var featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode];
		var featureIndex = this._getFeatureIndex(featureVectorDatasetCode, featureCode);
		var isVocabulary = this._isFeatureVocabulary(featureVectorDatasetCode, featureCode);
		
		if(isVocabulary) { //Don't paint vocabularies
			this._repaintScaleDropDown(false, true);
			for(var rowsIdx = 1; rowsIdx < featuresData.featureVectors.length; rowsIdx++) {
				for(var colsIdx = 1; colsIdx < featuresData.featureVectors[rowsIdx].length; colsIdx++) {
					var wellData = featuresData.featureVectors[rowsIdx][colsIdx];
					if(wellData.vocabularyTerms[featureIndex]) {
						var color = this._textToColor(wellData.vocabularyTerms[featureIndex]);
						this._repaintWellToColor(rowsIdx, colsIdx, color, "", false);
					}
				}
			}
		} else {
			//3. Define Color Step
			var minMaxScale = this._getMaxMinScale(featureVectorDatasetCode, featureCode);
			if(!visibleScale) {
				visibleScale = minMaxScale;
			}
			var minValue = minMaxScale.min;
			var maxValue = minMaxScale.max;
			var shiftedMaxValue = null;
			var shiftedMinValue = null;
			var totalValuesScale = maxValue - minValue;
			
			if(minValue < 0) {
				shiftedMinValue = Math.abs(minValue);
			} else {
				shiftedMinValue = 0;
			}
			
			shiftedMaxValue =  maxValue + shiftedMinValue;
			var colorStepSize = shiftedMaxValue / this._plateModel.numHeatmapColors;
				
			//4. Paint Colors
			for(var rowsIdx = 1; rowsIdx < featuresData.featureVectors.length; rowsIdx++) {
				for(var colsIdx = 1; colsIdx < featuresData.featureVectors[rowsIdx].length; colsIdx++) {
					var wellData = featuresData.featureVectors[rowsIdx][colsIdx];
					if(wellData.values[featureIndex] !== "NaN") {
						var isOutOfScaleRange = wellData.values[featureIndex] < visibleScale.min || wellData.values[featureIndex] > visibleScale.max;
						var value = wellData.values[featureIndex] + shiftedMinValue;
						var valueColorStep = Math.ceil(value / colorStepSize);
						if (valueColorStep === 0) { //Corner case - lower value and negative number
							valueColorStep = 1;
						} else if(isNaN(valueColorStep) && value === 0) { //Corner case - all numbers are 0
							valueColorStep = 1;
						}
						var color = this._getColorForStepBetweenWhiteAndBlack(valueColorStep, this._plateModel.numHeatmapColors);
						this._repaintWellToColor(rowsIdx, colsIdx, color, valueColorStep, isOutOfScaleRange);
					} else {
						this._repaintWellToColor(rowsIdx, colsIdx, "#ffffff", "", true); //Out of scale NaN value
					}
				}
			}
			
			//5. Paint Scale
			this._setScaleFields(visibleScale.min, visibleScale.max);
			if(totalValuesScale > 0) {
				this._repaintScale(shiftedMinValue, colorStepSize, this._plateModel.numHeatmapColors);
			}
		}
	}
	
	this._repaintScale = function(shift, colorStepSize, numSteps) {
		var $scaleTable = $("<table>", { "class" : "table table-bordered", "style" : "table-layout: fixed;" });
		
		var $rowIndex = $("<tr>");
		$scaleTable.append($rowIndex);
		var $rowValue = $("<tr>");
		$scaleTable.append($rowValue);
		var $rowColor = $("<tr>");
		$scaleTable.append($rowColor);
		
		for(var i = 1; i <= numSteps; i++) {
			$rowValue.append($("<td>", { "style" : "background-color:" + this._getColorForStepBetweenWhiteAndBlack(i, numSteps) + ";" }));
		}
		
		for(var i = 1; i <= numSteps; i++) {
			var colorStepSizeI = colorStepSize*i;
			$rowColor.append($("<td>").append(parseFloat(colorStepSizeI.toPrecision(9)) - shift));
		}
		
		this._$scale.empty();
		this._$scale.append($scaleTable);
	}
	
	this._getColorForStepBetweenWhiteAndBlack = function(step, numSteps) {
		var stepSize = Math.floor(255/numSteps);
		var revertStep = numSteps - step + 1;
		var greyValue = Math.round(stepSize * revertStep);
		var hexValue = greyValue.toString(16);
		var color = "#" + hexValue + hexValue + hexValue;
		return color;
	}
	
	//
	// Utility methods for color annotations
	//
	this._repaintGridToAnnotationsColors = function() {
		this._cleanGrid();
		for(var i = 1; i <= this._plateModel.numRows; i++) {
			for(var j = 1; j <= this._plateModel.numColumns; j++) {
				this._repaintWellToColorAnnotation(i, j);
			}
		}
	}
	
	this._repaintWellToColorAnnotation = function(row, column) {
		var well = this._plateModel.getWell(row,column);
		if(!well.isEmpty) {
			var isAnnotableWell = jQuery.inArray("$WELL.COLOR_ENCODED_ANNOTATION", profile.getAllPropertiCodesForTypeCode(well.sampleTypeCode) ) !== -1;
			if(isAnnotableWell) {
				var selectedColorEncodedAnnotation = well.properties["$WELL.COLOR_ENCODED_ANNOTATION"];
				if(selectedColorEncodedAnnotation && selectedColorEncodedAnnotation !== "DEFAULT") {
					this._repaintWellToColor(row, column, this._getColorForAnnotationCode(selectedColorEncodedAnnotation));
				} else {
					this._repaintWellToColor(row, column, "", false);
				}
			} else {
				this._repaintWellToColor(row, column, "", false);
			}
		} else {
			this._repaintWellToColor(row, column, "transparent", false);
		}
	}
	
	this._getColorForAnnotationCode = function(code) {
		var valueInfo = profile.getVocabularyTermByCodes("$WELL.COLOR_ENCODED_ANNOTATIONS", code);
		if(valueInfo) {
			return valueInfo.description.split(":")[0].trim();
		}
		return null;
	}
	
	this._getWellColorAnnotationGroups = function($cell, well) {
		var selectedAnnotation = well.properties["$WELL.COLOR_ENCODED_ANNOTATION"];
		var $component = $("<select>", { "id" : 'colorEncodedWellAnnotations-' + well.permId, class : 'form-control', 'permId' : well.permId, "identifier" : well.identifier });
		if(this._plateModel.isDisabled) {
			$component.attr('disabled', '');
		}
		
		var $defaultOption = $("<option>").attr('value', '').attr('disabled', '').text('Select an annotation');
		if(!selectedAnnotation) {
			$defaultOption.attr('selected', '');
		}
		$component.append($defaultOption);
			
		var colorEncodedAnnotations = profile.getVocabularyByCode("$WELL.COLOR_ENCODED_ANNOTATIONS");
		for(var cIdx = 0; cIdx < colorEncodedAnnotations.terms.length; cIdx++) {
			var $option = $("<option>").attr('value', colorEncodedAnnotations.terms[cIdx].code).text(colorEncodedAnnotations.terms[cIdx].label);
			if(selectedAnnotation === colorEncodedAnnotations.terms[cIdx].code) {
				$option.attr('selected', '');
			}
			$component.append($option);
		}
		
		var _this = this;
		$cell.attr("id", "well-" + well.permId);
		$component.change(function(event) {
			var $componentChange = $(this);
			var value = $componentChange.val();
			var permId = $componentChange.attr("permId");
			var identifier = $componentChange.attr("identifier");
			if(value === "DEFAULT") {
				$("#well-"+permId).css( { "background-color" : "" } );
			} else {
				$("#well-"+permId).css( { "background-color" : _this._getColorForAnnotationCode(value) } );
			}
			_this._plateModel.changesToDo.push({ "permId" : permId, "identifier" : identifier, "properties" : {"$WELL.COLOR_ENCODED_ANNOTATION" : value } });
			if(mainController.currentView.setDirty) {
				mainController.currentView.setDirty();
			}
		});
		var $componentWithLabel = $("<span>").append("Color Encoded Annotation: ").append($component);
		Select2Manager.add($component);
		return $componentWithLabel;
	}
}