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

function TrashManagerView(trashManagerController, trashManagerModel) {
	this._trashManagerController = trashManagerController;
	this._trashManagerModel = trashManagerModel;
	
	this.repaint = function($container) {
		$container.empty();
		
		var $containerColumn = $("<form>", { 
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $trashIcon = $("<span>", { 'class' : 'glyphicon glyphicon-trash'});
		$containerColumn.append($("<h2>").append($trashIcon).append(" Trashcan"));
		
		$container.append($containerColumn);
	}
}