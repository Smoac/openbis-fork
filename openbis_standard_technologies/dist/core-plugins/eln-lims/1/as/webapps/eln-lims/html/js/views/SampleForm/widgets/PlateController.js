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

function PlateController(sample, isDisabled) {
	this._plateModel = new PlateModel(sample, isDisabled);
	this._plateView = new PlateView(this, this._plateModel);
	
	this.getPlaceHolderId = function() {
		return this._plateModel.getPlaceHolderId();
	}
	
	this.getPlaceHolder = function() {
		return this._plateView.getPlaceHolder();
	}
	
	this.initWithPlaceHolder = function() {
		var _this = this;
		var repeatUntilSet = function() {
			var placeHolderFound = $("#" + _this._plateModel.getPlaceHolderId());
			if(placeHolderFound.length === 0) {
				setTimeout(repeatUntilSet, 100);
			} else {
				_this.init(placeHolderFound);
			}
		}
		repeatUntilSet();
	}
	
	this._getMaterialFromCode = function(materials, code) {
		for(var mIdx = 0; mIdx < materials.length; mIdx++) {
			if(materials[mIdx].materialCode === code) {
				return materials[mIdx];
			}
		}
		return null;
	}
	
	this._getMaterialTypeFromPropertyValue = function(propertyValue) {
		var materialIdentifierParts = propertyValue.split(" ");
		var materialType = materialIdentifierParts[1].substring(1, materialIdentifierParts[1].length-1);
		return materialType;
	}
	
	this._getMaterialCodeFromPropertyValue = function(propertyValue) {
		return propertyValue.split(" ")[0];
	}
	
	this._getMaterialIdentifierFromPropertyValue = function(propertyValue) {
		var materialIdentifierParts = propertyValue.split(" ");
		var materialType = materialIdentifierParts[1].substring(1, materialIdentifierParts[1].length - 1);
		var materialIdentifier = IdentifierUtil.getMaterialIdentifier(materialType, materialIdentifierParts[0]);
		return materialIdentifier;
	}
	
	this.init = function($container) {
		var _this = this;
		$container.prepend("Loading Wells ...");
		if(this._plateModel.sample.contained) {
			this._plateView.repaint($container);
		} else {
			mainController.serverFacade.searchContained(this._plateModel.sample.permId, function(contained) {
				//GENES - Used by Nexus
				var materialIdentifiers = [];
				for(var i = 0; i < contained.length; i++) {
					if(contained[i].properties) {
						var genePropertyValue = contained[i].properties["GENE"];
						if(genePropertyValue) {
							var genePropertyIdentifier = _this._getMaterialIdentifierFromPropertyValue(genePropertyValue);
							if(($.inArray(genePropertyIdentifier, materialIdentifiers) === -1)) {
								materialIdentifiers.push(genePropertyIdentifier);
							}
						}
					}
				}
				
				//Get Materials
				mainController.serverFacade.getMaterialsForIdentifiers(materialIdentifiers, function(materials) {
					if(materials.result) {
						for(var i = 0; i < contained.length; i++) {
							if(contained[i].properties["GENE"]) {
								if(contained[i].properties && contained[i].properties["GENE"]) {
									var geneCode = _this._getMaterialCodeFromPropertyValue(contained[i].properties["GENE"]);
									var gene = _this._getMaterialFromCode(materials.result, geneCode);
									if(gene) {
										contained[i].cachedMaterials = [gene];
									}
								}
							}
						}
					}
					
					_this._plateModel.sample.contained = contained;
					
					//Get Feature Vector Datasets
					mainController.serverFacade.customELNApi({
						"method" : "listFeatureVectorDatasetsPermIds",
						"samplePlatePermId" : _this._plateModel.sample.permId
					}, function(error, result){
						if(error) {
							Util.showError(error);
							_this._plateView.repaint($container);
						} else {
							_this._plateModel.sample.featureVectorsCache.featureVectorDatasets = result.data;
							
							//Finally paint the view
							_this._plateView.repaint($container);
						}
					});
					
				});
			});
		}
	}
	
	this.getChangesToDo = function() {
		return this._plateModel.changesToDo;
	}
}