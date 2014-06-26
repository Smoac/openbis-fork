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

function StorageView(storageModel) {
	this._storageModel = storageModel;
	
	this.defaultStoragesDropDown = FormUtil.getDefaultStoragesDropDown("", true);
	
	this.repaint = function($container) {
		$container.empty();
		$container.append("<h2>Storage Widget Test</h2>");
		var $controlGroup = FormUtil.getFieldForComponentWithLabel(this.defaultStoragesDropDown, "Storage");
		$container.append($controlGroup);
	}
	
	//
	// Getters
	//
	this.getModel = function() {
		return this._storageModel;
	}
}