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

function StorageManagerView(storageManagerController, storageManagerModel, storageFromView, storageToView) {
	this._storageManagerController = storageManagerController;
	this._storageManagerModel = storageManagerModel;
	
	this._storageFromView = storageFromView;
	this._storageToView = storageToView;
	this._changeLogContainer = $("<div>").append("None");
	
	this._moveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", null, "Save Changes");
	this._moveBtn.removeClass("btn-default");
	this._moveBtn.addClass("btn-primary");
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		
		$header.append($("<h2>").append("Storage Manager"));
		$header.append(this._moveBtn);
		
		var $containerColumn = $("<form>", { 
			"class" : "form-horizontal", 
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $twoColumnsContainer = $("<div>", {"id" : "storageFromContainer", "class" : "row"});
		
		var $storageFromContainer = $("<div>", {"id" : "storageFromContainer", "class" : "col-md-6"});
		$twoColumnsContainer.append($storageFromContainer);
		this._storageFromView.repaint($storageFromContainer);
		
		var $storageToContainer = $("<div>", {"id" : "storageToContainer", "class" : "col-md-6"});
		$twoColumnsContainer.append($storageToContainer);
		this._storageToView.repaint($storageToContainer);
		
		
		$containerColumn.append($twoColumnsContainer);
		$containerColumn.append($("<div>").append($("<h2>").append("Changes")).append(this._changeLogContainer));
		$container.append($containerColumn);
	}
	
	this.getMoveButton = function() {
		return this._moveBtn;
	}
	
	this.updateChangeLogView = function() {
		this._changeLogContainer.empty();
		var changeLog = this._storageManagerModel.changeLog;
		for(var cIdx = 0; cIdx < changeLog.length; cIdx++) {
			var item = changeLog[cIdx];
			this._changeLogContainer.append("<strong>Type:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.type);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>Identifier:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.data.identifier);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Box:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.boxProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Position:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.positionProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Storage:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.nameProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append("<strong>New Rack:</strong>");
			this._changeLogContainer.append(" ");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.rowProperty]);
			this._changeLogContainer.append(",");
			this._changeLogContainer.append(item.newProperties[item.storagePropertyGroup.columnProperty]);
			this._changeLogContainer.append(" ");
			
			this._changeLogContainer.append($("<br>"));
		}
	}
}