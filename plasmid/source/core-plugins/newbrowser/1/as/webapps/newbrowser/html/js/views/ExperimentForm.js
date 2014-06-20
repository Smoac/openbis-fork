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

ExperimentFormMode = {
    CREATE : 0,
    EDIT : 1,
    VIEW : 2
}

function ExperimentForm(containerId, mainController, experiment, mode) {
	this._containerId = containerId;
	this._mainController = mainController;
	this._experiment = experiment;
	this._mode = mode;
	
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
		var $formTitle = null;
		if(this._mode === ExperimentFormMode.VIEW || this._mode === ExperimentFormMode.EDIT) {
			$formTitle = $("<h1>").append("Experiment " + this._experiment.identifier);
		} else {
			$formTitle = $("<h1>").append("Create " + this._experiment.experimentTypeCode);
		}
		$formColumn.append($formTitle);
		
		//
		// Metadata Fields
		//
		var experimentType = this._mainController.profile.getExperimentTypeForExperimentTypeCode(this._experiment.experimentTypeCode);
		
		//
		// Create Sub Experiment
		//
		if(this._mode === ExperimentFormMode.VIEW) {
			var $createSubExpBtn = $("<a>", { "class" : "btn btn-default"}).append("Create Sub Experiment");
			$createSubExpBtn.click(function() {
				var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true);
				Util.blockUI("Select the type for the sub Experiment: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeDropdownCancel'>Cancel</a>");
				
				$("#sampleTypeDropdown").on("change", function(event) {
					var sampleTypeCode = $("#sampleTypeDropdown")[0].value;
					var argsMap = {
							"sampleTypeCode" : sampleTypeCode,
							"experimentIdentifier" : _this._experiment.identifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					
					_this._mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				
				$("#sampleTypeDropdownCancel").on("click", function(event) { 
					Util.unblockUI();
				});
			});
			$formColumn.append($createSubExpBtn);
		}
		$("#" + this._containerId).append($form);
	}
}