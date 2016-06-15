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
	
	this.repaint = function($container) {
		$container.empty();
		
		var $form = $("<div>", { "class" : "form-horizontal row"});
		var $formColumn = $("<div>", { "class" : FormUtil.formColumClass });
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append("Lab Notebook");
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		
		var $export = FormUtil.getButtonWithIcon("glyphicon-export", function() {
			Util.blockUI();
			var facade = mainController.serverFacade;
			facade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
				var spaces = dataWithSpacesAndProjects.result;
	            var labSpaces = [];
				for (var i = 0; i < spaces.length; i++) {
	                var space = spaces[i];
	                if(!profile.isInventorySpace(space.code)) {
	                	labSpaces.push({ type: "SPACE", permId : space.code, expand : true });
	                }
	            }
	            
				facade.exportAll(labSpaces, true, function(error, result) {
					if(error) {
						Util.showError(error);
					} else {
						Util.showSuccess("Export is being processed, you will receibe an email when is ready, if you logout the process will stop.", function() { Util.unblockUI(); });
					}
				});
				
			});
		});
		
		toolbarModel.push({ component : $export, tooltip: "Export" });
		
		
		
		$formColumn.append($formTitle);
		$formColumn.append(FormUtil.getToolbar(toolbarModel));
		$formColumn.append("<br>");
		
		$container.append($form);
	}
}