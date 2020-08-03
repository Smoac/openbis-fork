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

function SpaceFormController(mainController, space) {
	this._mainController = mainController;
	this._spaceFormModel = new SpaceFormModel(space);
	this._spaceFormView = new SpaceFormView(this, this._spaceFormModel);

	this.init = function(views) {
		var _this = this;
		
		require([ "as/dto/space/id/SpacePermId", "as/dto/space/fetchoptions/SpaceFetchOptions", 
				"as/dto/project/id/ProjectIdentifier", "as/dto/rights/fetchoptions/RightsFetchOptions" ],
			function(SpacePermId, SpaceFetchOptions, ProjectIdentifier, RightsFetchOptions) {
			var id = new SpacePermId(space);
			mainController.openbisV3.getSpaces([ id ], new SpaceFetchOptions()).done(function(map) {
				_this._spaceFormModel.v3_space = map[id];
				var space = _this._spaceFormModel.space;
				_this._mainController.getUserRole({
					space: space
				}, function(roles){
					_this._spaceFormModel.roles = roles;
					var dummyId = new ProjectIdentifier("/" + space + "/__DUMMY_FOR_RIGHTS_CALCULATION__");
					mainController.openbisV3.getRights([id, dummyId], new RightsFetchOptions()).done(function(rightsByIds) {
						_this._spaceFormModel.projectRights = rightsByIds[dummyId];
						_this._spaceFormView.repaint(views);
					});
				});
			});
		});
	}

	this.createProject = function() {
		this._mainController.changeView('showCreateProjectPage', this._spaceFormModel.space);
	}

}