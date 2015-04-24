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
	
	this.init = function($container) {
		var _this = this;
		$container.append("Loading Wells ...");
		mainController.serverFacade.searchContained(this._plateModel.sample.permId, function(contained) {
			_this._plateModel.wells = contained;
			_this._plateView.repaint($container);
		});
	}
}