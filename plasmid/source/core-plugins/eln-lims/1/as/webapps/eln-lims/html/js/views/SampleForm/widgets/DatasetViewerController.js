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

/**
 * Creates an instance of DataSetViewer.
 *
 * @constructor
 * @this {DataSetViewer}
 * @param {String} containerId The container where the DataSetViewer will be atached.
 * @param {String} profile Global configuration.
 * @param {Sample} sample The sample where to check for the data.
 * @param {ServerFacade} serverFacade Point of contact to make calls to the server
 * @param {String} datastoreDownloadURL The datastore url in format http://localhost:8889/datastore_server.
 * @param {Map} datasets API result with the datasets to show.
 * @param {Boolean} enableUpload If true, the button to create datasets is shown, this will require the sample to be present.
 * @param {Boolean} enableOpenDataset If true, pressing on a row opens the dataset form on view mode for the given dataset.
 */
function DataSetViewerController(containerId, profile, sample, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableOpenDataset) {
	this._datasetViewerModel = new DataSetViewerModel(containerId, profile, sample, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableOpenDataset);
	this._datasetViewerView = new DataSetViewerView(this, this._datasetViewerModel);
	
	this._isPreviewable = function(file) {
		if(!file.isDirectory) {
			var haveExtension = file.pathInDataSet.lastIndexOf(".");
			if( haveExtension !== -1 && (haveExtension + 1 < file.pathInDataSet.length)) {
				var extension = file.pathInDataSet.substring(haveExtension + 1, file.pathInDataSet.length).toLowerCase();
				
				return 	extension === "svg" || 
						extension === "jpg" || extension === "jpeg" ||
						extension === "png" ||
						extension === "gif" ||
						extension === "html" ||
						extension === "pdf";
			}
		}
		return false;
	}
	
	this.init = function() {
		// Loading the datasets
		if(this._datasetViewerModel.datasets) {
			this.updateDatasets(this._datasetViewerModel.datasets);
			
			this._datasetViewerView.repaintDatasets();
			if(!this._datasetViewerModel.enableOpenDataset && this._datasetViewerModel.datasets.length === 1) {
				this._datasetViewerView.updateDirectoryView(datasets[0].code, "/");
			}
		} else {
			var _this = this;
			this.serverFacade.listDataSetsForSample(this.sample, true, function(datasets) {
				_this.updateDatasets(datasets.result);
				
				_this._datasetViewerView.repaintDatasets();
				if(!_this._datasetViewerModel.enableOpenDataset && _this._datasetViewerModel.datasets.length === 1) {
					_this._datasetViewerView.updateDirectoryView(datasets[0].code, "/");
				}
			});
		}
	}
	
	this.updateDatasets = function(datasets) {
		for(var i = 0; i < datasets.length; i++) { //DataSets for sample
			var dataset = datasets[i];
			this._datasetViewerModel.sampleDataSets[dataset.code] = dataset;
		}
	}
	
	this._repaintTestsPassed = function($container) {
		//
		// No data store URL
		//
		if(this._datasetViewerModel.datastoreDownloadURL === null) {
			$container.append("<br>");
			$container.append($("<p>")
					.append($("<span>", { class: "glyphicon glyphicon-ban-circle" }))
					.append(" Please configure properly your DSS server properly, looks like is not reachable."));
			return false;
		}
		
		//
		// Don't paint data sets for entities that don't have
		//
		var numberOfDatasets = 0;
		for(var datasetCode in this._datasetViewerModel.sampleDataSets) {
			numberOfDatasets++;
		}
		
		if(numberOfDatasets === 0) {
			$container.append("<br>");
			$container.append($("<p>")
								.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
								.append(" No datasets found."));
			return false;
		}
		
		return true;
	}
	
}