/*
 * Copyright 2016 ETH Zuerich, Scientific IT Services
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

function LabNotebookView(labNotebookController, labNotebookView) {
	var labNotebookController = labNotebookController;
	var labNotebookView = labNotebookView;
	
	this.repaint = function(views) {
		
		var $form = $("<div>");
		var $formColumn = $("<div>");
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append("Lab Notebook");
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		
		mainController.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
			var spaces = dataWithSpacesAndProjects.result;
	        var labSpaces = [];
			for (var i = 0; i < spaces.length; i++) {
	       		var space = spaces[i];
	           	if(!profile.isInventorySpace(space.code)) {
	           		labSpaces.push({ type: "SPACE", permId : space.code, expand : true });
	         	}
	       	}
	            
	        //Export
			var $exportAll = FormUtil.getExportButton(labSpaces, false, true);
			toolbarModel.push({ component : $exportAll, tooltip: "Export Metadata & Data" });
		
			var $exportOnlyMetadata = FormUtil.getExportButton(labSpaces, true, true);
			toolbarModel.push({ component : $exportOnlyMetadata, tooltip: "Export Metadata only" });
			
			views.header.append(FormUtil.getToolbar(toolbarModel));
		});
		
		views.header.append($formTitle);
		views.content.append($formColumn);
	}
}