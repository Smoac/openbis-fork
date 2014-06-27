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
	
	this._defaultStoragesDropDown = FormUtil.getDefaultStoragesDropDown("", true);
	this._userIdFilter = FormUtil._getInputField("text", "", "User id to filter", null, false);
	this._gridContainer = $("<div>");
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		$container.append("<h2>Storage Widget Test</h2>");
		
		this._defaultStoragesDropDown.change(function(event) {
			var value = $(this).val();
			_this._gridView.repaint(_this._gridContainer);
		});
		
		var $controlGroupStorages = FormUtil.getFieldForComponentWithLabel(this._defaultStoragesDropDown, "Storage");
		$container.append($controlGroupStorages);
		
		var $controlGroupUserId = FormUtil.getFieldForComponentWithLabel(this._userIdFilter, "User Id Filter");
		$container.append($controlGroupUserId);
		
		$container.append(this._gridContainer);
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageModel;
	}
}