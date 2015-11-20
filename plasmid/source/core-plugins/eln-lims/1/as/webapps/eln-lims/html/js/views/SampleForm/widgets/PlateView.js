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
	this._$gridTable = null;
	this._$scale = $("<span>");
	
	this.getPlaceHolder = function() {
		var container = $("<div>", { "id" : this._plateModel.getPlaceHolderId() });
		var gridTable = this.getGridTable(false);
		container.append(gridTable);
		return container[0].outerHTML
	}
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		//Paint Toolbar
		var $toolbar = $("<div>");
		if( this._plateModel.isDisabled &&
			this._plateModel.sample.featureVectorsCache &&
			this._plateModel.sample.featureVectorsCache.featureVectorDatasets) {
			
			//1. Selected Feature Vector Dataset Features
			this._$featureVectorDatasetFeaturesDropdown = FormUtil.getDropDownForTerms(
					"featureVectorDatasetFeaturesDropdown-" + this._plateModel.sample.permId,
					[],
					"Choose a Feature Vector Dataset first",
					false
					);
			this._$featureVectorDatasetFeaturesDropdown.addClass("featureDropdown");
			this._$featureVectorDatasetFeaturesDropdown.change(function(event) {
				var selectedFeature = $(this).val();
				if(selectedFeature) {
					var featureVectorDatasetCode = _this._$featureVectorDatasetsDropdown.val();
					
					if(_this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode]) {
						_this._repaintGridToFeatureVectorColors(featureVectorDatasetCode, selectedFeature);
					} else {
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
							_this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode] = result.data[0]
							_this._repaintGridToFeatureVectorColors(featureVectorDatasetCode, selectedFeature);
						});
					}
				} else {
					_this._repaintGridToAnnotationsColors();
				}
			});
			
			//2. Feature Vector Dataset Dropdow
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
												"Choose a Feature Vector Dataset please",
												false
												);
			this._$featureVectorDatasetsDropdown.addClass("featureDropdown");
			
			this._$featureVectorDatasetsDropdown.change(function(event) {
				var featureVectorDatasetCode = $(this).val();
				if(!featureVectorDatasetCode) {
					_this._$featureVectorDatasetFeaturesDropdown.empty();
					_this._$featureVectorDatasetFeaturesDropdown.append($("<option>").attr('value', '').text("Choose a Feature Vector Dataset first"));
					_this._repaintGridToAnnotationsColors();
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
						});
					} else {
						loadFeatureVectorDatasetFeatures();
					}
				}
			});
			
			//Build Toolbar
			$toolbar.append(this._$featureVectorDatasetsDropdown)
					.append(this._$featureVectorDatasetFeaturesDropdown);
		}
		
		//Paint grid
		this._$gridTable = this.getGridTable(true);
		$container.append($toolbar).append(this._$gridTable).append(this._$scale);
		
		//Painting default colors after populating the table
		this._repaintGridToAnnotationsColors();
	}
	
	this.getGridTable = function(withWells) {
		var $gridTable = $("<table>", { "class" : "table table-bordered gridTable", "style" : "table-layout: fixed;" });
		
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
					$cell.addClass('well');
					if(withWells) {
						var well = this._plateModel.getWell(i-1,j);
						if(well) {
							$cell.attr("id", "well-" + well.permId);
							
							//Color Annotations only enabled for wells with a type having COLOR_ENCODED_ANNOTATION property
							var isAnnotableWell = jQuery.inArray("COLOR_ENCODED_ANNOTATION", profile.getAllPropertiCodesForTypeCode(well.sampleTypeCode) ) !== -1;
							var colorEncodedWellAnnotationsSelector = "";
							
							if(isAnnotableWell) {
								colorEncodedWellAnnotationsSelector = this._getWellColorAnnotationGroups(well);
							}
							
							var tooltip = PrintUtil.getTable(well, false, null, 'inspectorWhiteFont',
															'colorEncodedWellAnnotations-holder-' + well.permId,
															colorEncodedWellAnnotationsSelector);
							
							$cell.tooltipster({
								content: $(tooltip),
								interactive: true
							});
						}
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
	this._getCell = function(row, column) {
		var $grid = this._$gridTable;
		var $gridRow = $(this._$gridTable.children().children()[row]);
		var $gridColumn = $($gridRow.children()[column]);
		return $gridColumn;
	}
	
	this._repaintWellToColor = function(row, column, rgbColor) {
		var $cell = this._getCell(row, column);
			$cell.css( { "background-color" : rgbColor });
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
	// Utility methods to handle feature vectors
	//
	this._repaintGridToFeatureVectorColors = function(featureVectorDatasetCode, featureCode) {
		//1. Obtain feature Index
		var featuresData = this._plateModel.sample.featureVectorsCache.featureVectorDatasetsFeaturesData[featureVectorDatasetCode];
		var featureIndex = null;
		
		for(var fIdx = 0; fIdx < featuresData.featureCodes.length; fIdx++) {
			if(featuresData.featureCodes[fIdx] === featureCode) {
				featureIndex = fIdx;
				break;
			}
		}
		
		//2. Define Color Step
		var minValue = null;
		var maxValue = null;
		
		for(var wellIdx = 0; wellIdx < featuresData.featureVectors.length; wellIdx++) {
			var wellData = featuresData.featureVectors[wellIdx];
			if(!wellData.vocabularyFeatureFlags[featureIndex]) { //Don't support vocabularies for now
				var value = wellData.values[featureIndex];
				
				if(!minValue || value < minValue) {
					minValue = value;
				}
				
				if(!maxValue || value > maxValue) {
					maxValue = value;
				}
			}
		}
		
		var colorStepSize = (maxValue - minValue) / this._plateModel.numHeatmapColors;
		
		//3. Clean Colors
		this._cleanGrid();
		
		//4. Paint Colors
		for(var wellIdx = 0; wellIdx < featuresData.featureVectors.length; wellIdx++) {
			var wellData = featuresData.featureVectors[wellIdx];
			var wellRow = wellData.wellPosition.wellRow;
			var wellColumn = wellData.wellPosition.wellColumn;
			if(!wellData.vocabularyFeatureFlags[featureIndex]) { //Don't support vocabularies for now
				if(wellData.values[featureIndex] !== "NaN") {
					var value = wellData.values[featureIndex];
					var valueColorStep = Math.round(value / colorStepSize);
					var color = this._getColorForStepBetweenWhiteAndBlack(valueColorStep, this._plateModel.numHeatmapColors);
					this._repaintWellToColor(wellRow, wellColumn, color);
				}
			}
		}
		
		//5. Paint Scale
		this._repaintScale(colorStepSize, this._plateModel.numHeatmapColors);
	}
	
	this._repaintScale = function(colorStepSize, numSteps) {
		var $scaleTable = $("<table>", { "class" : "table table-bordered", "style" : "table-layout: fixed;" });
		for(var i = 1; i <= numSteps; i++) {
			var $row = $("<tr>")
				.append($("<td>", { "style" : "background-color:" + this._getColorForStepBetweenWhiteAndBlack(i, numSteps) + ";" }))
				.append($("<td>").append(colorStepSize*i));
			$scaleTable.append($row);
		}
		
		this._$scale.empty();
		this._$scale.append($("<legend>").append("Scale"));
		this._$scale.append($scaleTable);
	}
	
	this._getColorForStepBetweenWhiteAndBlack = function(step, numSteps) {
		var stepSize = Math.round(255/numSteps);
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
		var well = this._plateModel.getWell(row-1,column);
		if(well) {
			var isAnnotableWell = jQuery.inArray("COLOR_ENCODED_ANNOTATION", profile.getAllPropertiCodesForTypeCode(well.sampleTypeCode) ) !== -1;
			if(isAnnotableWell) {
				var selectedColorEncodedAnnotation = well.properties["COLOR_ENCODED_ANNOTATION"];
				if(selectedColorEncodedAnnotation && selectedColorEncodedAnnotation !== "DEFAULT") {
					this._repaintWellToColor(row, column, this._getColorForAnnotationCode(selectedColorEncodedAnnotation));
				}
			}
		}
	}
	
	this._getColorForAnnotationCode = function(code) {
		var valueInfo = profile.getVocabularyTermByCodes("COLOR_ENCODED_ANNOTATIONS", code);
		if(valueInfo) {
			return valueInfo.description.split(":")[0].trim();
		}
		return null;
	}
	
	this._getWellColorAnnotationGroups = function(well) {
		var selectedAnnotation = well.properties["COLOR_ENCODED_ANNOTATION"];
		var $component = $("<select>", { "id" : 'colorEncodedWellAnnotations-' + well.permId, class : 'form-control', 'permId' : well.permId, "identifier" : well.identifier });
		if(this._plateModel.isDisabled) {
			$component.attr('disabled', '');
		}
		
		var $defaultOption = $("<option>").attr('value', '').attr('disabled', '').text('Select an annotation');
		if(!selectedAnnotation) {
			$defaultOption.attr('selected', '');
		}
		$component.append($defaultOption);
			
		var colorEncodedAnnotations = profile.getVocabularyByCode("COLOR_ENCODED_ANNOTATIONS");
		for(var cIdx = 0; cIdx < colorEncodedAnnotations.terms.length; cIdx++) {
			var $option = $("<option>").attr('value', colorEncodedAnnotations.terms[cIdx].code).text(colorEncodedAnnotations.terms[cIdx].label);
			if(selectedAnnotation === colorEncodedAnnotations.terms[cIdx].code) {
				$option.attr('selected', '');
			}
			$component.append($option);
		}
		
		var _this = this;
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
			_this._plateModel.changesToDo.push({ "permId" : permId, "identifier" : identifier, "properties" : {"COLOR_ENCODED_ANNOTATION" : value } });
			if(mainController.currentView.setDirty) {
				mainController.currentView.setDirty();
			}
		});
		var $componentWithLabel = $("<span>").append("Color Encoded Annotation: ").append($component);
		return $componentWithLabel;
	}
}