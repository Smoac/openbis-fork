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

function SpaceFormView(spaceFormController, spaceFormModel) {
	this._spaceFormController = spaceFormController;
	this._spaceFormModel = spaceFormModel;
	
	this.repaint = function(views) {
		var _this = this;
		var $container = views.content;
		
		var $form = $("<div>", { "class" : "form-horizontal"});
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var typeTitle = "Space: ";
//		if(profile.isInventorySpace(this._spaceFormModel.space.code)) {
//			typeTitle = "";
//		}
		
		var $formTitle = $("<h2>").append(typeTitle + this._spaceFormModel.space.code);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		var $createProj = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
			_this._spaceFormController.createProject();
		});
		
		var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
			Util.blockUI();
			var facade = mainController.serverFacade;
			facade.exportAll([{ type: "SPACE", permId : _this._spaceFormModel.space.code, expand : true }], false, function(error, result) {
				if(error) {
					Util.showError(error);
				} else {
					Util.showSuccess("Export is being processed, you will receive an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
				}
			});
		});
		
		toolbarModel.push({ component : $createProj, tooltip: "Create Project" });
		toolbarModel.push({ component : $export, tooltip: "Export" });
		
		var $header = views.header;
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		$container.append($form);
	}
}