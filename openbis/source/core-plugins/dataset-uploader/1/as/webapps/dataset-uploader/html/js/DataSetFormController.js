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
	this._container = null;
	this._sampleOrExperiment = null;
	this._dataSetFormModel = null;
	this._dataSetFormView = null;
	
	this.init = function($container, sampleOrExperiment) {
		this._container = $container;
		this._sampleOrExperiment = sampleOrExperiment;
		this._dataSetFormModel = new DataSetFormModel(sampleOrExperiment);
		this._dataSetFormView = new DataSetFormView(this, this._dataSetFormModel);
		
		var _this = this;
		openBIS.listDataStores(function(datastoresData) {
			_this._dataSetFormModel.dataStores = datastoresData.result;
			openBIS.listDataSetTypes(
					function(datasetsData) {
						_this._dataSetFormModel.dataSetTypes = datasetsData.result;
						_this._dataSetFormView.repaint($container);
					}
			);
		});
	}
	
	this._getDataSetType = function(typeCode) {
		for(var i = 0; i < this._dataSetFormModel.dataSetTypes.length; i++) {
			if(this._dataSetFormModel.dataSetTypes[i].code === typeCode) {
				return this._dataSetFormModel.dataSetTypes[i];
			}
		}
		return null;
	}
	
	//
	// Form Submit
	//
	this.submitDataSet = function() {
		//
		// Check upload is finish
		//
		Util.blockUI();
		var _this = this;
		
		//
		// Metadata Submit and Creation (Step 2)
		//
		var metadata = this._dataSetFormModel.dataSet.properties;
			
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
				"filenames" : _this._dataSetFormModel.files,
				"folderName" : folderName,
				"isZipDirectoryUpload" : isZipDirectoryUpload,
				//Metadata
				"metadata" : metadata,
				//For Moving files
				"sessionID" : openBIS.getSession(),
				"openBISURL" : openBIS._internal.openbisUrl
		};
		
		var sampleOrExperimentIdentifier = this._dataSetFormModel.sampleOrExperiment.identifier;
		if(sampleOrExperimentIdentifier.split("/").length === 3) {
			parameters["sampleIdentifier"] = sampleOrExperimentIdentifier;
		} else if(sampleOrExperimentIdentifier.split("/").length === 4) {
			parameters["experimentIdentifier"] = sampleOrExperimentIdentifier;
		}
			
		if(this._dataSetFormModel.dataStores.length > 0) {
			openBIS.createReportFromAggregationService(this._dataSetFormModel.dataStores[0].code, "dataset-uploader-api", parameters, function(response) {
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
					var errorMessage = stacktrace.substring(startIndex, endIndex).trim();
					Util.showError(errorMessage, function() {Util.unblockUI();});
				} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
					Util.showSuccess("DataSet Created.", function() {
						Util.unblockUI();
						_this.init(_this._container, _this._sampleOrExperiment);
					});
					
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}