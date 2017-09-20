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

function JupyterNotebookView(jupyterNotebookController, jupyterNotebookModel) {
	this._jupyterNotebookController = jupyterNotebookController;
	this._jupyterNotebookModel = jupyterNotebookModel;
	
	this.repaint = function() {
		var _this = this;
		var entity = this._jupyterNotebookModel.entity;
		
		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		
		$window.append($('<legend>').append("Create Jupyter Notebook"));
		
		var $datasetsContainer = $("<div>", { style : "width: 100%;" });
		$window.append(FormUtil.getFieldForComponentWithLabel($datasetsContainer, "Datasets (*)"));
		var datasetsSearchDropdown = new AdvancedEntitySearchDropdown(true, true, "Select as many datasets as you need", false, false, true);
		datasetsSearchDropdown.init($datasetsContainer);
		
		if(entity) {
			switch(entity["@type"]) {
				case "DataSet":
					datasetsSearchDropdown.addSelectedDataSet(entity.code);
					break;
			}
		}
		
		
		var $ownerContainer = $("<div>", { style : "width: 100%;" });
		$window.append(FormUtil.getFieldForComponentWithLabel($ownerContainer, "Owner (*)"));
		var ownerSearchDropdown = new AdvancedEntitySearchDropdown(false, true, "Select one owner " + ELNDictionary.sample, true, true, false);
		ownerSearchDropdown.init($ownerContainer);
		
		if(entity) {
			switch(entity["@type"]) {
				case "DataSet":
					if(entity.sampleIdentifierOrNull) {
						ownerSearchDropdown.addSelectedSample(entity.sampleIdentifierOrNull);
					} else if(entity.experimentIdentifier) {
						ownerSearchDropdown.addSelectedExperiment(entity.experimentIdentifier);
					}
					break;
				case "Sample":
					ownerSearchDropdown.addSelectedSample(entity.identifier);
					break;
				case "Experiment":
					ownerSearchDropdown.addSelectedExperiment(entity.identifier);
					break;
			}
		}
		
		
		var $workspace = FormUtil._getInputField('text', null, 'workspace Name', null, true);
		var $notebookName = FormUtil._getInputField('text', null, 'notebook Name', null, true);
		$window.append(FormUtil.getFieldForComponentWithLabel($workspace, "Workspace"));
		$window.append(FormUtil.getFieldForComponentWithLabel($notebookName, "Notebook Name"));
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });
		$window.submit(function() {
			var selectedDatasets = datasetsSearchDropdown.getSelected();
			var notebookDatasets = [];
			for(var dIdx = 0; dIdx < selectedDatasets.length; dIdx++) {
				notebookDatasets.push(selectedDatasets[dIdx].permId.permId);
			}
				
			var selectedOwner = ownerSearchDropdown.getSelected();
			var notebookOwner = selectedOwner[0];
			_this._jupyterNotebookController.create($workspace.val(), $notebookName.val(), notebookDatasets);
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