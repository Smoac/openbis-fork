/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function CreateUserView(createUserController, createUserModel) {
	this._createUserController = createUserController;
	this._createUserModel = createUserModel;
	this._warning = null;
	this._passField = null;
	this._passwordGroup = null;
	
	this.showPasswordField = function() {
		this._passField.removeAttr('disabled');
		this._passwordGroup.show();
		this._warning.show();
	}
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', { 
			'class' : 'form-horizontal', 
			'action' : 'javascript:void(0);'
		});
		
		$window.submit(function() {
			_this._createUserController.createUser();
		});
		
		$window.append($('<legend>').append("Create User"));
		
		//
		// Warning
		//
		this._warning = $("<p>")
							.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
							.append(" Your authentication service requires a password to create a user.");
		this._warning.hide();
		$window.append(this._warning);
		//
		// User ID
		//
		var $userIdField = FormUtil._getInputField('text', null, 'User ID', null, true);
		$userIdField.change(function(event) {
			_this._createUserModel.userId = $(this).val();
		});
		var $userIdGroup = FormUtil.getFieldForComponentWithLabel($userIdField, "User ID", null);
		$window.append($userIdGroup);
		
		//
		// Password
		//
		var $passField = FormUtil._getInputField('text', null, 'Password', null, true);
		$passField.change(function(event) {
			_this._createUserModel.password = $(this).val();
		});
		$passField.prop('disabled', true);
		var $passwordGroup = FormUtil.getFieldForComponentWithLabel($passField, "Password", null);
		$passwordGroup.hide();
		$window.append($passwordGroup);
		this._passField = $passField;
		this._passwordGroup = $passwordGroup;
		
		//
		// Buttons
		//
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });

		var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
		$btnCancel.click(function() {
			Util.unblockUI();
		});
		
		$window.append($btnAccept).append('&nbsp;').append($btnCancel);
		
		var css = {
				'text-align' : 'left',
				'top' : '15%',
				'width' : '70%',
				'left' : '15%',
				'right' : '20%',
				'overflow' : 'hidden'
		};
		
		Util.blockUI($window, css);
	}

}