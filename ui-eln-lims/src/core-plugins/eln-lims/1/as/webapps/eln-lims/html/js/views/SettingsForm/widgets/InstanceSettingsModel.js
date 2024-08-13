/*
 * Copyright 2024 ETH Zuerich, Scientific IT Services
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
//  './etc/instanceSettings.json';

function InstanceSettingsModel(mode, profileToEdit) {
	this.mode = mode;
	this.profileToEdit = profileToEdit;

    this.settings = (function () {
                         var json = null;
                         $.ajax({
                             'async': false,
                             'global': false,
                             'url': './etc/instanceSettings.json',
                             'dataType': "json",
                             'success': function (data) {
                                 json = data;
                             }
                         });
                         return json;
                     })();


    this.getSettings = function() {
        return this.settings;
    }





}