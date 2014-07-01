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

function StorageView(storageModel, gridView) {
	this._storageModel = storageModel;
	this._gridView = gridView;
	
	this._storageGroupsDropDown = FormUtil.getStoragePropertyGroupsDropdown("", true);
	this._defaultStoragesDropDown = FormUtil.getDefaultStoragesDropDown("", true);
	this._userIdFilter = FormUtil._getInputField("text", "", "User id to filter", null, false);
	this._gridContainer = $("<div>");
	this._boxField = FormUtil._getInputField("text", "", "Box Name", null, false);
	this._boxContentsDropDown = $('<select>', { 'id' : 'boxSamplesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		$container.append("<h2>" + this._storageModel.config.title + "</h2>");
		
		if(this._storageModel.config.storagePropertyGroupSelector === "on") {
			var $controlGroupStoragesGroups = FormUtil.getFieldForComponentWithLabel(this._storageGroupsDropDown, "Group");
			$container.append($controlGroupStoragesGroups);
			this._storageModel.storagePropertyGroup = profile.getStoragePropertyGroup(this._storageGroupsDropDown.val());
		}
		
		if(this._storageModel.config.storageSelector === "on") {
			var $controlGroupStorages = FormUtil.getFieldForComponentWithLabel(this._defaultStoragesDropDown, "Storage");
			$container.append($controlGroupStorages);
		}
		
		if(this._storageModel.config.userSelector === "on") {
			var $controlGroupUserId = FormUtil.getFieldForComponentWithLabel(this._userIdFilter, "User Id Filter");
			$container.append($controlGroupUserId);
		}
		
		$container.append(FormUtil.getFieldForComponentWithLabel(this._gridContainer, "Rack"));
		
		if(this._storageModel.config.boxSelector === "on" || this._storageModel.config.rackSelector === "on") {
			this._boxField.hide();
			var $controlGroupBox = FormUtil.getFieldForComponentWithLabel(this._boxField, "Box Name");
			$container.append($controlGroupBox);
		}
		
		if(this._storageModel.config.contentsSelector === "on") {
			var $controlGroupBoxContents = FormUtil.getFieldForComponentWithLabel(this._boxContentsDropDown, "Box Contents");
			$container.append($controlGroupBoxContents);
			this._boxContentsDropDown.multiselect();
		}
	}
	
	this.refreshBoxContents = function() {
		this._boxContentsDropDown.empty();
		var contents = this._storageModel.boxContents;
		if(contents) {
			for (var i = 0; i < contents.length; i++) {
				this._boxContentsDropDown.append($('<option>', { 'value' : contents[i].permId , 'selected' : ''}).html(contents[i].code));
			}
			this._boxContentsDropDown.multiselect('rebuild');
		}
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageModel;
	}
	
	this.getSelectStorageGroupDropdown = function() {
		return this._storageGroupsDropDown;
	}
	
	this.resetSelectStorageDropdown = function() {
		this._defaultStoragesDropDown.val("");
	}
	
	this.getSelectStorageDropdown = function() {
		return this._defaultStoragesDropDown;
	}
	
	this.getGridContainer = function() {
		return this._gridContainer;
	}
	
	this.getBoxField = function() {
		return this._boxField;
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
	
	this.showBoxName = function(boxName) {
		this._boxField.val(boxName);
		this._boxField.attr("disabled", "");
		this._boxField.show();
	}
	
	this.getBoxContentsDropDown = function() {
		return this._boxContentsDropDown;
	}
}