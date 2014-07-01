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

function StorageModel(configOverride) {
	if(configOverride) {
		this.config = configOverride;
	} else {
		this.config = {
				title : "",
				storagePropertyGroupSelector : "on",
				storageSelector : "on",
				userSelector : "on",
				boxSelector: "on",
				rackSelector: "on",
				contentsSelector: "off"
		};
	}
	
	this.storagePropertyGroup = null; //Selected Storage Property Group
	this.storageCode = null; //Selected Storage
	this.userIds = null;
	this.userIdsSelected = null; //Selected user Being filtered (creator or latest modifier of the box)
	this.row = null; //Selected Row
	this.column = null; //Selected Column
	this.boxName = null; //Selected Box
	this.boxContents = null; //Selected Box contents (samples)
	
	this.resetBoxInfo = function(row, column, boxName, boxContents) {
		this.row = row; //Selected Row
		this.column = column; //Selected Column
		this.boxName = boxName; //Selected Box
		this.boxContents = boxContents; //Selected Box contents (samples)
	}
}