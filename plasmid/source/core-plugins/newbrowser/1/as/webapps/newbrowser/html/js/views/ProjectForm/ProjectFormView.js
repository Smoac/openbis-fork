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
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $form = $("<div>", { "class" : "row"});
		
		var $formColumn = $("<form>", {
			"class" : FormUtil.formColumClass + " form-horizontal", 
			'role' : "form",
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.updateProject();'
		});
			
		$form.append($formColumn);
		
		//
		// Title
		//
		var title = null;
		if(this._projectFormModel.mode === FormMode.CREATE) {
			title = "Create Project";
		} else {
			title = "Project /" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code;
		}
		var $formTitle = $("<h2>").append(title);
		$formColumn.append($formTitle);
		
		if(this._projectFormModel.mode !== FormMode.CREATE) {
			//Delete
			$formTitle.append("&nbsp;");
			$formTitle.append(FormUtil.getDeleteButton(function(reason) {
				_this._projectFormController.deleteProject(reason);
			}, true));
		}
		
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
			$formTitle.append("&nbsp;");
			$formTitle.append($createExpBtn);
			$formTitle.append("&nbsp;");
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
		
		if(this._projectFormModel.mode === FormMode.CREATE) {
			var $textField = FormUtil._getInputField('text', null, "Project Code", null, true);
			$textField.keyup(function(event){
				var textField = $(this);
				textField.val(textField.val().toUpperCase());
				_this._projectFormModel.project.code = textField.val();
				_this._projectFormModel.isFormDirty = true;
			});
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textField, "Code"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._projectFormModel.project.code));
		}
		
		
		var description = Util.getEmptyIfNull(this._projectFormModel.project.description);
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var $textBox = FormUtil._getTextBox(null, "Description", false);
			$textBox.keyup(function(event){
				_this._projectFormModel.project.description = $(this).val();
				_this._projectFormModel.isFormDirty = true;
			});
			$textBox.val(description);
			$formColumn.append(FormUtil.getFieldForComponentWithLabel($textBox, "Description"));
		} else {
			$formColumn.append(FormUtil.getFieldForLabelWithText("Description", description));
		}
		
		//
		// Registration and modification info
		//
		if(this._projectFormModel.mode !== FormMode.CREATE) {
			var registrationDetails = this._projectFormModel.project.registrationDetails;
			
			var $registrator = FormUtil.getFieldForLabelWithText("Registrator", registrationDetails.userId);
			$formColumn.append($registrator);
			
			var $registationDate = FormUtil.getFieldForLabelWithText("Registration Date", (new Date(registrationDetails.registrationDate)).toLocaleString())
			$formColumn.append($registationDate);
			
			var $modifier = FormUtil.getFieldForLabelWithText("Modifier", registrationDetails.modifierUserId);
			$formColumn.append($modifier);
			
			var $modificationDate = FormUtil.getFieldForLabelWithText("ModificationDate", (new Date(registrationDetails.modificationDate)).toLocaleString());
			$formColumn.append($modificationDate);
		}
		
		if(this._projectFormModel.mode !== FormMode.VIEW) {
			var btnText = null;
			if(this._projectFormModel.mode === FormMode.CREATE) {
				btnText = "Create Project";
			} else if(this._projectFormModel.mode === FormMode.EDIT) {
				btnText = "Update Project " + this._projectFormModel.project.code;
			}
			
			var $updateBtn = $("<input>", { "type": "submit", "class" : "btn btn-primary", 'value' : btnText });
			$formColumn.append($updateBtn);
		}
		$container.append($form);
	}
}