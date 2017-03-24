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

function UserManagerView(userManagerController, userManagerModel) {
	this._userManagerController = userManagerController;
	this._userManagerModel = userManagerModel;
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		$container.empty();
		
		//
		// Form template and title
		//
		var $containerColumn = $("<form>", { 
			"class" : "form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		$header.append($("<h1>").append("User Manager"));
		
		//
		// ToolBox
		//
		var $toolbox = $("<div>", { 'id' : 'toolBoxContainer', class : 'toolBox'});
		$toolbox.append(this._getOptionsMenu());
		$header.append($toolbox);
		
		//
		// Data Grid
		//
		var dataGridContainer = $("<div>");
		
		var _this = this;
		
		var columns = [ {
			label : 'User ID',
			property : 'userId',
			sortable : true
		} , {
			label : 'Email',
			property : 'email',
			sortable : true
		}, {
			label : 'First Name',
			property : 'firstName',
			sortable : true
		}, {
			label : 'Last Name',
			property : 'lastName',
			sortable : true
		}, {
			label : 'Active',
			property : 'active',
			sortable : true
		}];
		
		columns.push({
			label : "Operations",
			property : 'operations',
			sortable : false,
			render : function(data) {
				//Dropdown Setup
				var $dropDownMenu = $("<span>", { class : 'dropdown' });
				var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
				var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
				$dropDownMenu.append($caret);
				$dropDownMenu.append($list);
				
				var clickFunction = function($dropDown) {
					return function(event) {
						event.stopPropagation();
						event.preventDefault();
						$caret.dropdown('toggle');
					};
				}
				$dropDownMenu.dropdown();
				$dropDownMenu.click(clickFunction($dropDownMenu));
				
				//Options
				var $changeOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Change Password'}).append("Change Password"));
				$changeOption.click(function(e) {
					_this._userManagerController.resetPassword(data.userId);
				});
				$list.append($changeOption);
				
				return $dropDownMenu;
			},
			filter : function(data, filter) {
				return false;
			},
			sort : function(data1, data2, asc) {
				return 0;
			}
		});
		
		var getDataList = function(callback) {
			var dataList = [];
			for(var idx = 0; idx < _this._userManagerModel.persons.length; idx++) {
				var person =  _this._userManagerModel.persons[idx];
				dataList.push({
					userId : person.userId,
					email : person.email,
					firstName : person.firstName,
					lastName : person.lastName,
					active : (person.active)?"True":"False"
				});
			}
			callback(dataList);
		}
		
		var dataGrid = new DataGridController(null, columns, [], null, getDataList, null, false, "USER_MANAGER_TABLE");
		dataGrid.init(dataGridContainer);
		
		//
		// Render
		//
		$containerColumn.append(dataGridContainer);
		$container.append($containerColumn);
	}
	
	this._getOptionsMenu = function() {
		var _this = this;
		var $dropDownMenu = $("<span>", { class : 'dropdown' });
		var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
		var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
		$dropDownMenu.append($caret);
		$dropDownMenu.append($list);
		
		var $createUserOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Create User'}).append("Create User"));
		$createUserOption.click(function() {
			_this._userManagerController.showCreateNewUserModal();
		});
		$list.append($createUserOption);
			
		return $dropDownMenu;
	}
}