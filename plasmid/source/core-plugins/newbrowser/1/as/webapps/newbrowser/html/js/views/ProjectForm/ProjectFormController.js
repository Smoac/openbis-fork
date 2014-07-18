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

function ProjectFormController(mainController, mode, project) {
	this._mainController = mainController;
	this._projectFormModel = new ProjectFormModel(mode, project);
	this._projectFormView = new ProjectFormView(this, this._projectFormModel);
	
	this.init = function($container) {
		this._projectFormView.repaint($container);
	}
	
	this.createNewExperiment = function(experimentTypeCode) {
		var argsMap = {
				"experimentTypeCode" : experimentTypeCode,
				"projectIdentifier" : "/" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code
		}
		var argsMapStr = JSON.stringify(argsMap);
		
		this._mainController.changeView("showCreateExperimentPage", argsMapStr);
	}
	
	this.enableEditing = function() {
		this._mainController.changeView('showEditProjectPageFromPermId', this._projectFormModel.project.permId);
	}
	
	this.isDirty = function() {
		return this._projectFormModel.isFormDirty;
	}
	
	this.updateProject = function() {
		if(this._mainController.profile.allDataStores.length > 0) {
			var method = "";
			if(this._projectFormModel.mode === FormMode.CREATE) {
				method = "insertProject";
			} else if(this._projectFormModel.mode === FormMode.EDIT) {
				method = "updateProject";
			}
			
			var parameters = {
					//API Method
					"method" : method,
					//Identification Info
					"projectIdentifier" : "/" + this._projectFormModel.project.spaceCode + "/" + this._projectFormModel.project.code,
					"projectDescription" : this._projectFormModel.project.description
			};
			
			var _this = this;
			this._mainController.serverFacade.createReportFromAggregationService(this._mainController.profile.allDataStores[0].code, parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					Util.showStacktraceAsError(stacktrace);
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					var message = "";
					if(_this._projectFormModel.mode === FormMode.CREATE) {
						message = "Created.";
					} else if(_this._projectFormModel.mode === FormMode.EDIT) {
						message = "Updated.";
					}
					
					var callbackOk = function() {
						_this._mainController.changeView("showProjectPageFromIdentifier", parameters["projectIdentifier"]);
						Util.unblockUI();
					}
					
					Util.showSuccess(message, callbackOk);
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
				
			});
			
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}