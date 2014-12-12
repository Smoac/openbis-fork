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
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', { 'class' : 'form-horizontal', 'action' : 'javascript:void(0);' });
		$window.submit(function() {
			Util.unblockUI();
			_this._typeAndFileModel.actionFunction(_this._typeAndFileModel.sampleType, _this._typeAndFileModel.file);
		});
		
		$window.append($('<legend>').append("Create User"));
		
		var userId = FormUtil._getInputField('text', null, 'User ID', null, true);
		userId.change(function(event) {
			this._createUserModel.userId = $(this).val();
		});
		var userIdField = FormUtil.getFieldForComponentWithLabel(userId, "User ID", null);
		$window.append(userIdField);
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });
		$btnAccept.click(function() {
			alert("CREATE USER");
		});
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