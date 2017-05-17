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

function SettingsFormView(settingsFormController, settingsFormModel) {
	this._settingsFormController = settingsFormController;
	this._settingsFormModel = settingsFormModel;
	
	this.repaint = function(views) {
		var _this = this;
		var $container = views.content;
		
		var $form = $("<div>");
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var typeTitle = "Settings";
		
		var $formTitle = $("<h2>").append(typeTitle);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		
		
		if(this._settingsFormModel.mode === FormMode.VIEW) {
			//Edit
			var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
				mainController.changeView('showEditSettingsPage');
			});
			toolbarModel.push({ component : $editButton, tooltip: "Edit" });
		} else { //Create and Edit
			//Save
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", function() {
				_this._settingsFormController.save();
			}, "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		var $header = views.header;
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		$container.append($form);
	}
}