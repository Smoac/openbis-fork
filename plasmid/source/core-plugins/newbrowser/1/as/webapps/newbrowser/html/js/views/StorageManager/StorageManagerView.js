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

function StorageManagerView(storageManagerModel, storageFromView, storageToView) {
	this._storageManagerModel = storageManagerModel;
	
	this._storageFromView = storageFromView;
	this._storageToView = storageToView;
	this._moveBtn = $("<a>", { "class" : "btn btn-default"}).append("<span class='glyphicon glyphicon-arrow-right'></span> Move Selected Samples");
	
	this.repaint = function($container) {
		$container.empty();
		
		var $containerColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $storageFromContainer = $("<div>", {"id" : "storageFromContainer", "class" : "row"});
		$containerColumn.append($storageFromContainer);
		this._storageFromView.repaint($storageFromContainer);
		
		var $storageToContainer = $("<div>", {"id" : "storageToContainer", "class" : "row"});
		$containerColumn.append($storageToContainer);
		this._storageToView.repaint($storageToContainer);
		
		$containerColumn.append(this._moveBtn);
		
		$container.append($containerColumn);
	}
	
	this.getMoveButton = function() {
		return this._moveBtn;
	}
}