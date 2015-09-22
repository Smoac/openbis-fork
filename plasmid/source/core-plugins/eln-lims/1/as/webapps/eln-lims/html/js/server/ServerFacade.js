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
			}
		}
		
		if(action && !isError){
			action(response);
		}
	}
	
	this.openbisServer.setResponseInterceptor(responseInterceptor);
	
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
	
	this.initServices = function(username, password, callbackFunction) {
		var defaultDataSetCode = profile.getDefaultDataStoreCode();
		mainController.serverFacade.createReportFromAggregationService(defaultDataSetCode, {
			"method" : "initServices",
			"username" : username,
			"password" : password,
			"openBISURL" : mainController.serverFacade.openbisServer._internal.openbisUrl
		}, function(result) {
			callbackFunction();
		});
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
		this.openbisServer.listExperiments(projects, null, callbackFunction);
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
		
		this.openbisServer.searchForSamples(sampleCriteria, callbackFunction)
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
		this.openbisServer.listDataSetsForSample(sampleToSend, trueOrFalse, callbackFunction);
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
 	this.createReportFromAggregationService = function(dataStoreCode, parameters, callbackFunction) {
 		if(!parameters) {
 			parameters = {};
 		}
 		parameters["sessionToken"] = this.openbisServer.getSession();
		this.openbisServer.createReportFromAggregationService(dataStoreCode, "newbrowserapi", parameters, callbackFunction);
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
	// Search Samples
	//
	this.searchSamples = function(fechOptions, callbackFunction)
	{	
		//Attributes
		var samplePermId = fechOptions["samplePermId"];
		var sampleIdentifier = fechOptions["sampleIdentifier"];
		var sampleCode = fechOptions["sampleCode"];
		var sampleTypeCode = fechOptions["sampleTypeCode"];
		
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
		
//		var localReference = this;
//		fechOptions["method"] = "searchSamples";
//		fechOptions["openBISURL"] = this.openbisServer._internal.openbisUrl;
//		this.createReportFromAggregationService(profile.getDefaultDataStoreCode(), fechOptions, function(result) {
//			if(result && result.result && result.result.rows[0][0].value === "OK") {
//				var json = result.result.rows[0][2].value;
//				var jsonParsed = JSON.parse(json);
//				var samples = JSON.parse(json).objects;
//			} else {
//				alert("V3 Dropbox Search Failed.");
//			}
//		});
		
		var matchClauses = [];
		
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
		
		var subCriterias = [];
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
	
	this.searchWithUniqueId = function(samplePermId, callbackFunction)
	{	
		this.searchSamples({
			"samplePermId" : samplePermId,
			"withProperties" : true,
			"withAncestors" : true,
			"withDescendants" : true
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
	
	this.searchWithExperiment = function(experimentIdentifier, callbackFunction)
	{	
		this.searchSamples({
			"sampleExperimentIdentifier" : experimentIdentifier,
			"withProperties" : true,
			"withParents" : true
		}, callbackFunction);
	}
	
	this.searchWithProperties = function(propertyTypeCodes, propertyValues, callbackFunction, isComplete)
	{	
		var properyKeyValueList = [];
	
		for(var i = 0; i < propertyTypeCodes.length ;i++) {
			var propertyTypeCode = propertyTypeCodes[i];
			var propertyTypeValue = propertyValues[i];
			properyKeyValueList.push({
				propertyTypeCode : "\"" + propertyTypeValue + "\""
			});
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
	// Free Text Search
	//
	this.searchWithText = function(freeText, callbackFunction)
	{
		var _this = this;
		var regEx = /\d{4}-\d{2}-\d{2}/g;
		var match = freeText.match(regEx);
		
		if(match && match.length === 1) { //Search With Date Mode, we merge results with dates found on registration and modification fields what is slow for large number of entities
			this.openbisServer.searchForSamplesWithFetchOptions(this._getCriteriaWithDate(freeText, true, false), ["PROPERTIES"], function(data1) {
				_this.openbisServer.searchForSamplesWithFetchOptions(_this._getCriteriaWithDate(freeText, false, true), ["PROPERTIES"], function(data2) {
					_this.openbisServer.searchForSamplesWithFetchOptions(_this._getCriteriaWithDate(freeText, false, false), ["PROPERTIES"], function(data3) {
						var results1 = _this.getInitializedSamples(data1.result);
						var results2 = _this.getInitializedSamples(data2.result);
						var results3 = _this.getInitializedSamples(data3.result);
						var resultsF = results1.concat(results2).concat(results3).uniqueOBISEntity();
						callbackFunction(resultsF);
					});
				});
			});
		} else if(match && match.length > 1) {
			Util.showError("Search only supports one date at a time!");
			callbackFunction([]);
		} else { //Normal Search
			this.openbisServer.searchForSamplesWithFetchOptions(this._getCriteriaWithDate(freeText, false, false), ["PROPERTIES"], function(data) {
				callbackFunction(_this.getInitializedSamples(data.result));
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
			matchClauses: [],
			operator: "MATCH_ALL_CLAUSES"
		};
		
		if(freeText) {
			sampleCriteria.matchClauses.push({
				"@type": "AnyFieldMatchClause",
				fieldType: "ANY_FIELD",
				desiredValue: "*" + freeText.trim() + "*"
			});
		}
		
		if(match && match.length > 0) {
			for(var mIdx = 0; mIdx < match.length; mIdx++) {
				if(isRegistrationDate) {
					sampleCriteria.matchClauses.push({
						"@type":"TimeAttributeMatchClause",
						fieldType : "ATTRIBUTE",
						fieldCode : "REGISTRATION_DATE",
						desiredValue : match[mIdx],
						compareMode : "EQUALS",
						timeZone : "+1",
						attribute : "REGISTRATION_DATE"
					});
				}
				
				if(isModificationDate) {
					sampleCriteria.matchClauses.push({
						"@type":"TimeAttributeMatchClause",
						fieldType : "ATTRIBUTE",
						fieldCode : "MODIFICATION_DATE",
						desiredValue : match[mIdx],
						compareMode : "EQUALS",
						timeZone : "+1",
						attribute : "MODIFICATION_DATE"
					});
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