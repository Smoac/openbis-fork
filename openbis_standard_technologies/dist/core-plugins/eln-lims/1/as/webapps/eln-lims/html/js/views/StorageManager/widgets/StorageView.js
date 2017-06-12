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

function StorageView(storageController, storageModel, gridViewRack, gridViewPosition) {
	this._storageController = storageController;
	this._storageModel = storageModel;
	this._gridViewRack = gridViewRack;
	this._gridViewPosition = gridViewPosition;
	
	this._storageGroupsDropDown = FormUtil.getStoragePropertyGroupsDropdown("", true);
	this._defaultStoragesDropDown = null;
	this._userIdDropdown = $('<select>', { 'id' : 'userIdSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
	this._gridContainer = $("<div>");
	this._boxField = FormUtil._getInputField("text", "", "Box Name", null, false);
	this._boxSizeDropDown = FormUtil.getDefaultStorageBoxSizesDropDown("", false);
	this._positionContainer = $("<div>");
	
	this.repaint = function($container) {
		var _this = this;
		
		FormUtil.getDefaultStoragesDropDown("", false, function($storagesDropdownComponent) {
			_this._defaultStoragesDropDown = $storagesDropdownComponent;
			
			//
			// Paint View
			//
			
			//$container.empty(); To allow display into a pop-up
			if( _this._storageModel.config.title) { //It can be null
				$container.append("<h2>" + _this._storageModel.config.title + "</h2>");
			}
			
			if( _this._storageModel.config.storagePropertyGroupSelector === "on") {
				//Paint
				var $controlGroupStoragesGroups = FormUtil.getFieldForComponentWithLabel(_this._storageGroupsDropDown, "Group");
				$container.append($controlGroupStoragesGroups);
				_this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup(_this._storageGroupsDropDown.val());
				//Attach Event
				_this._storageGroupsDropDown.change(function(event) {
					_this._storageController.setSelectStorageGroup($(this).val());
				});
			}
			
			if(_this._storageModel.config.storageSelector === "on") {
				//Sample to bind
				if(_this._storageModel.sample) {
					_this._defaultStoragesDropDown.val(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.nameProperty]);
				}
				//Paint
				var $controlGroupStorages = FormUtil.getFieldForComponentWithLabel(_this._defaultStoragesDropDown, "Storage");
				$container.append($controlGroupStorages);
				//Attach Event
				_this._defaultStoragesDropDown.change(function(event) {
					var storageName = $(this).val();
					if(_this._storageModel.sample) { //Sample to bind
						_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.nameProperty] = storageName;
					}
					_this._storageController.setSelectStorage(storageName);
					
					if(storageName === "") {
						_this._storageModel.cleanSample(false);
					} else {
						_this._storageModel.cleanSample(true);
					}
				});
			}
			
			if(_this._storageModel.config.userSelector === "on" && !_this._storageModel.sample) {
				//Paint
				var $controlGroupUserId = FormUtil.getFieldForComponentWithLabel(_this._userIdDropdown, "User Id Filter");
				$container.append($controlGroupUserId);
				_this._userIdDropdown.multiselect();
				//Attach Event
				_this._userIdDropdown.change(function() {
					var selectedUserIds = $(this).val();
					_this._storageController.setUserIdsSelected(selectedUserIds);
				});
			} else if(_this._storageModel.sample) { //If someone is updating a sample, his user should go with it
				_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
			}
			
			$container.append(FormUtil.getFieldForComponentWithLabel(_this._gridContainer, "Rack"));
			if(_this._storageModel.sample) {
				_this._storageController.setSelectStorage(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.nameProperty]);
			}
			
			if(_this._storageModel.config.boxSelector === "on" || _this._storageModel.config.rackSelector === "on") {
				//Paint
				_this._boxField.hide();
				var $controlGroupBox = FormUtil.getFieldForComponentWithLabel(_this._boxField, "Box Name");
				$container.append($controlGroupBox);
				//Attach Event
				_this._boxField.keyup(function() {
					if(_this._storageModel.sample) { // Sample to bind
						_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = $(this).val();
					}
					_this._storageController.setBoxSelected($(this).val());
				});
				// Sample to bind
				if(_this._storageModel.sample && _this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty]) {
					_this._boxField.show();
					_this._boxField.val(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty]);
					_this._storageController.setBoxSelected(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty]);
					_this._boxField.attr("disabled", "");
				}
			}
			
			if(_this._storageModel.config.boxSizeSelector === "on") {
				//Paint
				_this._boxSizeDropDown.hide();
				var $controlGroupBox = FormUtil.getFieldForComponentWithLabel(_this._boxSizeDropDown, "Box Size");
				$container.append($controlGroupBox);
				//Attach Event
				_this._boxSizeDropDown.change(function() {
					if(_this._storageModel.sample) { // Sample to bind
						_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty] = $(this).val();
					}
					_this._storageController.setBoxSizeSelected($(this).val(), true);
				});
				// Sample to bind
				if(_this._storageModel.sample && _this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty]) {
					_this._boxSizeDropDown.show();
					_this._boxSizeDropDown.val(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty]);
					_this._storageController.setBoxSizeSelected(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty], false);
					this._boxSizeDropDown.attr("disabled", "");
				}
			}
			
			if(_this._storageModel.config.positionSelector === "on") {
				$container.append(FormUtil.getFieldForComponentWithLabel(_this._positionContainer, "Box Position"));
			}
			if(_this._storageModel.config.positionSelector === "on" && _this._storageModel.sample) {
				_this.showPosField(this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxSizeProperty], false);
			}
			
			if(_this._storageModel.isDisabled) {
				_this._storageGroupsDropDown.attr("disabled", "");
				_this._defaultStoragesDropDown.attr("disabled", "");
				_this._userIdDropdown.attr("disabled", "");
				_this._boxField.attr("disabled", "");
			}
			
			_this._storageController.initFinish();
		});
		
		
	}
	
	this.refreshUserIdContents = function() {
		this._userIdDropdown.empty();
		var contents = this._storageModel.userIds;
		if(contents) {
			for (var i = 0; i < contents.length; i++) {
				this._userIdDropdown.append($('<option>', { 'value' : contents[i] , 'selected' : ''}).html(contents[i]));
			}
		} 
		this._userIdDropdown.multiselect('rebuild');
	}
	
	//
	// View specific methods
	//
	this.isNewBoxName = function() {
		return !this._boxField.prop('disabled');
	}
	
	this.resetSelectStorageDropdown = function() {
		this._defaultStoragesDropDown.val("");
	}
	
	this.refreshGrid = function() {
		this._gridViewRack.repaint(this._gridContainer);
		
		if(this._storageModel.sample && 
				this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.rowProperty] &&
				this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.columnProperty]) {
			this._storageController._gridController.selectPosition(
					this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.rowProperty],
					this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.columnProperty]);
			this._storageModel.row = this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.rowProperty];
			this._storageModel.column = this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.columnProperty];
		}
	}
	
	this.hideBoxSizeField = function() {
		this._boxSizeDropDown.val("");
		this._boxSizeDropDown.hide();
	}
	
	this.showBoxSizeField = function() {
		this._boxSizeDropDown.val("");
		this._boxSizeDropDown.removeAttr("disabled");
		this._boxSizeDropDown.show();
	}
	
	this.hidePosField = function() {
		this._positionContainer.empty();
		this._positionContainer.append($("<p>").append("Select a box to see his contents."));
	}
	
	this.showPosField = function(boxSizeCode, isNew) {
		if(this._storageModel.config.positionSelector === "on") {
			//Pointer to himself
			var _this = this;
			
			var propertyTypeCodes = [this._storageModel.storagePropertyGroup.boxProperty];
			var propertyValues = [this._storageModel.boxName];
			mainController.serverFacade.searchWithProperties(propertyTypeCodes, propertyValues, function(samples) {
				//Labels
				var labels = [];
				samples.forEach(function(element, index, array) {
					var displayName = null;
					var name = element.properties[profile.propertyReplacingCode];
					if(name) {
						displayName = element.code +"(" + name + ")";
					} else {
						displayName = element.code;
					}
					
					var positionProp  = element.properties[_this._storageModel.storagePropertyGroup.positionProperty];
					if(positionProp) {
						var positions = positionProp.split(" ");
						for(var pIdx = 0; pIdx < positions.length; pIdx++) {
							var position = positions[pIdx];
							if (position) {
								var xyPos = Util.getXYfromLetterNumberCombination(position);
								var x = xyPos[0];
								var y = xyPos[1];
								
								var row = labels[x];
								if(!row) {
									row = [];
									labels[x] = row;
								}
								
								var col = row[y];
								if(!col) {
									col = [];
									row[y] = col;
								}
								
								if(_this._storageModel.sample && element.permId === _this._storageModel.sample.permId) {
									continue;
								} else {
									label = { displayName : displayName, data : element };
									col.push(label);
								}
							}
						}
					} else {
						//Not position found
					}
				});
				
				//Repaint
				if(boxSizeCode) {
					_this._storageController._gridControllerPosition.getModel().useLettersOnRows = true;
					var rowsAndCols = boxSizeCode.split("X");
					var numRows = parseInt(rowsAndCols[0]);
					var numCols = parseInt(rowsAndCols[1]);
					_this._storageController._gridControllerPosition.getModel().reset(numRows, numCols, labels);
					_this._storageController._gridControllerPosition.getView().setPosSelectedEventHandler(function(posX, posY, isSelectedOrDeleted) {
						var newPosition = Util.getLetterForNumber(posX) + posY;
						var isMultiple = _this._storageController._gridControllerPosition.getModel().isSelectMultiple;
						var boxPosition = _this._storageModel.boxPosition;
						
						if(!boxPosition || !isMultiple) {
							boxPosition = "";
						}
						
						//Add delete position, takes in count some non standard inputs that can be done in batch registration/update
						if(isMultiple && !isSelectedOrDeleted) {
							boxPosition = boxPosition.replace(newPosition, '');
						} else {
							boxPosition += " " + newPosition;
						}
						boxPosition = boxPosition.replace(/  +/g, ' ');
						boxPosition = boxPosition.trim();
						//
						
						//Binded sample
						if(_this._storageModel.sample) {
							_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = boxPosition;
						}
						_this._storageModel.boxPosition = boxPosition;
					}); 
					
					//
					// Box low of space alert
					//
					var positionsUsed = samples.length;
					var totalPositions = numRows * numCols;
					var used = positionsUsed / totalPositions;
					if(used >= profile.storagesConfiguration["boxSpaceLowWarning"]) {
						Util.showInfo("Box space is getting low, currently " + positionsUsed + " out of " + totalPositions + " posible positions are taken.", function() {}, true);
					}
				}
				
				_this._storageController._gridControllerPosition.init(_this._positionContainer);
				
				if(isNew) {
					//Binded sample
					if(_this._storageModel.sample) {
						_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = null;
					}
					_this._storageModel.boxPosition = null;
				} else {
					if(_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty]) {
						_this._storageModel.boxPosition = _this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty];
						var positions = _this._storageModel.boxPosition.split(" ");
						for(var pIdx = 0; pIdx < positions.length; pIdx++) {
							var position = positions[pIdx];
							if(position) {
								var xyPos = Util.getXYfromLetterNumberCombination(position);
								_this._storageController._gridControllerPosition.selectPosition(xyPos[0], xyPos[1]);
							}
						}
					}
				}
			}, null, true);
		}
	}
	
	this.hideBoxField = function() {
		this._boxField.val("");
		this._boxField.hide();
	}
	
	this.showBoxField = function() {
		this._boxField.val("");
		this._boxField.removeAttr("disabled");
		this._boxField.show();
	}
	
	this.showBoxName = function() {
		this._boxField.val(this._storageModel.boxName);
		this._boxField.attr("disabled", "");
		this._boxField.show();
	}
	
	this.showBoxSize = function() {
		this._boxSizeDropDown.val(this._storageModel.boxSize);
		this._boxSizeDropDown.attr("disabled", "");
		this._boxSizeDropDown.show();
	}
	
}