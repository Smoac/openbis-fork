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

function SampleTableController(mainController, experimentIdentifier) {
	this._mainController = mainController;
	this._sampleTableModel = new SampleTableModel(experimentIdentifier);
	this._sampleTableView = new SampleTableView(this, this._sampleTableModel);
	this._dataGridController = null;
	
	this.init = function($container) {
		var _this = this;
		Util.blockUI();
		this._loadExperimentData(function() {
			_this._sampleTableView.repaint($container);
			Util.unblockUI();
		});
	}
	
	this._loadExperimentData = function(callback) {
		var _this = this;
		mainController.serverFacade.searchWithExperiment(this._sampleTableModel.experimentIdentifier, function(experimentSamples) {
			_this._sampleTableModel.allSamplesFromExperiment = experimentSamples;
			for(var i = 0; i < experimentSamples.length; i++) {
				if(_this._sampleTableModel.sampleTypesOnExperiment[experimentSamples[i].sampleTypeCode]) {
					_this._sampleTableModel.sampleTypesOnExperiment[experimentSamples[i].sampleTypeCode] = _this._sampleTableModel.sampleTypesOnExperiment[experimentSamples[i].sampleTypeCode] + 1;
				} else {
					_this._sampleTableModel.sampleTypesOnExperiment[experimentSamples[i].sampleTypeCode] = 1;
				}
			}
			callback();
		});
	}
	
	this._reloadTableWithSampleType = function(selectedSampleTypeCode) {
		if(sampleTypeCode !== '') { //Verify something selected
			//Get samples from type
			var samples = [];
			for (var idx = 0; idx < this._sampleTableModel.allSamplesFromExperiment.length; idx++) {
				var sampleToCheckType = this._sampleTableModel.allSamplesFromExperiment[idx];
				if(sampleToCheckType.sampleTypeCode === selectedSampleTypeCode) {
					samples.push(sampleToCheckType);
				}
			}
			
			//Create grid model for sample type
			var sampleType = profile.getSampleTypeForSampleTypeCode(selectedSampleTypeCode);
			var propertyCodes = profile.getAllPropertiCodesForTypeCode(selectedSampleTypeCode);
			var propertyCodesDisplayNames = profile.getPropertiesDisplayNamesForTypeCode(selectedSampleTypeCode, propertyCodes);
			
			//Fill Columns model
			var columns = [ {
				label : 'Code',
				property : 'code',
				sortable : true
			}];
			
			
			for (var idx = 0; idx < propertyCodes.length; idx++) {
				columns.push({
					label : propertyCodesDisplayNames[idx],
					property : propertyCodes[idx],
					sortable : true
				});
			}
			
			//Fill data model
			var getDataList = function(callback) {
				var dataList = [];
				for(var sIdx = 0; sIdx < samples.length; sIdx++) {
					var sample = samples[sIdx];
					var sampleModel = { 'code' : sample.code, 'permId' : sample.permId };
					for (var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
						var property = propertyCodes[pIdx];
						sampleModel[property] = sample.properties[property];
					}
					dataList.push(sampleModel);
				}
				callback(dataList);
			};
			
			//Click event
			var rowClick = function(e) {
				mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
			}
			
			//Create and display table
			this._dataGridController = new DataGridController(null, columns, getDataList, rowClick);
			this._dataGridController.init(this._sampleTableView.getTableContainer());
		}
	}
}