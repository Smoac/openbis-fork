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

var DataSetViewerMode = {
    LIST : 0,
    TREE : 1
}

function DataSetViewerModel(containerId, profile, sample, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableOpenDataset) {
	this.containerId = containerId;
	this.containerIdTitle = containerId + "-title";
	this.containerIdContent = containerId + "-content";
	
	this.profile = profile;
	this.serverFacade = serverFacade;
	
	this.sample = sample;
	this.datasets = datasets;
	
	this.enableUpload = enableUpload;
	this.enableOpenDataset = enableOpenDataset;
	this.sampleDataSets = {};
	this.datastoreDownloadURL = datastoreDownloadURL
	this.lastUsedPath = [];
	
	this.dataSetViewerMode = DataSetViewerMode.LIST;
	
	this.getDownloadLink = function(datasetCode, datasetFile, isShowSize) {
		var downloadUrl = this.datastoreDownloadURL + '/' + datasetCode + "/" + encodeURIComponent(datasetFile.pathInDataSet) + "?sessionID=" + mainController.serverFacade.getSession();
		
		var sizeInMb = parseInt(datasetFile.fileSize) / 1024 / 1024;
		var sizeInMbThreeDecimals = Math.floor(sizeInMb * 1000) / 1000;
		var size = null;
		var unit = null;
		if(sizeInMbThreeDecimals < 1) {
			size = sizeInMbThreeDecimals * 1000;
			unit = "Kb";
		} else {
			size = sizeInMbThreeDecimals;
			unit = "Mb";
		}
		
		var $link = $("<a>").attr("href", downloadUrl)
							.attr("target", "_blank")
							.append(datasetFile.pathInListing.replace("_", "%20"))
							.append(" ("+ size + unit +")")
							.click(function(event) {
								event.stopPropagation();
							});
		
		return $link;
	}
	
}