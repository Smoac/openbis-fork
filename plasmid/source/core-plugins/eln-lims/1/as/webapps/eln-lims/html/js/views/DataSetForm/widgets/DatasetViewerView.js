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

function DataSetViewerView(dataSetViewerController, dataSetViewerModel) {
	this._dataSetViewerController = dataSetViewerController;
	this._dataSetViewerModel = dataSetViewerModel;
	
	this.updateDirectoryView = function(code, path, isBack) {
		var _this = this;
		mainController.serverFacade.listFilesForDataSet(code, path, false, function(files) {
			if(!isBack) {
				_this._dataSetViewerModel.lastUsedPath.push(path);
			}
			_this.repaintFiles(code, files.result);
		});
	}
	
	this.repaintDatasets = function() {
		var _this = this;
		
		//
		// Container
		//
		var $containerTitle = $("<div>", {"id" : this._dataSetViewerModel.containerIdTitle });
		var $containerContent = $("<div>", {"id" : this._dataSetViewerModel.containerIdContent });
		
		
		//
		// Upload Button
		//
		var $uploadButton = "";
		if(this._dataSetViewerModel.enableUpload) {
			$uploadButton = $("<a>", { class: "btn btn-default" }).append($("<span>", { class: "glyphicon glyphicon-upload" })).append(" Upload New Dataset");
			$uploadButton.click(function() { 
				mainController.changeView('showCreateDataSetPageFromPermId',_this._dataSetViewerModel.sample.permId); //TO-DO Fix Global Access
			});
		}
		
		$containerTitle.append($("<div>").append($uploadButton));
		
		//
		// Tests
		//
		this._dataSetViewerController._repaintTestsPassed($containerContent);
		
		//
		// Simple Datasets Table
		//
		var tableClass = "table table-hover";
		
		var $dataSetsTable = $("<table>", { class: tableClass });
		$dataSetsTable.append($("<thead>").append($("<tr>")
					.append($("<th>", { "style" : "width: 50%;"}).text("Type"))
					.append($("<th>", { "style" : "width: 50%;"}).text("Code"))));
		var tbody = $("<tbody>");
		$dataSetsTable.append(tbody);
		
		for(var datasetCode in this._dataSetViewerModel.sampleDataSets) {
			var dataset = this._dataSetViewerModel.sampleDataSets[datasetCode];
			
			var getDatasetLinkEvent = function(code) {
				return function(event) {
					_this.updateDirectoryView(code, "/");
				};
			}
			
			var $datasetFormClickBtn = "";
			
			if(this._dataSetViewerModel.enableOpenDataset) {
				var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), 'showViewDataSetPageFromPermId', datasetCode);
				$datasetFormClickBtn = $("<a>", {"href": href, "class" : "browser-compatible-javascript-link" }).append(datasetCode);
				
				var getDatasetFormClick = function(datasetCode) {
					return function(event) {
						mainController.changeView('showViewDataSetPageFromPermId', datasetCode);
					};
				}
				
				$datasetFormClickBtn.click(getDatasetFormClick(datasetCode));
			} else {
				$datasetFormClickBtn = dataset.code;
			}
			
			var tRow = $("<tr>", { "style" : "cursor:pointer;" })
					.append($("<td>").html(dataset.dataSetTypeCode))
					.append($("<td>").append($datasetFormClickBtn));
			
			tRow.click(getDatasetLinkEvent(dataset.code));
			tbody.append(tRow);
			
		}
		
		$containerContent.append($("<legend>").append("Datasets:"));
		$containerContent.append($dataSetsTable);
		
		//
		//
		//
		var $mainContainer = $("#"+this._dataSetViewerModel.containerId);
		$mainContainer.empty();
		$mainContainer.append($containerTitle).append($containerContent);
	}
	
	this.repaintFiles = function(datasetCode, datasetFiles) {
		var _this = this;
		var parentPath = this._dataSetViewerModel.lastUsedPath[this._dataSetViewerModel.lastUsedPath.length - 1];
		var $container = $("#"+this._dataSetViewerModel.containerIdContent);
		$container.empty();
		
		// Path
		$container.append($("<legend>").append("Path: " + parentPath));
		
		//
		// Simple Files Table
		//
		var tableClass = "table table-hover";
		var $dataSetsTable = $("<table>", { class: tableClass });
		$dataSetsTable.append(
			$("<thead>").append(
				$("<tr>")
					.append($("<th>", { "style" : "width: 80%;"}).html("Name"))
					.append($("<th>", { "style" : "width: 20%;"}).html("Size (MB)"))
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
			if(_this._dataSetViewerModel.lastUsedPath.length === 1) {
				_this.repaintDatasets();
			} else {
				_this.updateDirectoryView(datasetCode, _this._dataSetViewerModel.lastUsedPath[_this._dataSetViewerModel.lastUsedPath.length - 2], true);
			}
			_this._dataSetViewerModel.lastUsedPath.pop();
			event.stopPropagation();
		};
		
		var $tableRow = $("<tr>", { "style" : "cursor:pointer;" })
							.append($("<td>").append("..").click(backClick))
							.append($("<td>"));
			$tableRow.click(backClick);
		$dataSetsTableBody.append($tableRow);
		
		//
		// Files/Directories
		//
		var dataset = this._dataSetViewerModel.sampleDataSets[datasetCode];
		for(var i = 0; i < datasetFiles.length; i++) {
			var $tableRow = $("<tr>");
			var downloadUrl = this._dataSetViewerModel.datastoreDownloadURL + '/' + datasetCode + "/" + encodeURIComponent(datasetFiles[i].pathInDataSet) + "?sessionID=" + mainController.serverFacade.getSession();
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
				$tableRow.attr("style", "cursor:pointer;");
				$tableRow.append($("<td>").append("/" + pathInDatasetDisplayName)).append($("<td>"));
				$tableRow.click(dirFunc);
			} else {
				var $pathInDatasetDisplayNameWithDownload = $("<a>").attr("href", downloadUrl)
				.attr("target", "_blank")
				.append(pathInDatasetDisplayName)
				.click(function(event) {
					event.stopPropagation();
				});
				
				$tableRow.append($("<td>").append($pathInDatasetDisplayNameWithDownload));
				
				var sizeInMb = parseInt(datasetFiles[i].fileSize) / 1024 / 1024;
				var sizeInMbThreeDecimals = Math.floor(sizeInMb * 1000) / 1000;
				$tableRow.append($("<td>").html(sizeInMbThreeDecimals));
			}
			
			$dataSetsTableBody.append($tableRow);
		}
		
		$dataSetsTable.append($dataSetsTableBody);
		$container.append($dataSetsTable);
	}
}