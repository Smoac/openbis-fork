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

function MoveSampleController(samplePermId, successAction) {
	this._moveSampleModel = new MoveSampleModel(samplePermId, successAction);
	this._moveSampleView = new MoveSampleView(this, this._moveSampleModel);
	
	this.init = function() {
		var _this = this;
		mainController.serverFacade.searchWithUniqueId(this._moveSampleModel.samplePermId, function(data) {
			_this._moveSampleModel.sample = data[0];
			_this._moveSampleView.repaint();
		});
	}
	
	this.move = function() {
		var _this = this;
		if(!this._moveSampleModel.isNewExperiment && !this._moveSampleModel.experimentIdentifier) {
			Util.showError("Please choose an experiment.", function() {});
			return;
		}
		
		if(this._moveSampleModel.isNewExperiment && !this._moveSampleModel.experimentIdentifier) {
			Util.showError("Please choose the project and experiment name.", function() {});
			return;
		}
		
		if(this._moveSampleModel.isNewExperiment && !this._moveSampleModel.experimentType) {
			Util.showError("Please choose the experiment type.", function() {});
			return;
		}
		
		mainController.serverFacade.moveSample(
				this._moveSampleModel.sample.identifier,
				this._moveSampleModel.experimentIdentifier,
				this._moveSampleModel.experimentType, function(isOK) {
					if(isOK) {
						Util.showSuccess("Sample " + _this._moveSampleModel.sample.identifier + " moved to " + _this._moveSampleModel.experimentIdentifier, function() { Util.unblockUI(); if(_this._moveSampleModel.successAction) { _this._moveSampleModel.successAction(); } });
					} else {
						Util.showError("Sample " + _this._moveSampleModel.sample.identifier + " failed " + _this._moveSampleModel.experimentIdentifier, function() {});
					}
				});
	}
}