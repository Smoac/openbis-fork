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

function ExperimentFormController(mainController, mode, experiment) {
	this._mainController = mainController;
	this._experimentFormModel = new ExperimentFormModel(mode, experiment);
	this._experimentFormView = new ExperimentFormView(this, this._experimentFormModel);
	
	this.init = function($container) {
		this._experimentFormView.repaint($container);
	}
	
	this.isDirty = function() {
		return this._experimentFormModel.isFormDirty;
	}
	
	this.updateExperiment = function() {
		Util.blockUI();
		
		var experimentType = this._mainController.profile.getExperimentTypeForExperimentTypeCode(this._experimentFormModel.experiment.experimentTypeCode);
		
		//Identification Info (This way of collecting the identifier also works for the creation mode)
		var projectIdentifier = this._experimentFormModel.experiment.identifier.split("/");
		var experimentSpace = projectIdentifier[1];
		var experimentProject = projectIdentifier[2];
		var experimentCode = this._experimentFormModel.experiment.code;
		var experimentIdentifier = "/" + experimentSpace + "/" + experimentProject + "/" + experimentCode;
		
		var method = "";
		if(this._experimentFormModel.mode === FormMode.CREATE) {
			method = "insertExperiment";
		} else if(this._experimentFormModel.mode === FormMode.EDIT) {
			method = "updateExperiment";
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"experimentType" : this._experimentFormModel.experiment.experimentTypeCode,
				"experimentIdentifier" : experimentIdentifier,
				//Properties
				"experimentProperties" : this._experimentFormModel.experiment.properties
		};
		
		var _this = this;
		
		if(this._mainController.profile.allDataStores.length > 0) {
			this._mainController.serverFacade.createReportFromAggregationService(this._mainController.profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var experimentType = _this._mainController.profile.getExperimentTypeForExperimentTypeCode(_this._experimentFormModel.experiment.experimentTypeCode);
					var experimentTypeDisplayName = experimentType.description;
					if(!experimentTypeDisplayName) {
						experimentTypeDisplayName = _this._experimentFormModel.experiment.experimentTypeCode;
					}
					
					var message = "";
					if(_this._experimentFormModel.mode === FormMode.CREATE) {
						message = "Created.";
					} else if(_this._experimentFormModel.mode === FormMode.EDIT) {
						message = "Updated.";
					}
					
					var callbackOk = function() {
						var projectIdentifier = "/" + experimentSpace + "/" + experimentProject;
						_this._mainController.sideMenu.refreshExperiment(projectIdentifier, experimentCode);
						_this._experimentFormModel.isFormDirty = false;
						_this._mainController.changeView("showExperimentPageFromIdentifier", experimentIdentifier);
						Util.unblockUI();
					}
					
					Util.showSuccess(experimentTypeDisplayName + " " + message, callbackOk);
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
				
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}