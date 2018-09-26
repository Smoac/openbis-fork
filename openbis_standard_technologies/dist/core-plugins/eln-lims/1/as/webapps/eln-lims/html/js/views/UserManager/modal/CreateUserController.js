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

function CreateUserController() {
	this._createUserModel = new CreateUserModel();
	this._createUserView = new CreateUserView(this, this._createUserModel);
	
	this.init = function() {
		this._createUserView.repaint();
	}
	
	this.createUser = function() {
		var _this = this;
		this._createUserView.disableAccept();
		
			var createUser = function() {
				mainController.serverFacade.createELNUser(_this._createUserModel.userId, function(isRegistered, message) {
					if(isRegistered) {
						Util.showSuccess(message, function() {
							Util.unblockUI();
							mainController.changeView("showUserManagerPage");
						});
					} else if (message.indexOf("Following persons already exist") !== -1){
						Util.showUserError(message, function() {
							_this._createUserView.enableAccept();
						}, true);
					} else {
						_this._createUserView.showPasswordField();
						_this._createUserModel.isPasswordRequired = true;
						_this._createUserView.enableAccept();
					}
				});
			}
			
			if(!this._createUserModel.isPasswordRequired) {
				createUser();
			} else {
				if(_this._createUserModel.password === _this._createUserModel.passwordRepeat) {
					mainController.serverFacade.registerUserPassword(
							_this._createUserModel.userId,
							_this._createUserModel.password,
							function(isRegistered) {
								if(isRegistered) {
									createUser();
								} else {
									Util.showError("User can't be created, check with your administator.", function() {
										_this._createUserView.enableAccept();
									}, true);
								}
							});
				} else {
					Util.showUserError("Passwords are not equal.", function() { _this._createUserView.enableAccept(); }, true);
				}
			}
		
	}
}