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

function AdvancedEntitySearchDropdown(	isMultiple,
										isRequired,
										placeholder,
										selectsExperiments,
										selectsSamples,
										selectsDatasets) {
	var isMultiple = isMultiple;
	var isRequired = isRequired;
	var placeholder = placeholder;
	var selectsExperiments = selectsExperiments;
	var selectsSamples = selectsSamples;
	var selectsDatasets = selectsDatasets;
	var $select = FormUtil.getDropdown({}, "");
	var storedParams = null;
	
	//
	// External API
	//
	
	this.addSelectedExperiment = function(experimentIdentifier) {
		var _this = this;
		require([ 'as/dto/experiment/id/ExperimentIdentifier', "as/dto/experiment/fetchoptions/ExperimentFetchOptions" ],
				function(ExperimentIdentifier, ExperimentFetchOptions) {
		            var id1 = new ExperimentIdentifier(experimentIdentifier);
		            var fetchOptions = new ExperimentFetchOptions();
		            fetchOptions.withProperties();
		            mainController.openbisV3.getExperiments([ id1 ], fetchOptions).done(function(map) {
		            	_this.addSelected(map[experimentIdentifier]);
		            });
		});
	}
	
	this.addSelectedSample = function(sampleIdentifier) {
		var _this = this;
		require([ "as/dto/sample/id/SampleIdentifier", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
				function(SampleIdentifier, SampleFetchOptions) {
		            var id1 = new SampleIdentifier(sampleIdentifier);
		            var fetchOptions = new SampleFetchOptions();
		            fetchOptions.withProperties();
		            mainController.openbisV3.getSamples([ id1 ], fetchOptions).done(function(map) {
		            	_this.addSelected(map[sampleIdentifier]);
		            });
		});
	}
	
	this.addSelectedDataSets = function(datasetPermIds) {
		var _this = this;
		require([ "as/dto/dataset/id/DataSetPermId", "as/dto/dataset/fetchoptions/DataSetFetchOptions" ],
				function(DataSetPermId, DataSetFetchOptions) {
					var ids = [];
					for(var dIdx = 0; dIdx < datasetPermIds.length; dIdx++) {
						var id = new DataSetPermId(datasetPermIds[dIdx]);
						ids.push(id);
					}
		            var fetchOptions = new DataSetFetchOptions();
		            fetchOptions.withProperties();
		            mainController.openbisV3.getDataSets(ids, fetchOptions).done(function(map) {
		            	for(var dIdx = 0; dIdx < datasetPermIds.length; dIdx++) {
							_this.addSelected(map[datasetPermIds[dIdx]]);
						}
		            });
		});
	}
	
	this.addSelected = function(v3entity) {
		var text = getDisplayName(v3entity);
		var id = null;
		
		if(v3entity.permId && v3entity.permId.permId) { //Only v3 objects supported
			id = v3entity.permId.permId;
		} else {
			throw {
			    name: "NonV3ObjectException",
			    message: "Object without v3 permId",
			    toString: function() {
			      return this.name + ": " + this.message;
			    }
			}
		}
		
		var data = {
				id : id,
				text : text,
				data : v3entity
		};
		
		var newOption = new Option(text, id, true, true);
		newOption.data = data;
		$select.append(newOption).trigger('change');
	}
	
	this.getSelected = function() {
		var selected = $select.select2('data');
		var entities = [];
		for(var eIdx = 0; eIdx < selected.length; eIdx++) {
			if(selected[eIdx].data) {
				entities.push(selected[eIdx].data.data);
			}
			if(selected[eIdx].element.data) {
				entities.push(selected[eIdx].element.data.data);
			}
		}
		return entities;
	}
	
	//
	// Search Entity
	//
	var getDisplayName = function(entity) {
		var text = null;
		if(profile.propertyReplacingCode && entity.properties && entity.properties[profile.propertyReplacingCode]) {
			text = entity.code + " (" + entity.properties[profile.propertyReplacingCode] + ")";
		} else {
			text = entity.code;
		}
		return text;
	}
	
	var searchExperiment = function(action) {
		var criteria = { 	
						entityKind : "EXPERIMENT", 
						logicalOperator : "AND", 
						rules : { "UUIDv4" : { type : "All", value : storedParams.data.q } }
					};
		mainController.serverFacade.searchForExperimentsAdvanced(criteria, null, function(results) { results.type = "Experiments"; action(results) });
	}
	
	var searchSample = function(action) {
		var criteria = { 	
						entityKind : "SAMPLE", 
						logicalOperator : "AND", 
						rules : { "UUIDv4" : { type : "All", value : storedParams.data.q } }
					};
		mainController.serverFacade.searchForSamplesAdvanced(criteria, null, function(results) { results.type = "Samples"; action(results) });
	}
	
	var searchDataset = function(action) {
		var criteria = { 	
						entityKind : "DATASET", 
						logicalOperator : "AND", 
						rules : { "UUIDv4" : { type : "All", value : storedParams.data.q } }
					};
		mainController.serverFacade.searchForDataSetsAdvanced(criteria, null, function(results) { results.type = "DataSets"; action(results) });
	}
	
	//
	// Build Select
	//
	this.init = function($container) {
		$select.attr("multiple", "multiple");
		
		if(isRequired) {
			$select.attr("required", "required");
		}
		if(isMultiple) {
			maximumSelectionLength = 9999;
		} else {
			maximumSelectionLength = 1;
		}
		
		$container.append($select);
		
		$select.select2({
				width: '100%', 
				theme: "bootstrap",
				maximumSelectionLength: maximumSelectionLength,
				minimumInputLength: 2,
				placeholder : placeholder,
				ajax: {
					delay: 1000,
					processResults: function (data) {
						var results = [];
						
						for(var dIdx = 0; dIdx < data.length; dIdx++) {
							var group = {
								text: data[dIdx].type, 
								children : []
							}
							
							var entities = data[dIdx].objects;
							for(var eIdx = 0; eIdx < entities.length; eIdx++) {
								group.children.push({
									id : entities[eIdx].permId.permId,
									text : getDisplayName(entities[eIdx]),
									data : {
										id : entities[eIdx].permId.permId,
										text : getDisplayName(entities[eIdx]),
										data : entities[eIdx]
									}
								})
							}
							
							if(entities.length > 0) {
								results.push(group);
							}
						}
						
					    return {
					    	"results": results,
					    	"pagination": {
					    		"more": false
					    	}
						};
					},
				    transport: function (params, success, failure) {
				    	storedParams = params;
				    	
						// Searches
						var searches = [];
						var searchesResults = [];
						if(selectsExperiments) {
							searches.push(searchExperiment);
						}
						if(selectsSamples) {
							searches.push(searchSample);
						}
						if(selectsDatasets) {
							searches.push(searchDataset);
						}
						      
						var action = null;
						action = function(result) {
							searchesResults.push(result);
						 	if(searches.length > 0) {
						    	var search = searches.shift();
						    	search(action);
						    } else {
						    	success(searchesResults);
						    }
						};
						      
						var search = searches.shift();
						search(action);
						      
					 	return {
					    	abort : function () { /*Not implemented*/ }
					  	}
				    }
			  }
		});
	}
	
}