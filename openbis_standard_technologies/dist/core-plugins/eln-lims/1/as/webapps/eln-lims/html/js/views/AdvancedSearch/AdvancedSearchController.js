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

function AdvancedSearchController(mainController, forceSearch) {
	this._mainController = mainController;
	this._advancedSearchModel = new AdvancedSearchModel(forceSearch);
	this._advancedSearchView = new AdvancedSearchView(this, this._advancedSearchModel);

	this.init = function($container) {
		var _this = this;
		_this._advancedSearchView.repaint($container);
	}
	
	this.search = function() {
		this._advancedSearchView.renderResults(this._advancedSearchModel.criteria);
	}
	
	this.searchWithPagination = function(criteria, isGlobalSearch) {
		return function(callback, options) {
			var callbackForSearch = function(results) {
				var dataList = [];
				for(var rIdx = 0; rIdx < results.objects.length; rIdx++) {
					var entity = results.objects[rIdx];
					
					var rowData = {};
					if(isGlobalSearch) {
						rowData.matched = entity.match;
						rowData.score = entity.score;
						
						switch(entity.objectKind) {
							case "SAMPLE":
								entity = entity.sample;
							break;
							case "EXPERIMENT":
								entity = entity.experiment;
							break;
							case "DATA_SET":
								entity = entity.dataSet;
							break;
							case "MATERIAL":
								continue; //Skip materials, they are not supported on the ELN
							break;
						}
					}
					
					if(!entity) {
						continue;
					}
					
					//properties
					if(entity["@type"]) {
						rowData.entityKind = entity["@type"].substring(entity["@type"].lastIndexOf(".") + 1, entity["@type"].length);
					}
					
					if(entity.experiment) {
						rowData.experiment = entity.experiment.code;
					}
					
					rowData.entityType = (entity.type)?entity.type.code:"";
					rowData.code =  entity.code;
					rowData.permId = (entity.permId)?entity.permId.permId:"";
					rowData.registrationDate = (entity.registrator && entity.registrator.registrationDate)?Util.getFormatedDate(new Date(entity.registrator.registrationDate)):null;
					rowData.modificationDate = (entity.modifier && entity.modifier.registrationDate)?Util.getFormatedDate(new Date(entity.modifier.registrationDate)):null;
					rowData.entityObject = entity;
					
					if(entity.identifier) {
						rowData.identifier = entity.identifier.identifier;
					}
					
					for(var propertyCode in entity.properties) {
						rowData[propertyCode] = entity.properties[propertyCode];
					}
					
					//Add the row data
					dataList.push(rowData);
				}
				
				callback({
					objects : dataList,
					totalCount : results.totalCount
				});
			}
			
			var fetchOptions = {};
			
			if(options) {
				fetchOptions.count = options.pageSize;
				fetchOptions.from = options.pageIndex * options.pageSize;
			}
			
			if(!criteria.cached) {
				fetchOptions.cache = "RELOAD_AND_CACHE";
				criteria.cached = true;
			} else {
				fetchOptions.cache = "CACHE";
			}
				
			var criteriaToSend = $.extend(true, {}, criteria);
			
			if(options && options.search) {
				var filter = options.search.toLowerCase().split(/[ ,]+/); //Split by regular space or comma
				for(var fIdx = 0; fIdx < filter.length; fIdx++) {
					var fKeyword = filter[fIdx];
					criteriaToSend.rules[Util.guid()] = { type : "All", name : "", value : fKeyword };
				}
			}
			
			if(options && options.sortProperty && options.sortDirection) {
				fetchOptions.sort = { 
						type : null,
						name : null,
						direction : options.sortDirection
				}
				switch(options.sortProperty) {
					case "code":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "code";
						break;
					case "identifier":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "identifier";
						break;
					case "entityType":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "type";
						break;
					case "registrationDate":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "registrationDate"
						
						break;
					case "modificationDate":
						fetchOptions.sort.type = "Attribute";
						fetchOptions.sort.name = "modificationDate";
						break;
					case "entityKind":
					case "identifier":
					case "experiment":
					case "matched":
					case "score":
						fetchOptions.sort = null;
						break;
					default: //Properties
						fetchOptions.sort.type = "Property";
						fetchOptions.sort.name = options.sortProperty;
						break;
				}
			}
			
			switch(criteriaToSend.entityKind) {
				case "ALL":
					$(".repeater-search").empty();
					var freeText = "";
					for(var ruleId in criteriaToSend.rules) {
						if(criteriaToSend.rules[ruleId].value) {
							freeText += " " +  criteriaToSend.rules[ruleId].value;
						}
					}
					mainController.serverFacade.searchGlobally(freeText, fetchOptions, callbackForSearch);
					break;
				case "SAMPLE":
					mainController.serverFacade.searchForSamplesAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
					break;
				case "EXPERIMENT":
					mainController.serverFacade.searchForExperimentsAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
					break;
				case "DATASET":
					mainController.serverFacade.searchForDataSetsAdvanced(criteriaToSend, fetchOptions, callbackForSearch);
					break;
			}
		}
	}
}