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

/**
 * Class ServerFacade.
 *
 * Contains all methods used to access the server, is used as control point to modify the API methods used
 * without impacting other classes.
 *
 * @constructor
 * @this {ServerFacade}
 * @param {openbis} openbisServer API facade to access the server.
 */
function ServerFacade(openbisServer) {
	this.openbisServer = openbisServer;
	
	//
	// Intercepting general errors
	//
	var responseInterceptor = function(response, action){
		var isError = false;
		if(response && response.error) {
			if(response.error.message === "Session no longer available. Please login again.") {
				isError = true;
				Util.showError(response.error.message, function() {
					location.reload(true);
				}, true);
			} else if(response.error === "Request failed: ") {
				Util.showError(response.error + "openBIS or DSS cannot be reached. Please try again or contact your admin.", null, true);
			}
		}
		
		if(action && !isError){
			action(response);
		}
	}
	
	this.openbisServer.setResponseInterceptor(responseInterceptor);
	
	//
	// Display Settings
	//
	this.getSetting = function(keyOrNull, callback) {
		mainController.serverFacade.openbisServer.getWebAppSettings("ELN-LIMS", function(response) {
			var settings = response.result.settings;
			if(!settings) {
				settings = {};
			}
			if(keyOrNull) {
				callback(settings[keyOrNull]);
			} else {
				callback(settings);
			}
		});
	}
	
	this.setSetting = function(key, value) {
		var _this = this;
		var webAppId = "ELN-LIMS";
		this.openbisServer.getWebAppSettings(webAppId, function(response) {
			var settings = response.result.settings;
			if(!settings) {
				settings = {};
			}
			settings[key] = value;
			
			var webAppSettings = {
					"@type" : "WebAppSettings",
					"webAppId" : webAppId,
					"settings" : settings
			}
			
			_this.openbisServer.setWebAppSettings(webAppSettings, function(result) {});
		});
	}
	
	//
	// Login Related Functions
	//
	this.getUserId = function() {
		var sessionId = this.openbisServer.getSession();
		var userId = sessionId.substring(0, sessionId.indexOf("-"));
		return userId;
	}
	
	this.login = function(username, pass, callbackFunction) {
		this.openbisServer.login(username, pass, callbackFunction);
	}

	this.ifRestoredSessionActive = function(callbackFunction) {
		this.openbisServer.ifRestoredSessionActive(callbackFunction);
	}

	this.logout = function(callbackFunction) {
		this.openbisServer.logout(callbackFunction);
	}
	
	//
	// User Related Functions
	//
	this.listPersons = function(callbackFunction) {
		this.openbisServer.listPersons(callbackFunction);
	};
	
	this.registerUserPassword = function(userId, userPass, callbackFunction) {
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(),
			{
				"method" : "registerUserPassword",
				"userId" : userId,
				"password" : userPass
			},
			function(data){
				if(data.result.rows[0][0].value == "OK") {
					callbackFunction(true);
				} else {
					callbackFunction(false);
				}
			});
	}
	
	this.createELNUser = function(userId, callback) {
 		var _this = this;
 		var inventorySpacesToRegister = [];
 		var inventorySpaceToRegisterFunc = function(spaceCode, callback) {
			return function() {
				_this.openbisServer.registerPersonSpaceRole(spaceCode, userId, "USER", function(data) {
					if(data.error) {
						callback(false, data.error.message);
					} else {
						var spaceToRegister = inventorySpacesToRegister.pop();
						if(spaceToRegister) {
							spaceToRegister();
						} else {
							callback(true, "User " + userId + " created successfully.");
						}
					}
				});
			}
		};
 		
		_this.openbisServer.registerPerson(userId, function(data) {
			if(data.error) {
				callback(false, data.error.message);
			} else {
				_this.openbisServer.registerSpace(userId, "Space for user " + userId, function(data) {
					if(data.error) {
						callback(false, data.error.message);
					} else {
						_this.openbisServer.registerPersonSpaceRole(userId, userId, "ADMIN", function(data) {
							if(data.error) {
								callback(false, data.error.message);
							} else {
								for(var i = 0; i < profile.inventorySpaces.length; i++) {
									var spaceCode = profile.inventorySpaces[i];
									inventorySpacesToRegister.push(inventorySpaceToRegisterFunc(spaceCode, callback));
								}
								
								var spaceToRegister = inventorySpacesToRegister.pop();
								if(spaceToRegister) {
									spaceToRegister();
								} else {
									callback(true, "User " + userId + " created successfully.");
								}
							}
						});
					}			
				});
			}			
		});
	}
	
	//
	//
	//
	this.exportAll = function(entities, includeRoot, callbackFunction) {
		this.customELNApi({
			"method" : "exportAll",
			"includeRoot" : includeRoot,
			"entities" : entities,
		}, callbackFunction, "exports-api");
	}
	
	//
	// Metadata Related Functions
	//
	this.listSampleTypes = function(callbackFunction) {
		this.openbisServer.listSampleTypes(callbackFunction);
	}
	
	this.listExperimentTypes = function(callbackFunction) {
		this.openbisServer.listExperimentTypes(callbackFunction);
	}
	
	this.listVocabularies = function(callbackFunction) {
		this.openbisServer.listVocabularies(callbackFunction);
	}
	
	this.listDataSetTypes = function(callbackFunction) {
		this.openbisServer.listDataSetTypes(callbackFunction);
	}
	
	this.listSpaces = function(callbackFunction) {
		this.openbisServer.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
			var spaces = [];
			for(var i = 0; i < data.result.length; i++) {
				spaces.push(data.result[i].code);
			}
			callbackFunction(spaces);
		});
	}
	
	this.listSpacesWithProjectsAndRoleAssignments = function(somethingOrNull, callbackFunction) {
		this.openbisServer.listSpacesWithProjectsAndRoleAssignments(somethingOrNull, callbackFunction);
	}
	
	this.getSpaceFromCode = function(spaceCode, callbackFunction) {
		this.openbisServer.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
			data.result.forEach(function(space){
				if(space.code === spaceCode) {
					callbackFunction(space);
				}
			});
		});
	}
	
	this.listExperiments = function(projects, callbackFunction) {
		if(projects && projects.length > 0) {
			this.openbisServer.listExperiments(projects, null, callbackFunction);
		} else {
			callbackFunction({});
		}
	}
	
	this.getProjectFromIdentifier = function(identifier, callbackFunction) {
		this.openbisServer.listProjects(function(data) {
			data.result.forEach(function(project){
				var projIden = "/" + project.spaceCode + "/" + project.code;
				if(projIden === identifier) {
					callbackFunction(project);
					return;
				}
			});
		});
	}
	
	this.getProjectFromPermId = function(permId, callbackFunction) {
		this.openbisServer.listProjects(function(data) {
			data.result.forEach(function(project){
				if(project.permId === permId) {
					callbackFunction(project);
					return;
				}
			});
		});
	}
	
	this.listExperimentsForIdentifiers = function(experimentsIdentifiers, callbackFunction) {
		this.openbisServer.listExperimentsForIdentifiers(experimentsIdentifiers, callbackFunction);
	}
	
	this.listSamplesForExperiments = function(experiments, callbackFunction) {
		var experimentsMatchClauses = []
		
		experiments.forEach(function(experiment){
			experimentsMatchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "PERM_ID",
				desiredValue : experiment.permId
			});
		});
		
		var experimentCriteria = {
				matchClauses : experimentsMatchClauses,
				operator : "MATCH_ANY_CLAUSES"
		}
		
		var experimentSubCriteria = {
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "EXPERIMENT",	
				"criteria" : experimentCriteria
		}

		var sampleCriteria = 
		{
			subCriterias : [ experimentSubCriteria ],
			operator : "MATCH_ALL_CLAUSES"
		};
		
		if(experiments.length === 0) {
			callbackFunction({});
		} else {
			this.openbisServer.searchForSamples(sampleCriteria, callbackFunction);
		}
	}
	
	this.listPropertyTypes = function(callbackFunction) {
		if(this.openbisServer.listPropertyTypes) { //If not present will not break, but annotations should not be used.
			this.openbisServer.listPropertyTypes(false, callbackFunction);
		}
	}
	

	//
	//OLD METHOD
	/*this.generateCode = function(sampleType, action) {
		this.openbisServer.countNumberOfSamplesForType(sampleType.code, function(response) {
			if(response.result || response.result === 0) {
				action(sampleType.codePrefix + (parseInt(response.result) + 1));
			}
		});
	}*/
	
	this.generateCode = function(sampleType, action) {
		var prefix = sampleType.codePrefix;
		this.searchWithType(
				sampleType.code,
				prefix+"*",
				false,
				function(results) {
					if(results.length == 0){
						nextcode = prefix + "1";
					} else{
						var codes = [];
						for(var idx=0; idx<results.length; idx++){
							numeric_code = results[idx].code.substring(prefix.length);							
							numeric_code = numeric_code.replace("_","");
							numeric_code = parseInt(numeric_code);
							if(isNaN(numeric_code))
								numeric_code = 1;
							codes[idx] = numeric_code; 
						}
						codes = codes.sort(function (a, b) { 
						    return a - b;
						});
						var nextid = codes[codes.length-1] + 1;
						var nextcode = prefix + nextid;
					}
					action(nextcode);
				});
	}
		
	this.deleteDataSets = function(datasetIds, reason, callback) {
		this.openbisServer.deleteDataSets(datasetIds, reason, "TRASH", callback);
	}
	
	this.deleteSamples = function(sampleIds, reason, callback) {
		this.openbisServer.deleteSamples(sampleIds, reason, "TRASH", callback);
	}
	
	this.deleteExperiments = function(experimentIds, reason, callback) {
		this.openbisServer.deleteExperiments(experimentIds, reason, "TRASH", callback);
	}
	
	this.deleteProjects = function(projectIds, reason, callback) {
		this.openbisServer.deleteProjects(projectIds, reason, callback);
	}
	
	this.listDeletions = function(callback) {
		this.openbisServer.listDeletions(["ALL_ENTITIES"], callback);
	}
	
	this.deletePermanently = function(deletionIds, callback) {
		this.openbisServer.deletePermanently(deletionIds, callback);
	}
	
	this.revertDeletions = function(deletionIds, callback) {
		this.openbisServer.revertDeletions(deletionIds, callback);
	}
	
	//
	// Data Set Related Functions
	//
	this.listDataSetsForSample = function(sampleToSend, trueOrFalse, callbackFunction) {
		var _this = this;
		var listDataSetsForV1Sample = function(v1Sample) {
			var cleanSample = $.extend({}, v1Sample);
			delete cleanSample.parents;
			delete cleanSample.children;
			_this.openbisServer.listDataSetsForSample(cleanSample, trueOrFalse, callbackFunction);
		}
		
		if(sampleToSend.id !== -1) { //Is V1 Sample
			listDataSetsForV1Sample(sampleToSend);
		} else { //Ask for a V1 Sample
			this.searchWithUniqueIdV1(sampleToSend.permId, function(sampleList) {
				listDataSetsForV1Sample(sampleList[0]);
			});
		}
	}

	this.listFilesForDataSet = function(datasetCode, pathInDataset, trueOrFalse, callbackFunction) {
		this.openbisServer.listFilesForDataSet(datasetCode, pathInDataset, trueOrFalse, callbackFunction);
	}

	//
	// Samples Import Related Functions
	//
	this.uploadedSamplesInfo = function(sampleTypeCode, fileKeyAtHTTPSession, callbackFunction) {
		this.openbisServer.uploadedSamplesInfo(sampleTypeCode, fileKeyAtHTTPSession, callbackFunction);
	}

	this.registerSamples = function(sampleTypeCode, fileKeyAtHTTPSession, somethingOrNull, callbackFunction) {
		this.openbisServer.registerSamples(sampleTypeCode, fileKeyAtHTTPSession, somethingOrNull, callbackFunction);
	}

	this.updateSamples = function(sampleTypeCode, fileKeyAtHTTPSession, somethingOrNull, callbackFunction) {
		this.openbisServer.updateSamples(sampleTypeCode, fileKeyAtHTTPSession, somethingOrNull, callbackFunction);
	}
	
	this.fileUpload = function(file, callbackFunction) {
		//Building Form Data Object for Multipart File Upload
		var formData = new FormData();
		formData.append("sessionKeysNumber", 1);
		formData.append("sessionKey_0", "sample-file-upload");
		formData.append("sample-file-upload", file);
		formData.append("sessionID", this.openbisServer.getSession());
		
		$.ajax({
			type: "POST",
			url: "/openbis/openbis/upload",
			contentType: false,
			processData: false,
			data: formData,
			success: function(result) {
				callbackFunction(result);
			}
		});
	}
	
	this.getTemplateLink = function(entityType, operationKind) {
		var GET = '/openbis/openbis/template-download?entityKind=SAMPLE';
			GET += '&entityType=' + entityType;
			GET += '&autoGenerate=false';
			GET += '&with_experiments=true';
			GET += '&with_space=true';
			GET += '&batch_operation_kind=' + operationKind;
			GET += '&timestamp=' + new Date().getTime();
			GET += '&sessionID=' + this.openbisServer.getSession();
		return GET;
	}

	this.getDirectLinkURL = function(callbackFunction) {
		this.customELNApi({ "method" : "getDirectLinkURL"}, callbackFunction);
	}
	
	//
	// Sample Others functions
	//
	this.moveSample = function(sampleIdentifier, experimentIdentifier, experimentType, callbackFunction) {
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(),
				{
					"method" : "moveSample",
					"sampleIdentifier" : sampleIdentifier,
					"experimentIdentifier" : experimentIdentifier,
					"experimentType" : experimentType
				},
				function(data){
					if(data.result.rows[0][0].value == "OK") {
						callbackFunction(true);
					} else {
						callbackFunction(false, data.result.rows[0][1].value);
					}
				});
	}
	//
	// Data Set Import Related Functions
	//
	
	this.fileUploadToWorkspace = function(dataStoreURL, fileFieldId, fileSessionKey, callbackHandler) {
		//File
		var file = document.getElementById(fileFieldId).files[0];
		var sessionID = this.openbisServer.getSession();
		var id = 0;
		var startByte = 0;
		var endByte = file.size;
		
		$.ajax({
			type: "POST",
			url: dataStoreURL + "/session_workspace_file_upload?sessionID=" + sessionID + "&filename=" + fileSessionKey + "&id=" + id + "&startByte=" + startByte + "&endByte=" + endByte,
			contentType: "multipart/form-data",
			processData: false,
			data: file,
			success: function(result) {
				callbackHandler(result);
			},
			error: function(result) {
				Util.showError("The upload failed. Configure your environment properly.", function() {Util.unblockUI();});
			}
		});
	}
	
	//
	// ELN Custom API
 	//
 	this.customELNApi = function(parameters, callbackFunction, service) {
 		if(!service) {
 			service = "eln-lims-api";
 		}
 		
 		if(!parameters) {
 			parameters = {};
 		}
 		parameters["sessionToken"] = this.openbisServer.getSession();
 		
 		var dataStoreCode = profile.getDefaultDataStoreCode();
 		this.openbisServer.createReportFromAggregationService(dataStoreCode, service, parameters, function(data) {
 			var error = null;
 			var result = {};
 			if(data.error) { //Error Case 1
 				error = data.error.message;
 			} else if (data.result.columns[1].title === "Error") { //Error Case 2
 				error = data.result.rows[0][1].value;
 			} else if (data.result.columns[0].title === "STATUS" && data.result.rows[0][0].value === "OK") { //Success Case
 				result.message = data.result.rows[0][1].value;
 				result.data = data.result.rows[0][2].value;
 				if(result.data) {
 					result.data = JSON.parse(result.data);
 				}
 			} else {
 				error = "Unknown Error.";
 			}
 			callbackFunction(error, result);
 		});
	}
 	
 	this.createReportFromAggregationService = function(dataStoreCode, parameters, callbackFunction, service) {
 		if(!service) {
 			service = "eln-lims-api";
 		}
 		if(!parameters) {
 			parameters = {};
 		}
 		parameters["sessionToken"] = this.openbisServer.getSession();
 		
		this.openbisServer.createReportFromAggregationService(dataStoreCode, service, parameters, callbackFunction);
	}
	
	//
	// Configuration Related Functions
	//
	this.getSession = function() {
		return this.openbisServer.getSession();
	}

	this.listDataStores = function(callbackFunction) {
		this.openbisServer.listDataStores(callbackFunction);
	}

	this.getUserDisplaySettings = function(callbackFunction) {
		if(this.openbisServer.getUserDisplaySettings) { //If the call exists
			this.openbisServer.getUserDisplaySettings(callbackFunction);
		}
	}

	//
	// Search Related Functions
	//
	
	this._createMaterialIdentifier = function(identifierString) {
		var parts = identifierString.split("/");

		return {
			"@type" : "MaterialIdentifierGeneric",
			"materialTypeIdentifier" : {
				"@type" : "MaterialTypeIdentifierGeneric",
				"materialTypeCode" : parts[1]
			},
			"materialCode" : parts[2]
		};
	}
	
	this.getMaterialsForIdentifiers = function(materialIdentifiers, callback) {
		var materialIdentifierObjects = [];
		for(var i = 0; i < materialIdentifiers.length; i++) {
			materialIdentifierObjects.push(this._createMaterialIdentifier(materialIdentifiers[i]));
		}
		this.openbisServer.getMaterialByCodes(materialIdentifierObjects, callback);
	}
	
	//
	// Search DataSet
	//
	
	this.searchDataSetWithUniqueId = function(dataSetPermId, callbackFunction) {
		var dataSetMatchClauses = [{
    			"@type":"AttributeMatchClause",
    			fieldType : "ATTRIBUTE",			
    			attribute : "PERM_ID",
    			desiredValue : dataSetPermId
		}]
		
		var dataSetCriteria = 
		{
			matchClauses : dataSetMatchClauses,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		this.openbisServer.searchForDataSets(dataSetCriteria, callbackFunction)
	}
	
	this.searchDataSetsWithTypeForSamples = function(dataSetTypeCode, samplesPermIds, callbackFunction)
	{
		var sampleMatchClauses = []
		
		samplesPermIds.forEach(function(samplesPermId){
			sampleMatchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "PERM_ID",
				desiredValue : samplesPermId
			});
		});
		
		var sampleCriteria = {
				matchClauses : sampleMatchClauses,
				operator : "MATCH_ANY_CLAUSES"
		}
		
		var sampleSubCriteria = {
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "SAMPLE",	
				"criteria" : sampleCriteria
		}
		
		var dataSetMatchClauses = [{
    			"@type":"AttributeMatchClause",
    			fieldType : "ATTRIBUTE",			
    			attribute : "TYPE",
    			desiredValue : dataSetTypeCode
		}]

		var dataSetCriteria = 
		{
			matchClauses : dataSetMatchClauses,
			subCriterias : [ sampleSubCriteria ],
			operator : "MATCH_ALL_CLAUSES"
		};
		
		this.openbisServer.searchForDataSets(dataSetCriteria, callbackFunction)
	}
	
	// Used for blast search datasets
	this.getSamplesForDataSets = function(dataSetCodes, callback) {
		this.openbisServer.getDataSetMetaDataWithFetchOptions(dataSetCodes, [ 'SAMPLE' ], callback);
	}
	
	//
	// New Advanced Search
	//
	
	this.searchForDataSetsAdvanced = function(advancedSearchCriteria, callback) {
		var criteriaClass = 'as/dto/dataset/search/DataSetSearchCriteria';
		var fetchOptionsClass = 'as/dto/dataset/fetchoptions/DataSetFetchOptions';
		var searchMethodName = 'searchDataSets';
		this.searchForEntityAdvanced(advancedSearchCriteria, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForExperimentsAdvanced = function(advancedSearchCriteria, callback) {
		var criteriaClass = 'as/dto/experiment/search/ExperimentSearchCriteria';
		var fetchOptionsClass = 'as/dto/experiment/fetchoptions/ExperimentFetchOptions';
		var searchMethodName = 'searchExperiments';
		this.searchForEntityAdvanced(advancedSearchCriteria, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForSamplesAdvanced = function(advancedSearchCriteria, callback) {
		var criteriaClass = 'as/dto/sample/search/SampleSearchCriteria';
		var fetchOptionsClass = 'as/dto/sample/fetchoptions/SampleFetchOptions';
		var searchMethodName = 'searchSamples';
		this.searchForEntityAdvanced(advancedSearchCriteria, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForSpacesAdvanced = function(advancedSearchCriteria, callback) {
		var criteriaClass = 'as/dto/space/search/SpaceSearchCriteria';
		var fetchOptionsClass = 'as/dto/space/fetchoptions/SpaceFetchOptions';
		var searchMethodName = 'searchSpaces';
		this.searchForEntityAdvanced(advancedSearchCriteria, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForProjectsAdvanced = function(advancedSearchCriteria, callback) {
		var criteriaClass = 'as/dto/project/search/ProjectSearchCriteria';
		var fetchOptionsClass = 'as/dto/project/fetchoptions/ProjectFetchOptions';
		var searchMethodName = 'searchProjects';
		this.searchForEntityAdvanced(advancedSearchCriteria, callback, criteriaClass, fetchOptionsClass, searchMethodName);
	}
	
	this.searchForEntityAdvanced = function(advancedSearchCriteria, callback, criteriaClass, fetchOptionsClass, searchMethodName) {
		require(['openbis', 
		         criteriaClass,
		         fetchOptionsClass,
		         'as/dto/common/search/DateObjectEqualToValue'], function(openbis, EntitySearchCriteria, EntityFetchOptions, DateObjectEqualToValue) {
			
			try {
				//Boilerplate
				var testProtocol = window.location.protocol;
				var testHost = window.location.hostname;
				var testPort = window.location.port;
				
				var testUrl = testProtocol + "//" + testHost + ":" + testPort;
				var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";
				
				var v3Api = new openbis(testApiUrl);
				v3Api._private.sessionToken = mainController.serverFacade.getSession();
				
				//Setting the searchCriteria given the advancedSearchCriteria model
				var searchCriteria = new EntitySearchCriteria();
				
				//Setting the fetchOptions given standard settings
				var fetchOptions = new EntityFetchOptions();
				if(fetchOptions.withTags) {
					fetchOptions.withTags();
				}
				if(fetchOptions.withType) {
					fetchOptions.withType();
				}
				if(fetchOptions.withSpace) {
					fetchOptions.withSpace();
				}
				if(fetchOptions.withExperiment) {
					fetchOptions.withExperiment();
				}
				if(fetchOptions.withRegistrator) {
					fetchOptions.withRegistrator();
				}
				if(fetchOptions.withModifier) {
					fetchOptions.withModifier();
				}
				if(fetchOptions.withParents) {
					fetchOptions.withParents();
				}
				if(fetchOptions.withProjects) {
					fetchOptions.withProjects();
				}
				if(fetchOptions.withProjects) {
					fetchOptions.withProjects();
				}
				
				//Operator
				var operator = advancedSearchCriteria.logicalOperator;
				if (!operator) {
					operator = "AND";
				}
				searchCriteria.withOperator(operator);
				
				//Rules
				var ruleKeys = Object.keys(advancedSearchCriteria.rules);
				for (var idx = 0; idx < ruleKeys.length; idx++)
				{
					var fieldType = advancedSearchCriteria.rules[ruleKeys[idx]].type;
					var fieldName = advancedSearchCriteria.rules[ruleKeys[idx]].name;
					var fieldNameType = null;
					var fieldValue = advancedSearchCriteria.rules[ruleKeys[idx]].value;
					
					if(fieldName) {
						var firstDotIndex = fieldName.indexOf(".");
						fieldNameType = fieldName.substring(0, firstDotIndex);
						fieldName = fieldName.substring(firstDotIndex + 1, fieldName.length);
					}
					
					if(!fieldValue) {
						fieldValue = "*";
					}
					
					var setPropertyCriteria = function(criteria, propertyName, propertyValue) {
						criteria.withProperty(propertyName).thatEquals(propertyValue);
					}
					
					var setAttributeCriteria = function(criteria, attributeName, attributeValue) {
						switch(attributeName) {
							//Used by all entities
							case "CODE":
								criteria.withCode().thatEquals(attributeValue);
								break;
							case "PERM_ID":
								criteria.withPermId().thatEquals(attributeValue);
								break;
							case "METAPROJECT":
								criteria.withTag().withCode().thatEquals(attributeValue); //TO-DO To Test, currently not supported by ELN UI
								break;
							case "REGISTRATION_DATE": //Must be a string object with format 2009-08-18
								criteria.withRegistrationDate().thatEquals(attributeValue);
								break;
							case "MODIFICATION_DATE": //Must be a string object with format 2009-08-18
								criteria.withModificationDate().thatEquals(attributeValue);
								break;
							case "SAMPLE_TYPE":
							case "EXPERIMENT_TYPE":
							case "DATA_SET_TYPE":
								criteria.withType().withCode().thatEquals(attributeValue);
								break;
							//Only Sample
							case "SPACE":
								criteria.withSpace().withCode().thatEquals(attributeValue);
								break;
							//Only Experiment
							case "PROJECT":
								criteria.withProject().withCode().thatEquals(attributeValue);
								break;
							case "PROJECT_PERM_ID":
								criteria.withProject().withPermId().thatEquals(attributeValue);
								break;
							case "PROJECT_SPACE":
								criteria.withProject().withSpace().withCode().thatEquals(attributeValue);
								break;
						}
					}
					
					switch(fieldType) {
						case "All":
							searchCriteria.withAnyField().thatEquals(fieldValue);
							break;
						case "Property":
							setPropertyCriteria(searchCriteria, fieldName, fieldValue);
							break;
						case "Attribute":
							setAttributeCriteria(searchCriteria, fieldName, fieldValue);
							break;
						case "Sample":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(searchCriteria.withSample(), fieldName, fieldValue);
									break;
								case "ATTR":
									setAttributeCriteria(searchCriteria.withSample(), fieldName, fieldValue);
									break;
							}
							break;
						case "Experiment":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(searchCriteria.withExperiment(), fieldName, fieldValue);
									break;
								case "ATTR":
									setAttributeCriteria(searchCriteria.withExperiment(), fieldName, fieldValue);
									break;
							}
							break;
						case "Parent":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(searchCriteria.withParents(), fieldName, fieldValue);
									break;
								case "ATTR":
									setAttributeCriteria(searchCriteria.withParents(), fieldName, fieldValue);
									break;
							}
							break;
						case "Children":
							switch(fieldNameType) {
								case "PROP":
									setPropertyCriteria(searchCriteria.withChildren(), fieldName, fieldValue);
									break;
								case "ATTR":
									setAttributeCriteria(searchCriteria.withChildren(), fieldName, fieldValue);
									break;
							}
							break;
					}
				}
				
				v3Api[searchMethodName](searchCriteria, fetchOptions)
				.done(function(result) {
					callback(result);  //this will call the method defined in the AdvancedSearchController which will display the table
				})
				.fail(function(result) {
					Util.showError("Call failed to server: " + JSON.stringify(result));
				});
			} catch(exception) {
				Util.showError(exception.name + ": " + exception.message);
			}
		});
	}
	
	//
	// Search Samples
	//
	
	this.getV3SamplesAsV1 = function(v3Samples) {
		var v1Samples = [];
		for(var sIdx = 0; sIdx < v3Samples.length; sIdx++) {
			v1Samples.push(this.getV3SampleAsV1(v3Samples[sIdx]));
		}
		return v1Samples;
	}
	
	this.getV3SampleAsV1 = function(v3Sample) {
		var CONST_UNSUPPORTED_NUMBER = -1;
		var CONST_UNSUPPORTED_OBJ = null;
		var CONST_UNSUPPORTED_BOOL = false;
		
		var v1Sample = {};
		v1Sample["@type"] = "Sample";
		v1Sample["@id"] = CONST_UNSUPPORTED_NUMBER;
		v1Sample["spaceCode"] = v3Sample.space.code;
		v1Sample["permId"] = v3Sample.permId.permId;
		v1Sample["code"] = v3Sample.code;
		v1Sample["identifier"] = v3Sample.identifier.identifier;
		v1Sample["experimentIdentifierOrNull"] = (v3Sample.experiment)?v3Sample.experiment.identifier.identifier:null;
		v1Sample["sampleTypeCode"] = v3Sample.type.code;
		v1Sample["properties"] = v3Sample.properties;
		
		v1Sample["registrationDetails"] = {};
		v1Sample["registrationDetails"]["@type"] = "EntityRegistrationDetails";
		v1Sample["registrationDetails"]["@id"] = CONST_UNSUPPORTED_NUMBER;
		v1Sample["registrationDetails"]["userFirstName"] = v3Sample.registrator.firstName;
		v1Sample["registrationDetails"]["userLastName"] = v3Sample.registrator.lastName;
		v1Sample["registrationDetails"]["userEmail"] = v3Sample.registrator.email;
		v1Sample["registrationDetails"]["userId"] = v3Sample.registrator.userId;
		v1Sample["registrationDetails"]["modifierFirstName"]  = (v3Sample.modifier)?v3Sample.modifier.firstName:null;
		v1Sample["registrationDetails"]["modifierLastName"] = (v3Sample.modifier)?v3Sample.modifier.lastName:null;
		v1Sample["registrationDetails"]["modifierEmail"] = (v3Sample.modifier)?v3Sample.modifier.email:null;
		v1Sample["registrationDetails"]["modifierUserId"] = (v3Sample.modifier)?v3Sample.modifier.userId:null;
		v1Sample["registrationDetails"]["registrationDate"] = v3Sample.registrator.registrationDate;
		v1Sample["registrationDetails"]["modificationDate"] = (v3Sample.modifier)?v3Sample.modifier.registrationDate:null;
		v1Sample["registrationDetails"]["accessTimestamp"] = CONST_UNSUPPORTED_OBJ;
		
		v1Sample["parents"] = null;
		if(v3Sample.parents) {
			v1Sample["parents"] = this.getV3SamplesAsV1(v3Sample.parents);
		}
		v1Sample["children"] = null;
		if(v3Sample.children) {
			v1Sample["children"] = this.getV3SamplesAsV1(v3Sample.children);
		} 
		
		v1Sample["stub"] = CONST_UNSUPPORTED_BOOL;
		v1Sample["metaprojects"] = CONST_UNSUPPORTED_OBJ;
		v1Sample["sampleTypeId"] = CONST_UNSUPPORTED_NUMBER;
		v1Sample["id"] = CONST_UNSUPPORTED_NUMBER;
		
		return v1Sample;
	}
	
	this.searchSamplesV3DSS = function(fechOptions, callbackFunction)
	{
		var localReference = this;
		fechOptions["method"] = "searchSamples";
		fechOptions["custom"] = profile.searchSamplesUsingV3OnDropboxRunCustom;
		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(), fechOptions, function(result) {
			if(result && result.result && result.result.rows[0][0].value === "OK") {
				var json = result.result.rows[0][2].value;
				var jsonParsed = JSON.parse(json);
				require(["util/Json"], function(Json){
					Json.fromJson("SearchResult", jsonParsed).done(function(data) {
						var v3Samples = data.objects;
						var samplesAsV1 = localReference.getV3SamplesAsV1(v3Samples);
						callbackFunction(samplesAsV1);
					}).fail(function() {
						alert("V3 dropbox search failed to be parsed.");
					});
				});
			} else {
				alert("V3 dropbox search failed to execute.");
			}
		});
	}
	
	this.searchSamplesV1 = function(fechOptions, callbackFunction)
	{	
		//Text Search
		var anyFieldContains = fechOptions["anyFieldContains"];
		
		//Attributes
		var samplePermId = fechOptions["samplePermId"];
		var withExperimentWithProjectPermId = fechOptions["withExperimentWithProjectPermId"];
		var sampleIdentifier = fechOptions["sampleIdentifier"];
		var sampleCode = fechOptions["sampleCode"];
		var sampleTypeCode = fechOptions["sampleTypeCode"];
		var registrationDate = fechOptions["registrationDate"];
		var modificationDate = fechOptions["modificationDate"];
		
		//Properties
		var properyKeyValueList = fechOptions["properyKeyValueList"];
		
		//Sub Queries
		var sampleExperimentIdentifier = fechOptions["sampleExperimentIdentifier"];
		var sampleContainerPermId = fechOptions["sampleContainerPermId"];
		
		//Hierarchy Options
		var withProperties = fechOptions["withProperties"];
		var withParents = fechOptions["withParents"];
		var withChildren = fechOptions["withChildren"];
		var withAncestors = fechOptions["withAncestors"];
		var withDescendants = fechOptions["withDescendants"];
		
		var matchClauses = [];
		
		// Free Text
		if(anyFieldContains) {
			var words = anyFieldContains.split(" ");
			for(var sIdx = 0; sIdx < words.length; sIdx++) {
				var word = words[sIdx];
				if(word) {
					matchClauses.push({
						"@type": "AnyFieldMatchClause",
						fieldType: "ANY_FIELD",
						desiredValue: "*" + word.trim() + "*"
					});
				}
			}
		}
		
		// Attributes
		if(samplePermId) {
			matchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "PERM_ID",
				desiredValue : samplePermId 
			});
		}
		
		if(sampleIdentifier) {
			matchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "SPACE",
				desiredValue : sampleIdentifier.split("/")[1] 
			});
			
			matchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "CODE",
				desiredValue : sampleIdentifier.split("/")[2] 
			});
		}
		
		if(sampleCode) {
			matchClauses.push({
			  	"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "CODE",
				desiredValue : sampleCode 
			});
		}
		
		if(sampleTypeCode) {
			matchClauses.push({
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "TYPE",
				desiredValue : sampleTypeCode
			});
		}
		
		if(registrationDate) {
			matchClauses.push({
				"@type":"TimeAttributeMatchClause",
				fieldType : "ATTRIBUTE",
				fieldCode : "REGISTRATION_DATE",
				desiredValue : registrationDate,
				compareMode : "EQUALS",
				timeZone : "+1",
				attribute : "REGISTRATION_DATE"
			});
		}
		
		if(modificationDate) {
			matchClauses.push({
				"@type":"TimeAttributeMatchClause",
				fieldType : "ATTRIBUTE",
				fieldCode : "MODIFICATION_DATE",
				desiredValue : modificationDate,
				compareMode : "EQUALS",
				timeZone : "+1",
				attribute : "MODIFICATION_DATE"
			});
		}
		
		//Properties
		if(properyKeyValueList) {
			for(var kvIdx = 0; kvIdx < properyKeyValueList.length; kvIdx++) {
				var properyKeyValue = properyKeyValueList[kvIdx];
				for(properyTypeCode in properyKeyValue) {
					matchClauses.push(
							{	
								"@type":"PropertyMatchClause",
								fieldType : "PROPERTY",
								fieldCode : properyTypeCode,
								propertyCode : properyTypeCode,
								desiredValue : "\"" + properyKeyValue[properyTypeCode] + "\"",
								compareMode : "EQUALS"
							}
						);
				}
			}
		}
		
		//Sub Queries
		var subCriterias = [];
		
		if(withExperimentWithProjectPermId) {
			subCriterias.push({
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "EXPERIMENT",	
				"criteria" : {
					matchClauses : [{
							"@type":"AttributeMatchClause",
							fieldType : "ATTRIBUTE",			
							attribute : "PROJECT_PERM_ID",
							desiredValue : withExperimentWithProjectPermId
					}],
					operator : "MATCH_ALL_CLAUSES"
				}
			});
		}
		
		if(sampleExperimentIdentifier) {
			var sampleExperimentIdentifierParts = sampleExperimentIdentifier.split("/");
			subCriterias.push({
					"@type" : "SearchSubCriteria",
					"targetEntityKind" : "EXPERIMENT",	
					"criteria" : {
						matchClauses : [{
								"@type":"AttributeMatchClause",
								fieldType : "ATTRIBUTE",			
								attribute : "SPACE",
								desiredValue : sampleExperimentIdentifierParts[1]
							},{
								"@type":"AttributeMatchClause",
								fieldType : "ATTRIBUTE",			
								attribute : "PROJECT",
								desiredValue : sampleExperimentIdentifierParts[2]
							}, {
								"@type":"AttributeMatchClause",
								fieldType : "ATTRIBUTE",			
								attribute : "CODE",
								desiredValue : sampleExperimentIdentifierParts[3]
							}],
						operator : "MATCH_ALL_CLAUSES"
				}
			});
		}
		
		if(sampleContainerPermId) {
			subCriterias.push({
				"@type" : "SearchSubCriteria",
				"targetEntityKind" : "SAMPLE_CONTAINER",
				"criteria" : {
					matchClauses : [{
							"@type":"AttributeMatchClause",
							fieldType : "ATTRIBUTE",			
							attribute : "PERM_ID",
							desiredValue : sampleContainerPermId
						}],
					operator : "MATCH_ALL_CLAUSES"
				}
			});
		}
		
		var sampleCriteria = {
			matchClauses : matchClauses,
			subCriterias : subCriterias,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		//Hierarchy Options
		var options = [];
		
		if(withProperties) {
			options.push("PROPERTIES");
		}
		
		if(withAncestors) {
			options.push("ANCESTORS");
		}
		
		if(withDescendants) {
			options.push("DESCENDANTS");
		}
		
		if(withParents) {
			options.push("PARENTS");
		}
		
		if(withChildren) {
			options.push("CHILDREN");
		}
		
		var localReference = this;
		this.openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, options, function(data) {
			callbackFunction(localReference.getInitializedSamples(data.result));
		});
	}
	
	this.searchSamples = function(fechOptions, callbackFunction)
	{
		if(profile.searchSamplesUsingV3OnDropbox) {
			this.searchSamplesV3DSS(fechOptions, callbackFunction);
		} else {
			this.searchSamplesV1(fechOptions, callbackFunction);
		}
	}
	
	this.searchWithUniqueId = function(samplePermId, callbackFunction)
	{	
		this.searchSamples({
			"samplePermId" : samplePermId,
			"withProperties" : true,
			"withAncestors" : true,
			"withDescendants" : true
		}, callbackFunction);
	}
	
	this.searchWithUniqueIdV1 = function(samplePermId, callbackFunction)
	{	
		this.searchSamplesV1({
			"samplePermId" : samplePermId,
			"withProperties" : true,
			"withAncestors" : true,
			"withDescendants" : true
		}, callbackFunction);
	}
	
	this.searchByTypeWithParents = function(sampleType, callbackFunction)
	{
		this.searchSamples({
			"sampleTypeCode" : sampleType,
			"withProperties" : true,
			"withParents" : true,
		}, callbackFunction);
	}
	
	this.searchWithType = function(sampleType, sampleCode, includeAncestorsAndDescendants, callbackFunction)
	{
		this.searchSamples({
			"sampleTypeCode" : sampleType,
			"sampleCode" : sampleCode,
			"withProperties" : true,
			"withAncestors" : includeAncestorsAndDescendants,
			"withDescendants" : includeAncestorsAndDescendants
		}, callbackFunction);
	}
	
	this.searchWithExperiment = function(experimentIdentifier, projectPermId, properyKeyValueList, callbackFunction)
	{	
		this.searchSamples({
			"sampleExperimentIdentifier" : experimentIdentifier,
			"withExperimentWithProjectPermId" : projectPermId,
			"withProperties" : true,
			"withParents" : true,
			"properyKeyValueList" : properyKeyValueList
		}, callbackFunction);
	}
	
	this.searchWithProperties = function(propertyTypeCodes, propertyValues, callbackFunction, isComplete)
	{	
		var properyKeyValueList = [];
	
		for(var i = 0; i < propertyTypeCodes.length ;i++) {
			var propertyTypeCode = propertyTypeCodes[i];
			var propertyTypeValue = "\"" + propertyValues[i] + "\"";
			var newMap = {};
				newMap[propertyTypeCode] = propertyTypeValue;
				
			properyKeyValueList.push(newMap);
		}
		
		this.searchSamples({
			"withProperties" : true,
			"withAncestors" : isComplete,
			"withDescendants" : isComplete,
			"properyKeyValueList" : properyKeyValueList
		}, callbackFunction);
	}
	
	this.searchWithIdentifiers = function(sampleIdentifiers, callbackFunction)
	{
		var _this = this;
		var searchResults = [];
		var searchForIdentifiers = jQuery.extend(true, [], sampleIdentifiers);
		
		var searchNext = function() {
			if(searchForIdentifiers.length === 0) {
				callbackFunction(searchResults);
			} else {
				var next = searchForIdentifiers.pop();
				searchFunction(next);
			}
		}
		
		var searchFunction = function(sampleIdentifier) {
			_this.searchSamples({
				"withProperties" : true,
				"withAncestors" : true,
				"withDescendants" : true,
				"sampleIdentifier" : sampleIdentifier
			}, function(samples) {
				samples.forEach(function(sample) {
					searchResults.push(sample);
				});
				searchNext();
			});
		}
		
		searchNext();
	}
	
	this.searchContained = function(permId, callbackFunction) {
		this.searchSamples({
			"sampleContainerPermId" : permId,
			"withProperties" : true,
			"withParents" : true,
			"withChildren" : true
		}, callbackFunction);
	}
	
	this.getInitializedSamples = function(result) {
		
		//
		// Fill Map that uses as key the sample @id and value the sample object 
		//
		var samplesById = {};
		
		function storeSamplesById(originalSample)
		{
			var stack = [originalSample];
			
			var referredSample = null;
			while (referredSample = stack.pop()) {
				if (isNaN(referredSample)) {
					samplesById[referredSample["@id"]] = referredSample;
					if (referredSample.parents) {
						for(var i = 0, len = referredSample.parents.length; i < len; ++i) {
							stack.push(referredSample.parents[i]);
						}
					}
					if (referredSample.children) {
						for(var i = 0, len = referredSample.children.length; i < len; ++i) {
							stack.push(referredSample.children[i]);
						}
					}
				}					
			}
		}
		
		for(var i = 0; i < result.length; i++) {
			var sampleOrId = result[i];
			storeSamplesById(sampleOrId);
		}
		
		//
		// Fix Result List
		//
		var visitedSamples = {};
		function fixSamples(result)
		{
			for(var i = 0; i < result.length; i++)
			{
				var sampleOrId = result[i];
				
				if (isNaN(sampleOrId))
				{
					sampleOrId = samplesById[sampleOrId["@id"]];
				} else
				{
					sampleOrId = samplesById[sampleOrId]; 
				}
				result[i] = sampleOrId;
				if(visitedSamples[sampleOrId.permId]) {
					continue;
				} else {
					visitedSamples[sampleOrId.permId] = true;
				}
				
				//Fill Parents
				if(sampleOrId.parents) {
					for(var j = 0; j < sampleOrId.parents.length; j++) {
						var parentOrId = sampleOrId.parents[j];
						if(!isNaN(parentOrId)) { //If is an Id get the reference
							sampleOrId.parents[j] = samplesById[parentOrId];
						}
					}
					fixSamples(sampleOrId.parents);
				}
				
				//Fill Children
				if(sampleOrId.children) {
					for(var j = 0; j < sampleOrId.children.length; j++) {
						var childOrId = sampleOrId.children[j];
						if(!isNaN(childOrId)) { //If is an Id get the reference
							sampleOrId.children[j] = samplesById[childOrId];
						}
					}
					fixSamples(sampleOrId.children);
				}
			}
		}
		
		fixSamples(result);
		
		return result;
	}
	
	//
	// Global Search
	//
	this.searchGlobally = function(freeText, callbackFunction)
	{
		var _this = this;
		require(['openbis', 'as/dto/global/search/GlobalSearchCriteria', 
		         'as/dto/global/fetchoptions/GlobalSearchObjectFetchOptions'], 
		         function(openbis, GlobalSearchCriteria, GlobalSearchObjectFetchOptions){
			var protocol = window.location.protocol;
			var host = window.location.hostname;
			var port = window.location.port;
			var url = protocol + "//" + host + ":" + port;
			var v3api = new openbis(url + "/openbis/openbis/rmi-application-server-v3.json");
			v3api._private.sessionToken = mainController.serverFacade.getSession();
			var searchCriteria = new GlobalSearchCriteria();
			searchCriteria.withWildCards();
			searchCriteria.withText().thatContains(freeText);
			var fetchOptions = new GlobalSearchObjectFetchOptions();
			var sampleFetchOptions = fetchOptions.withSample();
			sampleFetchOptions.withSpace();
			sampleFetchOptions.withType();
			sampleFetchOptions.withRegistrator();
			sampleFetchOptions.withModifier();
			sampleFetchOptions.withExperiment();
			sampleFetchOptions.withProperties();
			
			var experimentFetchOptions = fetchOptions.withExperiment();
			experimentFetchOptions.withType();
			experimentFetchOptions.withRegistrator();
			experimentFetchOptions.withModifier();
			experimentFetchOptions.withProperties();
			
			var dataSetFetchOptions = fetchOptions.withDataSet();
			dataSetFetchOptions.withType();
			dataSetFetchOptions.withRegistrator();
			dataSetFetchOptions.withModifier();
			dataSetFetchOptions.withProperties();
			
			v3api.searchGlobally(searchCriteria, fetchOptions).done(function(results) {
				callbackFunction(results);
			}).fail(function(error) {
				Util.showError("Call failed to server: " + JSON.stringify(error));
				Util.unblockUI();
			});
		});
	}
	
	//
	// Legacy Global Search
	//
	this.searchWithText = function(freeText, callbackFunction)
	{
		var _this = this;
		var regEx = /\d{4}-\d{2}-\d{2}/g;
		var match = freeText.match(regEx);
		
		if(match && match.length === 1) { //Search With Date Mode, we merge results with dates found on registration and modification fields what is slow for large number of entities
			this.searchSamples(this._getCriteriaWithDate(freeText, true, false), function(samples1) {
				_this.searchSamples(_this._getCriteriaWithDate(freeText, false, true), function(samples2) {
					_this.searchSamples(_this._getCriteriaWithDate(freeText, false, false), function(samples3) {
						var results = samples1.concat(samples2).concat(samples3).uniqueOBISEntity();
						callbackFunction(results);
					});
				});
			});
		} else if(match && match.length > 1) {
			Util.showError("Search only supports one date at a time!");
			callbackFunction([]);
		} else { //Normal Search
			this.searchSamples(this._getCriteriaWithDate(freeText, false, false), function(samples) {
				callbackFunction(samples);
			});
			callbackFunction([]);
		}
	}
	
	this._getCriteriaWithDate = function(freeText, isRegistrationDate, isModificationDate) {
		//Find dates on string and delete them to use them differently on the search
		var regEx = /\d{4}-\d{2}-\d{2}/g;
		var match = freeText.match(regEx);
		freeText = freeText.replace(regEx, "");
		if(!isRegistrationDate && !isModificationDate && match && match.length > 0) {
			for(var mIdx = 0; mIdx < match.length; mIdx++) {
				freeText += " " + match[mIdx].replace(/-/g, "");
			}
		}
		
		//Build Search
		var sampleCriteria = {
			"withProperties" : true
		};
		
		if(freeText) {
			sampleCriteria["anyFieldContains"] = freeText;
		}
		
		if(match && match.length > 0) {
			for(var mIdx = 0; mIdx < match.length; mIdx++) {
				if(isRegistrationDate) {
					sampleCriteria["registrationDate"] = match[mIdx];
				}
				
				if(isModificationDate) {
					sampleCriteria["modificationDate"] = match[mIdx];
				}
			}
		}
		
		return sampleCriteria;
	}
	
	//
	// Search Domains
	//
	this.listSearchDomains = function(callbackFunction) {
		if(this.openbisServer.listAvailableSearchDomains) {
			this.openbisServer.listAvailableSearchDomains(callbackFunction);
		} else {
			callbackFunction();
		}
	}
	
	this.searchOnSearchDomain = function(preferredSearchDomainOrNull, searchText, callbackFunction) {
		
		//TO-DO: For testing please put codes that exist in your database and you can access, the rest leave it as it is, when done just pass null to the function.
		var optionalParametersOrNull = {
				"SEQ-1" : JSON.stringify({
					"searchDomain" : "Echo database",
					"dataSetCode" : "20141023091944740-99",
					"pathInDataSet" : "PATH-1",
					"sequenceIdentifier" : "ID-1",
					"positionInSequence" : "1"
				}),
				"SEQ-2" : JSON.stringify({
					"searchDomain" : "Echo database",
					"dataSetCode" : "20141023091930970-98",
					"pathInDataSet" : "PATH-2",
					"sequenceIdentifier" : "ID-2",
					"positionInSequence" : "2"
				})
		}
		
		this.openbisServer.searchOnSearchDomain(preferredSearchDomainOrNull, searchText, null, callbackFunction);
	}
}