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

function PlateController(sample) {
	this._plateModel = new PlateModel(sample);
	this._plateView = new PlateView(this, this._plateModel);
	
	this.getPlaceHolderId = function() {
		return this._plateModel.getPlaceHolderId();
	}
	
	this.getPlaceHolder = function() {
		return this._plateView.getPlaceHolder();
	}
	
	this.initWithPlaceHolder = function() {
		var _this = this;
		var repeatUntilSet = function() {
			var placeHolderFound = $("#" + _this._plateModel.getPlaceHolderId());
			if(placeHolderFound.length === 0) {
				setTimeout(repeatUntilSet, 100);
			} else {
				_this.init(placeHolderFound);
			}
		}
		repeatUntilSet();
	}
	this.init = function($container) {
		var _this = this;
		$container.empty();
		$container.append("Loading Wells ...");
		if(this._plateModel.sample.contained) {
			this._plateView.repaint($container);
		} else {
			mainController.serverFacade.searchContained(this._plateModel.sample.permId, function(contained) {
				_this._plateModel.sample.contained = contained;
				_this._plateView.repaint($container);
			});
		}
		
	}
}