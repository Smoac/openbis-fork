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
	this.profile = profile;
	this.containerIdTitle = containerId + "-title";
	this.containerIdContent = containerId + "-content";
	this.serverFacade = serverFacade;
	this.sample = sample;
	this.datasets = datasets;
	this.enableUpload = enableUpload;
	this.enableOpenDataset = enableOpenDataset;
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
				
				return 	extension === "svg" ||
						extension === "jpg" || extension === "jpeg" ||
						extension === "png" ||
						extension === "gif";
			}
		}
		return false;
	}
	
	this._init = function(datasets) {
		//
		// Loading Message
		//
		var $container = $("#"+this.containerId);
		$container.empty();
		
		var $containerTitle = $("<div>", {"id" : this.containerIdTitle });
		$container.append($containerTitle);
		$container.append($("<div>", {"id" : this.containerIdContent }));
		
		$containerTitle.append($("<legend>").html("Files"));
		$containerTitle.append($("<p>")
							.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
							.append(" Loading datasets."));
		
		//
		//
		//
		var localReference = this;
		var listFilesCallList = [];
		
		var callback = function() { //Just enqueues the next call
			var getCall = listFilesCallList.pop();
			if(getCall) {
				getCall(callback);
			} else {
				//Switch Title
				$containerTitle.empty();
				
				//Upload Button
				var $uploadButton = "";
				if(enableUpload) {
					$uploadButton = $("<a>", { class: "btn btn-default" }).append($("<span>", { class: "glyphicon glyphicon-upload" })).append(" Upload");
					$uploadButton.click(function() { 
						mainController.changeView('showCreateDataSetPageFromPermId',localReference.sample.permId); //TO-DO Fix Global Access
					});
				}
				
				$containerTitle.append($("<legend>").append("Files ").append($uploadButton));
				
				//Switch
				$containerTitle.append(localReference._getSwitch());				
				
				//Repaint
				localReference.repaintFiles();
			}
		}
		
		for(var i = 0; i < datasets.length; i++) { //DataSets for sample
			var dataset = datasets[i];
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
	}
	
	this.init = function() {
		//
		// Loading the datasets
		//
		if(datasets) {
			this._init(datasets);
		} else {
			var localReference = this;
			this.serverFacade.listDataSetsForSample(this.sample, true, function(datasets) {
				localReference._init(datasets.result);
			});
		}
	}
	
	this._getSwitch = function() {
		var _this = this;
		var $switch = $("<div>", {"class" : "switch-toggle well", "style" : "width:80%; margin-left: auto; margin-right: auto; min-height: 38px !important;"});
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
			.append($("<input>", {"value" : "fileMode", "id" : "fileMode","name" : "dataSetVieweMode", "type" : "radio", "checked" : ""}))
			.append($("<label>", {"for" : "fileMode", "onclick" : "", "style" : "padding-top:3px;"}).append("Files"))
			.append($("<input>", {"value" : "imageMode", "id" : "imageMode", "name" : "dataSetVieweMode", "type" : "radio"}))
			.append($("<label>", {"for" : "imageMode", "onclick" : "", "style" : "padding-top:3px;"}).append("Image Previews"));

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
	this.repaintImages = function() {
		var $container = $("#"+this.containerIdContent);
		$container.empty();
		
		if(!this._repaintTestsPassed($container)) {
			return;
		}
		
		for(var datasetCode in this.sampleDataSets) {
			var dataset = this.sampleDataSets[datasetCode];
			var datasetFiles = this.sampleDataSetsFiles[datasetCode];
			
			if(!datasetFiles) {
				$container.append($("<p>")
						.append($("<span>", { class: "glyphicon glyphicon-ban-circle" }))
						.append(" Please configure properly trusted-cross-origin-domains for this web app, datasets can't be retrieved from the DSS server."));
				return;
			}
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
						var $image = null;
						var isSEQSVG = null;
						var srcPath = _this.datastoreDownloadURL + '/' + dataset.code + "/" + file.pathInDataSet + "?sessionID=" + _this.serverFacade.getSession();
						
						if(dataset.dataSetTypeCode === "SEQ_FILE" && file.pathInDataSet.toLowerCase().endsWith(".svg")) {
							$image = $("<span>", { "class" : "zoomableImage", "style" : "width:300px; height:300px;" });
							isSEQSVG = true;
							var svgLoad = function(srcPath, $imageBox) {
								d3.xml(srcPath, "image/svg+xml", function(xml) {
									var imageSvg = document.importNode(xml.documentElement, true);
									d3.select(imageSvg)
										.attr("width", 300)
										.attr("height", 300)
										.attr("viewBox", "200 200 650 650");
									$imageBox.append($(imageSvg));
								});
							}
							svgLoad(srcPath, $image);
						} else {
							$image = $("<img>", {"class" : "zoomableImage", "style" : "width:300px", "src" : srcPath });
							isSEQSVG = false;
						}
						
						$image.css({
							"margin-right" : "10px"
						});
						
						var clickFunc = function(path, isSEQSVGFix) {
							return function() {
								Util.showImage(path, isSEQSVGFix);
							};
						}
						
						$image.click(clickFunc(srcPath, isSEQSVG));
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
		
		if(!this._repaintTestsPassed($container)) {
			return;
		}
		
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
//					.append($("<th>").html("Code"))
					.append($("<th>", { "style" : "width: 35%;"}).html("Type"))
					.append($("<th>").html("Name"))
					.append($("<th>", { "style" : "width: 15%;"}).html("Size (MB)"))
//					.append($("<th>").html("Preview"))
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
				
				var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), "showViewDataSetPageFromPermId", datasetCode);
				var $datasetLink = $("<a>", {"class" : "browser-compatible-javascript-link", "href" : href }).append(dataset.code);
				var $tableRow = $("<tr>")
//									.append($("<td>").html($datasetLink))
									.append($("<td>").html(dataset.dataSetTypeCode));
				
				var downloadUrl = datastoreDownloadURL + '/' + dataset.code + "/" + encodeURIComponent(datasetFiles[i].pathInDataSet) + "?sessionID=" + this.serverFacade.getSession();
				if(datasetFiles[i].isDirectory) {
					$tableRow.append($("<td>").html(datasetFiles[i].pathInDataSet));
					$tableRow.append($("<td>"));
				} else {
					$tableRow.append(
								$("<td>").append(
									$("<a>")
											.attr("href", downloadUrl)
											.attr("download", 'download')
											.html(datasetFiles[i].pathInDataSet)
											.click(function(event) {
												event.stopPropagation();
											})
								)
							);
					
					var sizeInMb = parseInt(datasetFiles[i].fileSize) / 1024 / 1024;
					var sizeInMbThreeDecimals = Math.floor(sizeInMb * 1000) / 1000;
					$tableRow.append($("<td>").html(sizeInMbThreeDecimals));
				}
				 
//				if(this._isPreviewable(datasetFiles[i])) {
//					$tableRow.append($("<td>").append(
//												$("<a>")
//													.attr("href", downloadUrl)
//													.attr("target", "_blank")
//													.append($("<span>").attr("class", "glyphicon glyphicon-search"))
//													.click(function(event) {
//														event.stopPropagation();
//													})
//											)
//									);
//				} else {
//					$tableRow.append($("<td>"));
//				}
				
				//Open DataSet
				if(this.enableOpenDataset) {
					$tableRow.attr('style', 'cursor: pointer;');
					
					var clickFunction = function(datasetCode) {
						return function(event) {
							mainController.changeView('showViewDataSetPageFromPermId', datasetCode);
						};
					}
					$tableRow.click(clickFunction(dataset.code));
				}
				
				$dataSetsTableBody.append($tableRow);
			}
		}
		
		$dataSetsTable.append($dataSetsTableBody);
		$container.append($dataSetsTable);
	}
}