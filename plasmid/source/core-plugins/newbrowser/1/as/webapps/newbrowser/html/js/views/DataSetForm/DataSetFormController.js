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

function DataSetFormController(mainController, mode, sample, dataSet) {
	this._mainController = mainController;
	this._dataSetFormModel = new DataSetFormModel(mode, sample, dataSet);
	this._dataSetFormView = new DataSetFormView(this, this._dataSetFormModel);
	
	this.init = function($container) {
		var _this = this;
		mainController.serverFacade.listDataSetTypes(
				function(data) {
					_this._dataSetFormModel.dataSetTypes = data.result;
					_this._dataSetFormView.repaint($container);
				}
		);
	}
	
	this.isDirty = function() {
		return this._dataSetFormModel.isFormDirty;
	}
	
	this._getDataSetType = function(typeCode) {
		for(var i = 0; i < this._dataSetFormModel.dataSetTypes.length; i++) {
			if(this._dataSetFormModel.dataSetTypes[i].code === typeCode) {
				return this._dataSetFormModel.dataSetTypes[i];
			}
		}
		return null;
	}
	
	this.deleteDataSet = function(reason) {
		var _this = this;
		mainController.serverFacade.deleteDataSets([this._dataSetFormModel.dataSet.code], reason, function(data) {
			if(data.error) {
				Util.showError(data.error.message);
			} else {
				Util.showSuccess("Data Set Deleted");
				mainController.changeView('showViewSamplePageFromPermId', _this._dataSetFormModel.sample.permId);
			}
		});
	}
	
	//
	// Form Submit
	//
	this.submitDataSet = function() {
		//
		// Check upload is finish
		//
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			if(this._dataSetFormModel.files.length === 0) {
				Util.blockUI();
				Util.showError("You should upload at least one file.", function() { Util.unblockUI(); });
				return;
			}
			
			if(Uploader.uploadsInProgress()) {
				Util.blockUI();
				Util.showError("Please wait the upload to finish.", function() { Util.unblockUI(); });
				return;
			}
		}
		
		Util.blockUI();
		var _this = this;
		
		//
		// Metadata Submit and Creation (Step 2)
		//
		var metadata = this._dataSetFormModel.dataSet.properties;
			
		var isZipDirectoryUpload = profile.isZipDirectoryUpload($('#DATASET_TYPE').val());
		if(isZipDirectoryUpload === null) {
			isZipDirectoryUpload = $("#isZipDirectoryUpload"+":checked").val() === "on";
		}
		
		var folderName = $('#folderName').val();
		if(!folderName) {
			folderName = 'DEFAULT';
		}
		
		var method = null;
		var sampleIdentifier = null;
		var dataSetTypeCode = null;
		var dataSetCode = null;
		if(this._dataSetFormModel.mode === FormMode.CREATE) {
			method = "insertDataSet";
			sampleIdentifier = this._dataSetFormModel.sample.identifier;
			dataSetTypeCode = $('#DATASET_TYPE').val();
		} else if(this._dataSetFormModel.mode === FormMode.EDIT) {
			method = "updateDataSet";
			dataSetCode = this._dataSetFormModel.dataSet.code;
			dataSetTypeCode = this._dataSetFormModel.dataSet.dataSetTypeCode;
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"dataSetCode" : dataSetCode, //Used for updates
				"sampleIdentifier" : sampleIdentifier, //Use for creation
				"dataSetType" : dataSetTypeCode,
				"filenames" : _this._dataSetFormModel.files,
				"folderName" : folderName,
				"isZipDirectoryUpload" : isZipDirectoryUpload,
				//Metadata
				"metadata" : metadata,
				//For Moving files
				"sessionID" : mainController.serverFacade.openbisServer.getSession(),
				"openBISURL" : mainController.serverFacade.openbisServer._internal.openbisUrl
		};
			
		if(profile.allDataStores.length > 0) {
			mainController.serverFacade.createReportFromAggregationService(profile.allDataStores[0].code, parameters, function(response) {
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
					var callbackOk = function() {
						_this._dataSetFormModel.isFormDirty = false;
						Util.unblockUI();
						if(_this._dataSetFormModel.mode === FormMode.CREATE) {
							mainController.changeView('showViewSamplePageFromPermId', _this._dataSetFormModel.sample.permId);
						} else if(_this._dataSetFormModel.mode === FormMode.EDIT) {
							mainController.changeView('showViewDataSetPageFromPermId', _this._dataSetFormModel.dataSet.code);
						}
					}
					Util.showSuccess("DataSet Created.", callbackOk);
				} else { //This should never happen
					Util.showError("Unknown Error.", function() {Util.unblockUI();});
				}
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
	}
}