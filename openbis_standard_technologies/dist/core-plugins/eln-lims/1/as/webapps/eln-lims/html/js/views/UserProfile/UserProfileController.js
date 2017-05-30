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

function UserProfileController(mainController, mode) {
	this._mainController = mainController;
	this._userProfileModel = new UserProfileModel(mode);
	this._userProfileView = new UserProfileView(this, this._userProfileModel);

	this.init = function(views) {
        this._userProfileView.repaint(views);
	}

	this.resetPassword = function() {
		var userId = this._mainController.serverFacade.getUserId();
		var resetPasswordController = new ResetPasswordController(userId);
		resetPasswordController.init();
	}

	this.getUserInformation = function(callback) {
		var userId = this._mainController.serverFacade.getUserId();
		this._mainController.serverFacade.listPersons(function(response) {
			for (person of response.result) {
				if (person.userId === userId) {
					callback(person);
				}
			}
		});
	}

	this.save = function(userInformation) {
		var errors = this._validate(userInformation);
		if (errors.length > 0) {
			Util.showError(FormUtil._getSanitizedErrorString("Validation Error:", errors));
			return;
		}
		var userId = this._mainController.serverFacade.getUserId();
		this._mainController.serverFacade.updateUserInformation(userId, userInformation, function(ok) {
			if (ok) {
				mainController.changeView("showUserProfilePage");
			} else {
				Util.showError(FormUtil._getSanitizedErrorString("Error:", ["Could not save user information."]));
			}
		});
	}

	this._validate = function(userInformation) {
		var errors = [];
		var fields = ["firstName", "lastName", "email"];
		for (var field of fields) {
			if (userInformation[field] == null || userInformation[field].length === 0) {
				errors.push(field + " can not be empty.");
			}
		}
		return errors;
	}

}