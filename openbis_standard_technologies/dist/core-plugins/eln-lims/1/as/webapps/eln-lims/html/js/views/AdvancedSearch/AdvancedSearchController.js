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

	this.init = function(views) {
		var _this = this;
		_this._advancedSearchView.repaint(views);
	}
	
	this.search = function() {
		var criteria = this._advancedSearchModel.criteria;
		var numberOfGeneralRules = 0;
		var numberOfRules = 0;
		
		for(ruleKey in criteria.rules) {
			var rule = criteria.rules[ruleKey];
			numberOfRules++;
			if(rule.value === null || rule.value === undefined || ("" + rule.value).trim() === "" || ("" + rule.value).trim() === "*") {
				numberOfGeneralRules++;
			}
		}
		
		
		var _this = this;
		var trueSearch = function() {
			_this._advancedSearchView.renderResults(criteria);
		}
		
		if(numberOfRules === numberOfGeneralRules) {
			var warning = "This search query is too broad. This might take a long time and might lead to a very large number of search results. \n Do you want to submit the query anyway?";
			Util.showWarning(warning, trueSearch);
		} else {
			trueSearch();
		}
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
					rowData.registrator = (entity.registrator)?entity.registrator.userId:null;
					rowData.registrationDate = (entity.registrationDate)?Util.getFormatedDate(new Date(entity.registrationDate)):null;
					rowData.modifier = (entity.modifier)?entity.modifier.userId:null;
					rowData.modificationDate = (entity.modificationDate)?Util.getFormatedDate(new Date(entity.modificationDate)):null;
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
				$("#search").removeClass("search-query-searching");
			}
			
			var fetchOptions = {};
			
			if(options) {
				fetchOptions.count = options.pageSize;
				fetchOptions.from = options.pageIndex * options.pageSize;
				fetchOptions.minTableInfo = true;
				fetchOptions.withExperiment = true;
				// TODO : Unused on the UI, should be added for DataSets
				// fetchOptions.withSample = true;
			}
			
			if(!criteria.cached || (criteria.cachedSearch !== options.search)) {
				fetchOptions.cache = "RELOAD_AND_CACHE";
				criteria.cachedSearch = options.search;
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
			
			$(".repeater-search").remove();
			
			switch(criteriaToSend.entityKind) {
				case "ALL":
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