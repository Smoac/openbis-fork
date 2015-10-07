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

function DataSetFormController() {
	var container = null;
	var sampleOrExperimentCopy = null;
	var dataSetFormModel = null;
	var dataSetFormView = null;
	
	this.init = function($container, sampleOrExperiment) {
		container = $container;
		sampleOrExperimentCopy = $.extend({}, sampleOrExperiment);
		dataSetFormModel = new DataSetFormModel(sampleOrExperimentCopy);
		dataSetFormView = new DataSetFormView(this, dataSetFormModel);
		
		var _this = this;
		openBIS.listDataStores(function(datastoresData) {
			dataSetFormModel.dataStores = datastoresData.result;
			openBIS.listDataSetTypes(
					function(datasetsData) {
						dataSetFormModel.dataSetTypes = datasetsData.result;
						dataSetFormView.repaint($container);
					}
			);
		});
	}
	
	//
	// Form Submit
	//
	this.submit = function() {
		Util.blockUI();
		var _this = this;
		var metadata = dataSetFormModel.dataSet.properties;
			
		var isZipDirectoryUpload = $("#isZipDirectoryUpload"+":checked").val() === "on";
		
		var folderName = $('#folderName').val();
		if(!folderName) {
			folderName = 'DEFAULT';
		}
		
		var method = "insertDataSet";
		var dataSetTypeCode = $('#DATASET_TYPE').val();
		
		
		var parameters = {
				//API Method
				"sessionToken" : openBIS.getSession(),
				"method" : method,
				//Identification Info
				"dataSetType" : dataSetTypeCode,
				"filenames" : dataSetFormModel.files,
				"folderName" : folderName,
				"isZipDirectoryUpload" : isZipDirectoryUpload,
				//Metadata
				"metadata" : metadata,
				//For Moving files
				"sessionID" : openBIS.getSession(),
				"openBISURL" : openBIS._internal.openbisUrl
		};
		
		var sampleOrExperimentIdentifier = dataSetFormModel.sampleOrExperiment.identifier;
		if(sampleOrExperimentIdentifier.split("/").length === 3) {
			parameters["sampleIdentifier"] = sampleOrExperimentIdentifier;
		} else if(sampleOrExperimentIdentifier.split("/").length === 4) {
			parameters["experimentIdentifier"] = sampleOrExperimentIdentifier;
		}
			
		if(dataSetFormModel.dataStores.length > 0) {
			openBIS.createReportFromAggregationService(dataSetFormModel.dataStores[0].code, "dataset-uploader-api", parameters, function(response) {
				if(response.error) { //Error Case 1
					Util.showError(response.error.message, function() {Util.unblockUI();});
				} else if (response.result.columns[1].title === "Error") { //Error Case 2
					var stacktrace = response.result.rows[0][1].value;
					var isUserFailureException = stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") === 0;
					var startIndex = null;
					var endIndex = null;
					if(isUserFailureException) {
						startIndex = "ch.systemsx.cisd.common.exceptions.UserFailureException".length + 2;
						endIndex = stacktrace.indexOf("at ch.systemsx");
					} else {
						startIndex = 0;
						endIndex = stacktrace.length;
					}
					var errorMessage = "Please retry, if the problem persists contact your admin: " + stacktrace.substring(startIndex, endIndex).trim();
					Util.showError(errorMessage, function() {
						Util.unblockUI();
						_this.init(container, sampleOrExperimentCopy);
					});
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					Util.showSuccess("DataSet Created.", function() {
						Util.unblockUI();
						_this.init(container, sampleOrExperimentCopy);
					});
					
				} else { //This should never happen
					Util.showError("Please retry, if the problem persists contact your admin: Unknown Error.", function() {
						Util.unblockUI();
						_this.init(container, sampleOrExperimentCopy);
					});
				}
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}