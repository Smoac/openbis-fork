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
	
	this.init = function(views) {
		var _this = this;
		
		require([ "as/dto/project/id/ProjectPermId", "as/dto/project/fetchoptions/ProjectFetchOptions" ],
				function(ProjectPermId, ProjectFetchOptions) {
				var id = new ProjectPermId(project.permId);
				var fetchOptions = new ProjectFetchOptions();
				fetchOptions.withSpace();
				mainController.openbisV3.getProjects([ id ], fetchOptions).done(function(map) {
	                _this._projectFormModel.v3_project = map[id];
	                _this._mainController.getUserRole({
	        			space: _this._projectFormModel.project.spaceCode,
	        			project: _this._projectFormModel.project.code,
	        		}, function(roles){
	        			_this._projectFormModel.roles = roles;
	        			_this._projectFormView.repaint(views);
	        		});
	            });		
		});
	}
	
	this.deleteProject = function(reason) {
		var _this = this;
		mainController.serverFacade.deleteProjects([this._projectFormModel.project.id], reason, function(data) {
			if(data.error) {
				Util.showError(data.error.message);
			} else {
				Util.showSuccess("Project Deleted");
				mainController.sideMenu.deleteNodeByEntityPermId(_this._projectFormModel.project.permId, true);
			}
		});
	}
	
	this.createNewExperiment = function(experimentTypeCode) {
		var argsMap = {
				"experimentTypeCode" : experimentTypeCode,
				"projectIdentifier" : IdentifierUtil.getProjectIdentifier(this._projectFormModel.project.spaceCode, this._projectFormModel.project.code)
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
		Util.blockUI();
		if(this._mainController.profile.allDataStores.length > 0) {
			var method = "";
			if(this._projectFormModel.mode === FormMode.CREATE) {
				if(!this._projectFormModel.project.code) {
					Util.showError("Code Missing.");
					return;
				}
				method = "insertProject";
			} else if(this._projectFormModel.mode === FormMode.EDIT) {
				method = "updateProject";
			}
			
			var parameters = {
					//API Method
					"method" : method,
					//Identification Info
					"projectIdentifier" : IdentifierUtil.getProjectIdentifier(this._projectFormModel.project.spaceCode, this._projectFormModel.project.code),
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
						message = "Project Created.";
						_this._mainController.sideMenu.refreshCurrentNode(); //Space Node
					} else if(_this._projectFormModel.mode === FormMode.EDIT) {
						message = "Project Updated.";
					}
					
					var callbackOk = function() {
						_this._projectFormModel.isFormDirty = false;
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

	this.getDefaultSpaceValue = function (key, callback) {
		this._mainController.serverFacade.getSetting(key, callback);
	};

	this.setDefaultSpaceValue = function (key, value) {
		this._mainController.serverFacade.setSetting(key, value);
	};
}
