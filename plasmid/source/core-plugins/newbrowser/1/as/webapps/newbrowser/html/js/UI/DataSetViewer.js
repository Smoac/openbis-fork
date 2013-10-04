function DataSetViewer(containerId, sample, openbisServer, datastoreDownloadURL) {
	this.containerId = containerId;
	this.openbisServer = openbisServer;
	this.sample = sample;
	this.sampleDataSets = {};
	this.sampleDataSetsFiles = {};
	
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
		//
		// Loading Message
		//
		var $container = $("#"+this.containerId);
		$container.empty();
		$container.append($("<legend>").html("DataSets"));
		$container.append($("<p>")
							.append($("<i>", { class: "icon-info-sign" }))
							.append(" Loading datasets."));
		//
		// Loading the datasets
		//
		var cleanSample = $.extend({}, this.sample);
		delete cleanSample.parents;
		delete cleanSample.children; 
		
		var localReference = this;
		this.openbisServer.listDataSetsForSample(cleanSample, true, function(datasets) {
			var listFilesCallList = [];
			
			var callback = function() { //Just enqueues the next call
				var getCall = listFilesCallList.pop();
				if(getCall) {
					getCall(callback);
				} else {
					localReference.repaint();
				}
			}
			
			for(var i = 0; i < datasets.result.length; i++) { //DataSets for sample
				var dataset = datasets.result[i];
				var listFilesForDataSet = function(dataset){ return function() { //Files in dataset
					localReference.openbisServer.listFilesForDataSet(dataset.code, "/", true, function(files) {
						localReference.sampleDataSets[dataset.dataSetTypeCode] = dataset;
						localReference.sampleDataSetsFiles[dataset.dataSetTypeCode] = files.result;
						callback();
					});
				}}	
				listFilesCallList.push(listFilesForDataSet(dataset));
			}
			
			callback();
		});
	}
	
	this.repaint = function() {
		var $container = $("#"+this.containerId);
		$container.empty();
		$container.append($("<legend>").html("DataSets"));
		
		//
		// Don't paint datasets for entities that don't have
		//
		var numberOfDatasets = 0;
		for(var datasetCode in this.sampleDataSets) {
			numberOfDatasets++;
		}
		
		if(numberOfDatasets === 0) {
			$container.append($("<p>")
								.append($("<i>", { class: "icon-info-sign" }))
								.append(" No datasets found."));
			return;
		}
		
		//
		// Simple Files Table
		//
		$dataSetsTable = $("<table>", { class: "table"});
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
		
		$dataSetsTableBody = $("<tbody>");
		
		for(var datasetCode in this.sampleDataSets) {
			var dataset = this.sampleDataSets[datasetCode];
			var datasetFiles = this.sampleDataSetsFiles[datasetCode];
			
			for(var i = 0; i < datasetFiles.length; i++) {
				var $tableRow = $("<tr>")
									.append($("<td>").html(dataset.code))
									.append($("<td>").html(dataset.dataSetTypeCode));
				
				var downloadUrl = datastoreDownloadURL + '/' + dataset.code + "/" + datasetFiles[i].pathInDataSet + "?sessionID=" + this.openbisServer.getSession();
					
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
													.append($("<i>").attr("class", "icon-search"))
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