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
 */
function DataSetViewer(containerId, profile, sample, serverFacade, datastoreDownloadURL) {
	this.containerId = containerId;
	this.profile = profile;
	this.containerIdTitle = containerId + "-title";
	this.containerIdContent = containerId + "-content";
	this.serverFacade = serverFacade;
	this.sample = sample;
	this.sampleDataSets = {};
	this.sampleDataSetsFiles = {};
	this.datastoreDownloadURL = datastoreDownloadURL
	
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
	
	this._isImage = function(file) {
		if(!file.isDirectory) {
			var haveExtension = file.pathInDataSet.lastIndexOf(".");
			if( haveExtension !== -1 && (haveExtension + 1 < file.pathInDataSet.length)) {
				var extension = file.pathInDataSet.substring(haveExtension + 1, file.pathInDataSet.length).toLowerCase();
				
				return 	extension === "jpg" || extension === "jpeg" ||
						extension === "png" ||
						extension === "gif";
			}
		}
		return false;
	}
	
	this.init = function() {
		//
		// Loading Message
		//
		var $container = $("#"+this.containerId);
		$container.empty();
		
		var $containerTitle = $("<div>", {"id" : this.containerIdTitle });
		$container.append($containerTitle);
		$container.append($("<div>", {"id" : this.containerIdContent }));
		
		$containerTitle.append($("<legend>").html("Data Sets"));
		$containerTitle.append($("<p>")
							.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
							.append(" Loading datasets."));
		//
		// Loading the datasets
		//
		var cleanSample = $.extend({}, this.sample);
		delete cleanSample.parents;
		delete cleanSample.children; 
		
		var localReference = this;
		this.serverFacade.listDataSetsForSample(cleanSample, true, function(datasets) {
			var listFilesCallList = [];
			
			var callback = function() { //Just enqueues the next call
				var getCall = listFilesCallList.pop();
				if(getCall) {
					getCall(callback);
				} else {
					//Switch Title
					$containerTitle.empty();
					
					//Upload Button
					var $uploadButton = $("<a>", { class: "btn btn-default" }).append($("<span>", { class: "glyphicon glyphicon-upload" }));
					$uploadButton.click(function() { 
						mainController.changeView('showCreateDataSetPage',localReference.sample); //TO-DO Fix Global Access
					} );
					
					$containerTitle.append($("<legend>").append("Data Sets ").append($uploadButton));
					
					//Switch
					$containerTitle.append(localReference._getSwitch());				
					
					//Repaint
					localReference.repaintImages();
				}
			}
			
			for(var i = 0; i < datasets.result.length; i++) { //DataSets for sample
				var dataset = datasets.result[i];
				var listFilesForDataSet = function(dataset){ return function() { //Files in dataset
					localReference.serverFacade.listFilesForDataSet(dataset.code, "/", true, function(files) {
						localReference.sampleDataSets[dataset.code] = dataset;
						localReference.sampleDataSetsFiles[dataset.code] = files.result;
						callback();
					});
				}}	
				listFilesCallList.push(listFilesForDataSet(dataset));
			}
			
			callback();
		});
	}
	
	this._getSwitch = function() {
		var _this = this;
		var $switch = $("<div>", {"class" : "switch-toggle well", "style" : "width:33%; margin-left: auto; margin-right: auto; min-height: 38px !important;"});
		$switch.change(function(event) {
			var mode = $('input[name=dataSetVieweMode]:checked').val();
			switch(mode) {
				case "imageMode":
					_this.repaintImages();
					break;
				case "fileMode":
					_this.repaintFiles();
					break;
			}
		});
		
		$switch
			.append($("<input>", {"value" : "imageMode", "id" : "imageMode", "name" : "dataSetVieweMode", "type" : "radio", "checked" : ""}))
			.append($("<label>", {"for" : "imageMode", "onclick" : "", "style" : "padding-top:3px;"}).append("Images"))
			.append($("<input>", {"value" : "fileMode", "id" : "fileMode","name" : "dataSetVieweMode", "type" : "radio"}))
			.append($("<label>", {"for" : "fileMode", "onclick" : "", "style" : "padding-top:3px;"}).append("Files"));

		$switch.append($("<a>", {"class" : "btn btn-primary"}));
		return $switch;
	}
	
	this._isDisplayed = function(dataSetTypeCode, fileName) {
		var passes = false;
		this.profile.dataSetViewerConf["DATA_SET_TYPES"].forEach(function(type) {
			var datasetTypePattern = new RegExp(type, "")
			passes = passes || datasetTypePattern.test(dataSetTypeCode);
		});
		
		if (!passes) {
			return false;
		}
		
		passes = false;
		this.profile.dataSetViewerConf["FILE_NAMES"].forEach(function(name) {
			var fileNamePattern = new RegExp(name, "")
			passes = passes || fileNamePattern.test(fileName);
		});
		
		return passes;
	}
	
	this.repaintImages = function() {
		var $container = $("#"+this.containerIdContent);
		$container.empty();
		
		//
		// No data store URL
		//
		if(datastoreDownloadURL === null) {
			$container.append($("<p>")
					.append($("<span>", { class: "glyphicon glyphicon-ban-circle" }))
					.append(" Please configure properly your DSS server properly, looks like is not reachable."));
			return;
		}
		
		//
		_this = this;
		var maxImages = 30;
		var numImages = 0;
		for(var datasetCode in this.sampleDataSets) {
			var dataset = this.sampleDataSets[datasetCode];
			var datasetFiles = this.sampleDataSetsFiles[datasetCode];
			
			datasetFiles.forEach(
				function(file) {
					if (numImages < maxImages && _this._isImage(file) &&  _this._isDisplayed(dataset.dataSetTypeCode, file.pathInDataSet)) {
						var $image = $("<img>", {"class" : "zoomableImage", "style" : "width:300px", "src" : _this.datastoreDownloadURL + '/' + dataset.code + "/" + file.pathInDataSet + "?sessionID=" + _this.serverFacade.getSession()});
						$image.css({
							"margin-right" : "10px"
						});
						$image.click(function() {
							Util.showImage($image.attr("src"));
						});
						$container.append($image);
						numImages++
					}
			});
		}
		
		if(numImages === maxImages) {
			$container.append($("<p>")
					.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
					.append(" You can't see more than " + maxImages + " image at the same time, please use the file browser mode."));
		}
	}
	
	this.repaintFiles = function() {
		var $container = $("#"+this.containerIdContent);
		$container.empty();
		
		//
		// No data store URL
		//
		if(datastoreDownloadURL === null) {
			$container.append($("<p>")
					.append($("<span>", { class: "glyphicon glyphicon-ban-circle" }))
					.append(" Please configure properly your DSS server properly, looks like is not reachable."));
			return;
		}
		
		//
		// Don't paint data sets for entities that don't have
		//
		var numberOfDatasets = 0;
		for(var datasetCode in this.sampleDataSets) {
			numberOfDatasets++;
		}
		
		if(numberOfDatasets === 0) {
			$container.append($("<p>")
								.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
								.append(" No datasets found."));
			return;
		}
		
		//
		// Simple Files Table
		//
		var $dataSetsTable = $("<table>", { class: "table"});
		$dataSetsTable.append(
			$("<thead>").append(
				$("<tr>")
					.append($("<th>").html("DataSet Code"))
					.append($("<th>").html("DataSet Type"))
					.append($("<th>").html("File Name"))
					.append($("<th>").html("File Size (Mbyte)"))
					.append($("<th>").html("Preview"))
			)
		);
		
		var $dataSetsTableBody = $("<tbody>");
		
		for(var datasetCode in this.sampleDataSets) {
			var dataset = this.sampleDataSets[datasetCode];
			var datasetFiles = this.sampleDataSetsFiles[datasetCode];
			
			if(!datasetFiles) {
				$container.append($("<p>")
						.append($("<span>", { class: "glyphicon glyphicon-ban-circle" }))
						.append(" Please configure properly trusted-cross-origin-domains for this web app, datasets can't be retrieved from the DSS server."));
				return;
			}
			
			for(var i = 0; i < datasetFiles.length; i++) {
				var $tableRow = $("<tr>")
									.append($("<td>").html(dataset.code))
									.append($("<td>").html(dataset.dataSetTypeCode));
				
				var downloadUrl = datastoreDownloadURL + '/' + dataset.code + "/" + encodeURIComponent(datasetFiles[i].pathInDataSet) + "?sessionID=" + this.serverFacade.getSession();
				if(datasetFiles[i].isDirectory) {
					$tableRow.append($("<td>").html(datasetFiles[i].pathInDataSet));
					$tableRow.append($("<td>"));
				} else {
					$tableRow.append(
								$("<td>").append(
									$("<a>").attr("href", downloadUrl)
											.attr("download", 'download')
											.html(datasetFiles[i].pathInDataSet)
								)
							);
					
					var sizeInMb = parseInt(datasetFiles[i].fileSize) / 1024 / 1024;
					var sizeInMbThreeDecimals = Math.floor(sizeInMb * 1000) / 1000;
					$tableRow.append($("<td>").html(sizeInMbThreeDecimals));
				}
				 
				if(this._isPreviewable(datasetFiles[i])) {
					$tableRow.append($("<td>").append(
												$("<a>")
													.attr("href", downloadUrl)
													.attr("target", "_blank")
													.append($("<span>").attr("class", "glyphicon glyphicon-search"))
											)
									);
				} else {
					$tableRow.append($("<td>"));
				}
				
				$dataSetsTableBody.append($tableRow);
			}
		}
		
		$dataSetsTable.append($dataSetsTableBody);
		$container.append($dataSetsTable);
	}
}