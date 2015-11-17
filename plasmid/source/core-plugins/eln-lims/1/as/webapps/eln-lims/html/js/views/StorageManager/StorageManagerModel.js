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

function StorageManagerModel() {
	this.changeLog = [{
		permId : "123456789",
		properties: {
			"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage", //Storage Group Name
			"NAME_PROPERTY" : 		"STORAGE_NAMES", //Should be a Vocabulary.
			"ROW_PROPERTY" : 		"STORAGE_ROW", //Should be an integer.
			"COLUMN_PROPERTY" : 	"STORAGE_COLUMN",  //Should be an integer.
			"BOX_PROPERTY" : 		"STORAGE_BOX_NAME", //Should be text.
			"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE", //Should be Vocabulary.
			"USER_PROPERTY" : 		"STORAGE_USER", //Should be text.
			"POSITION_PROPERTY" : 	"STORAGE_POSITION" //Should be text.
		}
	}];
}