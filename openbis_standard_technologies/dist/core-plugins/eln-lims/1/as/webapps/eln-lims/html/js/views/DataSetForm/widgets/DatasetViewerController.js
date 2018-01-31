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
 */
function DataSetViewerController(containerId, profile, entity, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableDeepUnfolding) {
	this._datasetViewerModel = new DataSetViewerModel(containerId, profile, entity, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableDeepUnfolding);
	this._datasetViewerView = new DataSetViewerView(this, this._datasetViewerModel);
	
	this.init = function() {
		var _this = this;
		// Loading the datasets
		if(this._datasetViewerModel.datasets) {
			if(this._datasetViewerModel.datasets.length > 0) {
				this.updateDatasets(this._datasetViewerModel.datasets);
			}
		} else {
			var _this = this;
			if (this._datasetViewerModel.isExperiment()) {
				serverFacade.listExperimentsForIdentifiers([this._datasetViewerModel.entity.identifier.identifier], function(data) {
					serverFacade.listDataSetsForExperiment(data.result[0], function(datasets) {
						var results;
						if(_this._datasetViewerModel.isExperiment()) { //Filter out datasets own by samples
							results = [];
							for(var dIdx = 0; dIdx < datasets.result.length; dIdx++) {
								var dataset = datasets.result[dIdx];
								if(!dataset.sampleIdentifierOrNull) {
									results.push(dataset);
								}
							}
						} else {
							results = datasets.result;
						}
						
						if(results.length > 0) {
							_this.updateDatasets(results);
						}
					});
				});
			} else {
				serverFacade.listDataSetsForSample(this._datasetViewerModel.entity, true, function(datasets) {
					if(datasets.result.length > 0) {
						_this.updateDatasets(datasets.result);
					}
				});
			}
		}
	}
	
	this.updateDatasets = function(datasets) {
		var _this = this;
		var datasetPermIds = [];
		
		_this._datasetViewerModel.datasets = datasets; //In case they are loaded after the model is already created.
		
		for(var i = 0; i < datasets.length; i++) { //DataSets for entity
			var dataset = datasets[i];
			this._datasetViewerModel.entityDataSets[dataset.code] = dataset;
			datasetPermIds.push(dataset.code);
		}
		
		require([ "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/fetchoptions/DataSetFetchOptions" ],
				function(DataSetPermId, DataSetFetchOptions) {
					var ids = [];
					for(var dIdx = 0; dIdx < datasetPermIds.length; dIdx++) {
						var id = new DataSetPermId(datasetPermIds[dIdx]);
						ids.push(id);
					}
		            var fetchOptions = new DataSetFetchOptions();
		            fetchOptions.withLinkedData().withExternalDms();
		            mainController.openbisV3.getDataSets(ids, fetchOptions).done(function(map) {
		            	for(var dIdx = 0; dIdx < datasetPermIds.length; dIdx++) {
							_this._datasetViewerModel.v3Datasets.push(map[datasetPermIds[dIdx]]);
						}
		            	_this._datasetViewerView.repaintDatasets();
		            });
		});
		
		
	}
	
}