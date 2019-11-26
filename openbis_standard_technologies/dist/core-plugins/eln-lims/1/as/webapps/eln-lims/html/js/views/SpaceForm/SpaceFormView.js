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
		
		var $form = $("<div>");
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var typeTitle = "Space: ";
		
		var $formTitle = $("<h2>").append(typeTitle + this._spaceFormModel.space);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if (_this._allowedToCreateProject()) {
			var $createProj = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
				_this._spaceFormController.createProject();
			});
			toolbarModel.push({ component : $createProj, tooltip: "Create Project" });
		}

		//Export
		var $exportAll = FormUtil.getExportButton([{ type: "SPACE", permId : _this._spaceFormModel.space, expand : true }], false);
		toolbarModel.push({ component : $exportAll, tooltip: "Export Metadata & Data" });
		
		var $exportOnlyMetadata = FormUtil.getExportButton([{ type: "SPACE", permId : _this._spaceFormModel.space, expand : true }], true);
		toolbarModel.push({ component : $exportOnlyMetadata, tooltip: "Export Metadata only" });
		
		//Jupyter Button
		if(profile.jupyterIntegrationServerEndpoint) {
			var $jupyterBtn = FormUtil.getButtonWithImage("./img/jupyter-icon.png", function () {
				var jupyterNotebook = new JupyterNotebookController(_this._spaceFormModel.space);
				jupyterNotebook.init();
			});
			toolbarModel.push({ component : $jupyterBtn, tooltip: "Create Jupyter notebook" });
		}

		// authorization
		if (this._spaceFormModel.roles.indexOf("ADMIN") > -1 ) {
			var $share = FormUtil.getButtonWithIcon("fa fa-users", function() {
				FormUtil.showAuthorizationDialog({
					space: _this._spaceFormModel.space,
				});
			});
			toolbarModel.push({ component : $share, tooltip: "Manage access" });
		}

		//Freeze
		if(_this._spaceFormModel.v3_space && _this._spaceFormModel.v3_space.frozen !== undefined) { //Freezing available on the API
			var isEntityFrozen = _this._spaceFormModel.v3_space.frozen;
			var isEntityFrozenTooltip = (isEntityFrozen)?"Entity Frozen":"Freeze Entity (Disable further modifications)";
			var $freezeButton = FormUtil.getFreezeButton("SPACE", _this._spaceFormModel.v3_space.permId.permId, isEntityFrozen);
			toolbarModel.push({ component : $freezeButton, tooltip: isEntityFrozenTooltip });
		}

		var $header = views.header;
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));
		
		$container.append($form);
	}
	
	this._allowedToCreateProject = function() {
		var space = this._spaceFormModel.v3_space;
		return space.frozenForProjects == false && this._spaceFormModel.projectRights.rights.indexOf("CREATE") >= 0;
	}
	
}
