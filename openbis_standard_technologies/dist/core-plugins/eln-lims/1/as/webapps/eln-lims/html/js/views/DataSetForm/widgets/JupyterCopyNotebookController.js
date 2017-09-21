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

function JupyterCopyNotebookController(datasetCode, existingNotebookURL) {
	this._jupyterNotebookModel = new JupyterCopyNotebookModel(datasetCode, existingNotebookURL);
	this._jupyterNotebookView = new JupyterCopyNotebookView(this, this._jupyterNotebookModel);
	
	this.init = function() {
		this._jupyterNotebookView.repaint();
	}
	
	this.create = function(workspace, notebook, existingNotebookURL, datasetCode) {
		JupyterUtil.openJupyterNotebookFromTemplate(workspace, notebook, existingNotebookURL, datasetCode);
		Util.unblockUI();
	}
}