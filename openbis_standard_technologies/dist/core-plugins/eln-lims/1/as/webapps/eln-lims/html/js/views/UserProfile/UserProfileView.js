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

function UserProfileView(userProfileController, userProfileModel) {
	this._userProfileController = userProfileController;
	this._userProfileModel = userProfileModel;

	this.repaint = function(views, profileToEdit) {

        // header
        var $header = views.header;
        var typeTitle = "User Profile";
        var $formTitle = $("<h2>").append(typeTitle);
        $header.append($formTitle);
		// ToolBox
		var $toolbox = $("<div>", { 'id' : 'toolBoxContainer', class : 'toolBox'});
		$toolbox.append(this._getOptionsMenu());
		$header.append($toolbox);

        // formColumn for content
		var $container = views.content;
        var $form = $("<div>");
        var $formColumn = $("<div>");
        $form.append($formColumn);
        $container.append($form);
    }

	this._getOptionsMenu = function() {
        var items = [
            {
                label : "Change Password",
                event : this._userProfileController.resetPassword.bind(this._userProfileController),
            }
        ];
        return FormUtil.getOperationsMenu(items);
	}

}