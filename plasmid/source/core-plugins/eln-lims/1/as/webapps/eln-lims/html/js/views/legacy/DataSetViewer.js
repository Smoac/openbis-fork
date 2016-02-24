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
function DataSetViewer(containerId, profile, sample, serverFacade, datastoreDownloadURL, datasets, enableUpload, enableOpenDataset) {
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
		if(datasets) {
			this.updateDatasets(datasets);
			this.repaintDatasets();
		} else {
			var _this = this;
			this.serverFacade.listDataSetsForSample(this.sample, true, function(datasets) {
				_this.updateDatasets(datasets.result);
				_this.repaintDatasets();
			});
		}
	}
	
	this.updateDatasets = function(datasets) {
		for(var i = 0; i < datasets.length; i++) { //DataSets for sample
			var dataset = datasets[i];
			this.sampleDataSets[dataset.code] = dataset;
		}
	}
	
	this._repaintTestsPassed = function($container) {
		//
		// No data store URL
		//
		if(datastoreDownloadURL === null) {
			$container.append($("<p>")
					.append($("<span>", { class: "glyphicon glyphicon-ban-circle" }))
					.append(" Please configure properly your DSS server properly, looks like is not reachable."));
			return false;
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
			return false;
		}
		
		return true;
	}
	
	this.lastUsedPath = [];
	this.updateDirectoryView = function(code, path, isBack) {
		var _this = this;
		this.serverFacade.listFilesForDataSet(code, path, false, function(files) {
			if(!isBack) {
				_this.lastUsedPath.push(path);
			}
			_this.repaintFiles(code, files.result);
		});
	}
	
	this.repaintDatasets = function() {
		var _this = this;
		
		//
		// Container
		//
		var $containerTitle = $("<div>", {"id" : this.containerIdTitle });
		var $containerContent = $("<div>", {"id" : this.containerIdContent });
		
		
		//
		// Upload Button
		//
		var $uploadButton = "";
		if(this.enableUpload) {
			$uploadButton = $("<a>", { class: "btn btn-default" }).append($("<span>", { class: "glyphicon glyphicon-upload" })).append(" Upload New Dataset");
			$uploadButton.click(function() { 
				mainController.changeView('showCreateDataSetPageFromPermId',_this.sample.permId); //TO-DO Fix Global Access
			});
		}
		
		$containerTitle.append($("<div>").append($uploadButton));
		
		//
		// Tests
		//
		if(!this._repaintTestsPassed($containerContent)) {
			return;
		}
		
		//
		// Simple Datasets Table
		//
		var tableClass = "table";
		if(this.enableOpenDataset) {
			tableClass += " table-hover";
		}
		
		var $dataSetsTable = $("<table>", { class: tableClass });
		$dataSetsTable.append($("<thead>").append($("<tr>")
					.append($("<th>", { "style" : "width: 35%;"}).html("Type"))
					.append($("<th>", { "style" : "width: 50%;"}).html("Code"))
					.append($("<th>", { "style" : "width: 15%;"}).html("Operations"))));
		var tbody = $("<tbody>");
		$dataSetsTable.append(tbody);
		
		for(var datasetCode in this.sampleDataSets) {
			var dataset = this.sampleDataSets[datasetCode];
			var getDatasetLinkEvent = function(code) {
				return function(event) {
					_this.updateDirectoryView(code, "/");
					event.stopPropagation();
				};
			}
			var $datasetLink = $("<a>").text(dataset.code).click(getDatasetLinkEvent(dataset.code));
			
			var datasetFormClick = function(datasetCode) {
				return function(event) {
					mainController.changeView('showViewDataSetPageFromPermId', datasetCode);
					event.stopPropagation();
				};
			}
			
			var $datasetFormClickBtn = "";
			
			if(this.enableOpenDataset) {
				$datasetFormClickBtn = $("<a>").append($("<span>").attr("class", "glyphicon glyphicon-search")).click(datasetFormClick(dataset.code));
			}
			
			var tRow = $("<tr>")
					.append($("<td>").html(dataset.dataSetTypeCode))
					.append($("<td>").append($datasetLink))
					.append($("<td>").append($datasetFormClickBtn));
			
			tRow.click(getDatasetLinkEvent(dataset.code));
			tbody.append(tRow);
			
		}
		
		$containerContent.append($("<legend>").append("Datasets:"));
		$containerContent.append($dataSetsTable);
		
		//
		//
		//
		var $mainContainer = $("#"+this.containerId);
		$mainContainer.empty();
		$mainContainer.append($containerTitle).append($containerContent);
	}
	
	this.repaintFiles = function(datasetCode, datasetFiles) {
		var _this = this;
		var parentPath = this.lastUsedPath[this.lastUsedPath.length - 1];
		var $container = $("#"+this.containerIdContent);
		$container.empty();
		
		
		// Path
		$container.append($("<legend>").append("Path: " + parentPath));
		
		//
		// Simple Files Table
		//
		var tableClass = "table";
		if(this.enableOpenDataset) {
			tableClass += " table-hover";
		}
		var $dataSetsTable = $("<table>", { class: tableClass });
		$dataSetsTable.append(
			$("<thead>").append(
				$("<tr>")
					.append($("<th>", { "style" : "width: 70%;"}).html("Name"))
					.append($("<th>", { "style" : "width: 15%;"}).html("Size (MB)"))
					.append($("<th>", { "style" : "width: 15%;"}).html("Operations"))
			)
		);
		
		var $dataSetsTableBody = $("<tbody>");
		//
		// Back
		//
		var $directoryLink = $("<a>").text("..").click(function(event) {
			_this.updateDirectoryView(datasetCode, parent);
			event.stopPropagation();
		});
		
		var backClick = function(event) {
			if(_this.lastUsedPath.length === 1) {
				_this.repaintDatasets();
			} else {
				_this.updateDirectoryView(datasetCode, _this.lastUsedPath[_this.lastUsedPath.length - 2], true);
			}
			_this.lastUsedPath.pop();
			event.stopPropagation();
		};
		
		var $tableRow = $("<tr>")
							.append($("<td>").append($("<a>").text("..").click(backClick)))
							.append($("<td>"))
							.append($("<td>"));
			$tableRow.click(backClick);
		$dataSetsTableBody.append($tableRow);
		
		//
		// Files/Directories
		//
		var dataset = this.sampleDataSets[datasetCode];
		for(var i = 0; i < datasetFiles.length; i++) {
			var $tableRow = $("<tr>");
			var downloadUrl = datastoreDownloadURL + '/' + datasetCode + "/" + encodeURIComponent(datasetFiles[i].pathInDataSet) + "?sessionID=" + this.serverFacade.getSession();
			var pathInDatasetDisplayName = "";
			var lastSlash = datasetFiles[i].pathInDataSet.lastIndexOf("/");
			if(lastSlash !== -1) {
				pathInDatasetDisplayName = datasetFiles[i].pathInDataSet.substring(lastSlash + 1);
			} else {
				pathInDatasetDisplayName = datasetFiles[i].pathInDataSet;
			}
			
			
			if(datasetFiles[i].isDirectory) {
				var getDirectoyClickFuncion = function(datasetCode, pathInDataSet) {
					return function() {
						_this.updateDirectoryView(datasetCode, pathInDataSet);
					};
				};
				
				var dirFunc = getDirectoyClickFuncion(datasetCode, datasetFiles[i].pathInDataSet);
				var $directoryLink = $("<a>").text("/" + pathInDatasetDisplayName)
											.click(function(event) {
												dirFunc();
												event.stopPropagation();
											});
				
				$tableRow.append($("<td>").append($directoryLink)).append($("<td>")).append($("<td>"));
				$tableRow.click(dirFunc);
			} else {
				$tableRow.append($("<td>").append($("<p>").text(pathInDatasetDisplayName)));
				
				var sizeInMb = parseInt(datasetFiles[i].fileSize) / 1024 / 1024;
				var sizeInMbThreeDecimals = Math.floor(sizeInMb * 1000) / 1000;
				$tableRow.append($("<td>").html(sizeInMbThreeDecimals));
				
				var $previewBtn = $("<a>").attr("href", downloadUrl)
				.attr("target", "_blank")
				.append($("<span>").attr("class", "glyphicon glyphicon-search"))
				.click(function(event) {
					event.stopPropagation();
				});
				
				var $downloadBtn = $("<a>").attr("href", downloadUrl)
				.attr("download", 'download')
				.append($("<span>").attr("class", "glyphicon glyphicon-download"))
				.click(function(event) {
					event.stopPropagation();
				});

				if(this._isPreviewable(datasetFiles[i])) {
					$tableRow.append($("<td>").append($previewBtn).append($downloadBtn));
				} else {
					$tableRow.append($("<td>").append($downloadBtn));
				}
			}
			
			$dataSetsTableBody.append($tableRow);
		}
		
		$dataSetsTable.append($dataSetsTableBody);
		$container.append($dataSetsTable);
	}
}