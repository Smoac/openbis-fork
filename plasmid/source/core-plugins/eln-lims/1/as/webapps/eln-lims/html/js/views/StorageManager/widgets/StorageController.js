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

function StorageController(configOverride) {
	if(!profile.storagesConfiguration["isBoxSizeAndPositionEnabled"]) {
		Util.showError("Your Storage Model don't works properly with the lattest ELN version, contact your admin.");
		return;
	}
	//Pointer to himself
	var _this = this;
	//Dependent widgets
	this._gridController = new GridController();
	this._gridControllerPosition = new GridController();
	
	//This controller M/V
	this._storageModel = new StorageModel(configOverride);
	this._storageView = new StorageView(this, this._storageModel, this._gridController.getView());
	
	if(this._storageModel.config.boxSelector === "on") {
		this._gridController.getView().setLabelSelectedEventHandler(function(posX, posY, label, data) {
			//Binded sample
			if(_this._storageModel.sample) {
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.rowProperty] = posX;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.columnProperty] = posY;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = label;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty] = data.size;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = null;
			}
			
			
			// Delete old state in model and view and set new sate in model and view
			_this._storageModel.resetBoxInfo(posX, posY, label, data.size, null);
			_this._storageView.showBoxName();
			_this._storageView.showBoxSize();
			_this._storageView.showPosField(data.size, true);
			
			if(_this._storageModel.config.contentsSelector === "on") {
				var labelData = _this._gridController.getModel().getLabelDataByLabelName(posX, posY, label);
				_this._storageModel.boxContents = labelData.samples;
				_this._storageView.refreshBoxContents();
			}
		}); 
	}
	
	if(this._storageModel.config.rackSelector === "on") {
		this._gridController.getView().setPosSelectedEventHandler(function(posX, posY) {
			//Binded sample
			if(_this._storageModel.sample) {
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.rowProperty] = posX;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.columnProperty] = posY;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = null;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty] = null;
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = null;
			}
			// Delete old state in model and view and set new sate in model and view
			_this._storageModel.resetBoxInfo(posX, posY, null, null, null);
			_this._storageView.showBoxField();
			_this._storageView.showBoxSizeField();
			_this._storageView.hidePosField();
			if(_this._storageModel.config.contentsSelector === "on") {
				_this._storageView.refreshBoxContents();
			}
		}); 
	}
	
	this.setBoxContentsSelected = function(selectedSamples) {
		this._storageModel.boxContents = selectedSamples;
	}

	this.setUserIdsSelected = function(userIdsSelected) {
		this._storageModel.userIdsSelected = userIdsSelected;
		this._gridController.getModel().labelsFilter = function(posX, posY, sortedLabels) {
			var sortedLabelsToReturn = [];
			for(var i = 0; i < sortedLabels.length; i++) {
				var labelSamples = _this._gridController.getModel().getLabelDataByLabelName(posX, posY, sortedLabels[i].displayName).samples;
				var labelSamplesSelected = [];
				for(var j = 0; j < labelSamples.length; j++) {
					var labelSample = labelSamples[j];
					var sampleUserId = labelSample.properties[_this._storageModel.storagePropertyGroup.userProperty];
					if($.inArray(sampleUserId, userIdsSelected) !== -1) {
						labelSamplesSelected.push(labelSample);
					}
				}
				if(labelSamplesSelected.length !== 0) {
					sortedLabelsToReturn.push(sortedLabels[i]);
				}
			}
			return sortedLabelsToReturn;
		}
		
		this._gridController.getModel().dataFilter = function(posX, posY, labelData) {
			var labelSamplesSelected = [];
			for(var j = 0; j < labelData.samples.length; j++) {
				var labelSample = labelData.samples[j];
				var sampleUserId = labelSample.properties[_this._storageModel.storagePropertyGroup.userProperty];
				if($.inArray(sampleUserId, userIdsSelected) !== -1) {
					labelSamplesSelected.push(labelSample);
				}
			}
			return { size : labelData.size , samples : labelSamplesSelected}; //Create new data object with selected samples
		}
		
		this._storageModel.resetBoxInfo(null, null, null, null, null);
		this._storageView.refreshGrid();
		this._storageView.hideBoxField();
		this._storageView.hideBoxSizeField();
		this._storageView.hidePosField();
		this._storageView.refreshBoxContents();
	}
	
	this.setUserIds = function(userIds) {
		this._storageModel.userIds = userIds;
		this._storageView.refreshUserIdContents();
	}
	
	this.setBoxSelected = function(boxName) {
		this._storageModel.boxName = boxName;
	}
	
	this.setBoxSizeSelected = function(boxSize, isNew) {
		this._storageModel.boxSize = boxSize;
		if(isNew) {
			this._storageView.showPosField(boxSize, isNew);
		}
	}
	
	this._deleteRackBoxContentStateInModelView = function() {
		// Delete old state in model and view and set new sate in model and view
		this._storageModel.resetBoxInfo(null, null, null, null, null);
		this._storageView.hideBoxField();
		this._storageView.hideBoxSizeField();
		this._storageView.hidePosField();
		if(this._storageModel.config.contentsSelector === "on") {
			this._storageView.refreshBoxContents();
		}
	}
	
	this.setSelectStorageGroup = function(storageGroupName) {
		//Delete old state
		this._deleteRackBoxContentStateInModelView();
		this._storageView.resetSelectStorageDropdown();
		this._gridController.getModel().reset();
		
		//Set new state
		this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup(storageGroupName);
		this._storageView.refreshGrid();
	}
	
	this.setSelectStorage = function(selectedStorageCode) {
		//
		// Delete old state in model and view
		//
		this._deleteRackBoxContentStateInModelView();
		
		//
		// Obtain Storage Configuration
		//
		this._storageModel.storageCode = selectedStorageCode;
		var storageConfig = profile.getStorageConfiguation(selectedStorageCode);
		
		if(storageConfig) {
			this._gridController.getModel().reset(storageConfig.rowNum, storageConfig.colNum);
		} else {
			this._gridController.getModel().reset(null, null);
		}
		
		//
		// Obtain Storage Boxes
		//
		var propertyTypeCodes = [_this._storageModel.storagePropertyGroup.nameProperty];
		var propertyValues = ["'" + selectedStorageCode + "'"];
		
		mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues,
				function(samples) {
					var boxes = [];
					var userIds = [];
					samples.forEach(function(element, index, array) {
						var boxCode = element.properties[_this._storageModel.storagePropertyGroup.boxProperty];
						var boxSize = element.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty];
						var boxRow  = element.properties[_this._storageModel.storagePropertyGroup.rowProperty];
						var boxCol  = element.properties[_this._storageModel.storagePropertyGroup.columnProperty];
						var userId = element.properties[_this._storageModel.storagePropertyGroup.userProperty];
						
						if($.inArray(userId, userIds) === -1) {
							userIds.push(userId);
						}
						
						var boxesRow = boxes[boxRow];
						if(!boxesRow) {
							boxesRow = [];
							boxes[boxRow] = boxesRow;
						}
						
						var boxesCol = boxesRow[boxCol];
						if(!boxesCol) {
							boxesCol = [];
							boxesRow[boxCol] = boxesCol;
						}
						
						var getBoxFromCol = function(col, boxName) {
							for(var i = 0; i < col.length; i++) {
								box = col[i];
								if(box.displayName = boxName) {
									return box;
								}
							}
							return null;
						}
						
						var boxSamples = getBoxFromCol(boxesCol, boxCode);
						if(!boxSamples) {
							boxSamples = { displayName : boxCode, data : { size: boxSize, samples: [] } };
						} else if(!boxSamples.data.size) { //To help instances where they are migrating data where not all boxes are set
								boxSamples.data.size = boxSize;
						}
						boxSamples.data.samples.push(element);
						
						boxesCol.push(boxSamples);
					}, true);
					
					//
					// Refresh Grid with the boxes
					//
					_this.setUserIds(userIds);
					_this._gridController.getModel().labels = boxes;
					_this._storageView.refreshGrid();
		});
	}
	
	//
	// Set the sample to bind before painting the view
	//
	this.bindSample = function(sample, isDisabled) {
		this._storageModel.sample = sample;
		this._storageModel.isDisabled = isDisabled;
		this._gridController.getModel().isDisabled = isDisabled;
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageModel;
	}
	
	this.getView = function() {
		return this._storageView;
	}
	
	//
	// Validation
	//
	this.isValid = function(callback) {
		var _this = this;
		this._isUserTypingExistingBox(function(error1) {
			if(error1) {
				Util.showError(error1, function() {}, true);
				callback(false);
			} else {
				_this._isPositionAlreadyUsed(function(error2) {
					if(error2) {
						Util.showError(error2, function() {}, true);
						callback(false);
					} else {
						callback(true);
					}
				});
			}
		});
	}
	
	this._isPositionAlreadyUsed = function(callback) {
		var _this = this;
		// Check user don't selects a position already selected by a sample that is not the binded one
		// ERROR: You selected a position already used by <SAMPLE_CODE>, please choose another.
		var propertyTypeCodes = [this._storageModel.storagePropertyGroup.boxProperty, this._storageModel.storagePropertyGroup.positionProperty];
		var propertyValues = ["'" + this._storageModel.boxName + "'", "'" + this._storageModel.boxPosition + "'"];
		mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues, function(samples) {
			var sampleCodes = [];
			var isBinded = false;
			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
				sampleCodes.push(samples[sIdx].code);
				if(_this._storageModel.sample) {
					isBinded = isBinded || (samples[sIdx].code === _this._storageModel.sample.code);
				}
			}
			if(samples.length > 1) { //More than one sample in that position
				callback("There is more than one sample in that position, exactly " + sampleCodes + ", weird?, contact your administrator.");
			} else if(samples.length > 0 && !_this._storageModel.sample) { //Sample in that position
				callback("You selected a position already used by " + sampleCodes + ", please choose another.");
			} else if(samples.length === 1 && _this._storageModel.sample && !isBinded) {
				callback("You selected a position already used by " + sampleCodes + ", please choose another.");
			} else if(samples.length === 1 && _this._storageModel.sample && isBinded) {
				callback(null);
			} else if(samples.length === 0) {
				callback(null);
			}
		});
	}
	
	this._isUserTypingExistingBox = function(callback) {
		var _this = this;
		// Check user don't types by hand an existing box
		// ERROR: You typed by hand an already exiting box <BOX_CODE>, please click on it to auto fill correct size and available positions.
		if(this._storageView.isNewBoxName()) {
			var propertyTypeCodes = [this._storageModel.storagePropertyGroup.boxProperty];
			var propertyValues = ["'" + this._storageModel.boxName + "'"];
			mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues, function(samples) {
				if(samples.length > 0) { //Box already exists with same name
					callback("You typed by hand an already exiting box '" + _this._storageModel.boxName + "', please click on it to auto fill correct size and available positions or choose other box name.");
				} else {
					callback(null);
				}
			});
		} else {
			callback(null);
		}
	}
}