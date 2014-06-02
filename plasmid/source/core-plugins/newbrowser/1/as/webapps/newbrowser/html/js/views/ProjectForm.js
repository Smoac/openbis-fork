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
function ProjectForm(containerId, mainController, project) {
	this._containerId = containerId;
	this._mainController = mainController;
	this._project = project;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var _this = this;
		$("#" + this._containerId).empty();
		
		var $form = $("<div>", { "class" : "row"});
		var $formColumn = $("<div>", { "class" : "col-md-12"});
			
		$form.append($formColumn);
		
		//
		// Title
		//
		$formColumn.append($("<h1>").append("Project /" + this._project.spaceCode + "/" + this._project.code));
		
		//
		// Metadata Fields
		//
		
		$("#" + this._containerId).append($form);
	}
}