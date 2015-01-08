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

function StorageView(storageController, storageModel, gridView) {
	this._storageController = storageController;
	this._storageModel = storageModel;
	this._gridView = gridView;
	
	this._storageGroupsDropDown = FormUtil.getStoragePropertyGroupsDropdown("", true);
	this._defaultStoragesDropDown = FormUtil.getDefaultStoragesDropDown("", false);
	this._userIdDropdown = $('<select>', { 'id' : 'userIdSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
	this._gridContainer = $("<div>");
	this._boxField = FormUtil._getInputField("text", "", "Box Name", null, false);
	this._boxContentsDropDown = $('<select>', { 'id' : 'boxSamplesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
	this._positionField = FormUtil._getInputField("text", "", "Position", null, false);
	
	this.repaint = function($container) {
		//
		// Paint View
		//
		var _this = this;
		//$container.empty(); To allow display into a pop-up
		if( this._storageModel.config.title) { //It can be null
			$container.append("<h2>" + this._storageModel.config.title + "</h2>");
		}
		
		if(this._storageModel.config.storagePropertyGroupSelector === "on") {
			//Paint
			var $controlGroupStoragesGroups = FormUtil.getFieldForComponentWithLabel(this._storageGroupsDropDown, "Group");
			$container.append($controlGroupStoragesGroups);
			this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup(this._storageGroupsDropDown.val());
			//Attach Event
			this._storageGroupsDropDown.change(function(event) {
				_this._storageController.setSelectStorageGroup($(this).val());
			});
		}
		
		if(this._storageModel.config.storageSelector === "on") {
			//Sample to bind
			if(this._storageModel.sample) {
				this._defaultStoragesDropDown.val(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.nameProperty]);
			}
			//Paint
			var $controlGroupStorages = FormUtil.getFieldForComponentWithLabel(this._defaultStoragesDropDown, "Storage");
			$container.append($controlGroupStorages);
			//Attach Event
			this._defaultStoragesDropDown.change(function(event) {
				if(_this._storageModel.sample) { //Sample to bind
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.nameProperty] = $(this).val();
				}
				_this._storageController.setSelectStorage($(this).val());
			});
		}
		
		if(this._storageModel.config.userSelector === "on" && !this._storageModel.sample) {
			//Paint
			var $controlGroupUserId = FormUtil.getFieldForComponentWithLabel(this._userIdDropdown, "User Id Filter");
			$container.append($controlGroupUserId);
			this._userIdDropdown.multiselect();
			//Attach Event
			this._userIdDropdown.change(function() {
				var selectedUserIds = $(this).val();
				_this._storageController.setUserIdsSelected(selectedUserIds);
			});
		} else if(this._storageModel.sample) { //If someone is updating a sample, his user should go with it
			this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
		}
		
		$container.append(FormUtil.getFieldForComponentWithLabel(this._gridContainer, "Rack"));
		if(this._storageModel.sample) { //If someone is updating a sample, his user should go with it
			this._storageController.setSelectStorage(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.nameProperty]);
		}
		
		if(this._storageModel.config.boxSelector === "on" || this._storageModel.config.rackSelector === "on") {
			//Paint
			this._boxField.hide();
			var $controlGroupBox = FormUtil.getFieldForComponentWithLabel(this._boxField, "Box Name");
			$container.append($controlGroupBox);
			//Attach Event
			this._boxField.keyup(function() {
				if(_this._storageModel.sample) { // Sample to bind
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.boxProperty] = $(this).val();
				}
				_this._storageController.setBoxSelected($(this).val());
			});
			// Sample to bind
			if(this._storageModel.sample) {
				this._boxField.show();
				this._boxField.val(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.boxProperty]);
			}
		}
		
		if(this._storageModel.config.contentsSelector === "on") {
			//Paint
			var $controlGroupBoxContents = FormUtil.getFieldForComponentWithLabel(this._boxContentsDropDown, "Box Contents");
			$container.append($controlGroupBoxContents);
			this._boxContentsDropDown.multiselect();
			//Attach Event
			this._boxContentsDropDown.change(function() {
				var samplesOfBox = _this._gridView._gridModel.getLabelDataByLabelName(_this._storageModel.row,  _this._storageModel.column, _this._storageModel.boxName);
				var selectedSamplePermIds = $(this).val();
				var selectedSamples = [];
				for(var i = 0; i < samplesOfBox.length; i++) {
					var sample = samplesOfBox[i];
					if($.inArray(sample.permId, selectedSamplePermIds) !== -1) {
						selectedSamples.push(sample);
					}
				}
				_this._storageController.setBoxContentsSelected(selectedSamples);
			});
		}
		
		if(this._storageModel.config.positionSelector === "on") {
			//Paint
			var $controlGroupPosition = FormUtil.getFieldForComponentWithLabel(this._positionField, "Position");
			$container.append($controlGroupPosition);
			//Attach Event
			this._positionField.change(function() {
				if(_this._storageModel.sample) { //Sample to bind
					_this._storageModel.sample.properties[_this._storageModel.storagePropertyGroup.positionProperty] = $(this).val();
				}
			});
			//Sample to bind
			if(this._storageModel.sample) {
				this._positionField.val(this._storageModel.sample.properties[this._storageModel.storagePropertyGroup.positionProperty]);
				this._positionField.show();
			}
		}
		
		if(this._storageModel.isDisabled) {
			this._storageGroupsDropDown.attr("disabled", "");
			this._defaultStoragesDropDown.attr("disabled", "");
			this._userIdDropdown.attr("disabled", "");
			this._boxField.attr("disabled", "");
			this._boxContentsDropDown.attr("disabled", "");
			this._positionField.attr("disabled", "");
		}
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
	
	this.refreshBoxContents = function() {
		this._boxContentsDropDown.empty();
		var contents = this._storageModel.boxContents;
		if(contents) {
			for (var i = 0; i < contents.length; i++) {
				this._boxContentsDropDown.append($('<option>', { 'value' : contents[i].permId , 'selected' : ''}).html(contents[i].code));
			}
		} 
		this._boxContentsDropDown.multiselect('rebuild');
	}
	
	//
	// View specific methods
	//
	this.resetSelectStorageDropdown = function() {
		this._defaultStoragesDropDown.val("");
	}
	
	this.refreshGrid = function() {
		this._gridView.repaint(this._gridContainer);
	}
	
	this.hidePosField = function() {
		this._positionField.val("");
		this._positionField.hide();
	}
	
	this.showPosField = function() {
		this._positionField.val("");
		this._positionField.removeAttr("disabled");
		this._positionField.show();
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


}