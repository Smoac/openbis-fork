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
	this._formColumClass = 'col-md-12'
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var _this = this;
		$("#" + this._containerId).empty();
		
		var $form = $("<div>", { "class" : "form-horizontal row"});
		var $formColumn = $("<div>", { "class" : this._formColumClass });
			
		$form.append($formColumn);
		
		//
		// Title
		//
		var $formTitle = $("<h2>").append("Project /" + this._project.spaceCode + "/" + this._project.code);
		$formColumn.append($formTitle);
		
		var $createExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Experiment");
		$createExpBtn.click(function() {
			var $dropdown = FormUtil.getExperimentTypeDropdown("experimentTypeDropdown", true);
			Util.blockUI("Select the type for the Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='experimentTypeDropdownCancel'>Cancel</a>");
			
			$("#experimentTypeDropdown").on("change", function(event) {
				var experimentTypeCode = $("#experimentTypeDropdown")[0].value;
				var argsMap = {
						"experimentTypeCode" : experimentTypeCode,
						"projectIdentifier" : "/" + _this._project.spaceCode + "/" + _this._project.code
				}
				var argsMapStr = JSON.stringify(argsMap);
				
				_this._mainController.changeView("showCreateExperimentPage", argsMapStr);
			});
			
			$("#experimentTypeDropdownCancel").on("click", function(event) { 
				Util.unblockUI();
			});
		});
		$formTitle.append(" ");
		$formTitle.append($createExpBtn);
		
		//
		// Metadata Fields
		//
		$formColumn.append($("<legend>").append("Identification Info"));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Space", this._project.spaceCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Code", this._project.spaceCode));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Description", this._project.description));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Registered By", this._project.registrationDetails.userId));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Registration Date", this._project.registrationDetails.registrationDate));
		$formColumn.append(FormUtil.getFieldForLabelWithText("Modification Date", this._project.registrationDetails.modificationDate));
		
		$("#" + this._containerId).append($form);
	}
	
	
}