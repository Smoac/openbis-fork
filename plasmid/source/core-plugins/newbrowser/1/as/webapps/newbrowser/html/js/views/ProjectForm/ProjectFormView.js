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

function ProjectFormView(projectFormController, projectFormModel) {
	this._projectFormController = projectFormController;
	this._projectFormModel = projectFormModel;
	
	this._createExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Experiment");
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "form-horizontal row"});
		var $formColumn = $("<div>", { "class" : FormUtil.formColumClass });
			
		$form.append($formColumn);
		
		//
		// Title
		//
		
		var $formTitle = $("<h2>").append("Project /" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code);
		$formColumn.append($formTitle);
		
		if(this._projectFormModel.mode === FormMode.VIEW) {
			var $createExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Experiment");
			$createExpBtn.click(function() {
				var $dropdown = FormUtil.getExperimentTypeDropdown("experimentTypeDropdown", true);
				Util.blockUI("Select the type for the Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='experimentTypeDropdownCancel'>Cancel</a>");
				
				$("#experimentTypeDropdown").on("change", function(event) {
					var experimentTypeCode = $("#experimentTypeDropdown")[0].value;
					_this._projectFormController.createNewExperiment(experimentTypeCode);
				});
				
				$("#experimentTypeDropdownCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			});
			$formTitle.append(" ");
			$formTitle.append($createExpBtn);
			$formTitle.append(" ");
			var $editBtn = $("<a>", { "class" : "btn btn-default"}).append("<span class='glyphicon glyphicon-edit'></span> Enable Editing");
			$editBtn.click(function() {
				_this._projectFormController.enableEditing();
			});
			$formTitle.append($editBtn);
		}
		
		
		//
		// Metadata Fields
		//
		$formColumn.append($("<legend>").append("Identification Info"));
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Space", this._projectFormModel.project.spaceCode));
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._projectFormModel.project.spaceCode));
		
		if(this._projectFormModel.mode === FormMode.EDIT) {
			var $textBox = FormUtil._getTextBox(null, "Description", false);
			$textBox.keyup(function(event){
				_this._projectFormModel.project.description = $(this).val();
			});
			$textBox.val(this._projectFormModel.project.description);
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Description", this._projectFormModel.project.description));
		}
		
		$formColumn.append(FormUtil.getFieldForLabelWithText("Registered By", this._projectFormModel.project.registrationDetails.userId));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Registration Date", new Date(this._projectFormModel.project.registrationDetails.registrationDate).toLocaleString()));
		
		if(this._projectFormModel.project.registrationDetails.modificationDate) {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Modification Date", new Date(this._projectFormModel.project.registrationDetails.modificationDate).toLocaleString()));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Modification Date", "Never modified"));
		}
		
		if(this._projectFormModel.mode === FormMode.EDIT) {
			var $updateBtn = $("<a>", { "class" : "btn btn-default"}).append("Update Project " + this._projectFormModel.project.code);
			$updateBtn.click(function() {
				_this._projectFormController.updateProject();
			});
			$formColumn.append($updateBtn);
		}
		$container.append($form);
	}
}