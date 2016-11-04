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

function AdvancedSearchController(mainController, forceFreeTextSearch) {
	this._mainController = mainController;
	this._advancedSearchModel = new AdvancedSearchModel(forceFreeTextSearch);
	this._advancedSearchView = new AdvancedSearchView(this, this._advancedSearchModel);

	this.init = function($container) {
		var _this = this;
		_this._advancedSearchView.repaint($container);
	}
	
	this.search = function() {
		var _this = this;
		var model = this._advancedSearchModel.criteria;
		
		Util.blockUI();
		var callbackFunction = function(results) {
			_this._advancedSearchView.renderResults(results);
			Util.unblockUI();
		};
		
		switch(this._advancedSearchModel.criteria.entityKind) {
			case "ALL":
				var freeText = "";
				for(var ruleId in model.rules) {
					if(model.rules[ruleId].value) {
						freeText += " " +  model.rules[ruleId].value;
					}
				}
				mainController.serverFacade.searchGlobally(freeText, callbackFunction);
				break;
			case "SAMPLE":
				mainController.serverFacade.searchForSamplesAdvanced(model, null, callbackFunction);
				break;
			case "EXPERIMENT":
				mainController.serverFacade.searchForExperimentsAdvanced(model, null, callbackFunction);
				break;
			case "DATASET":
				mainController.serverFacade.searchForDataSetsAdvanced(model, null, callbackFunction);
				break;
		}
	}
}