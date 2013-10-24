/*
 * Copyright 2013 ETH Zuerich, CISD
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
	// Login Related Functions
	//
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
	// Metadata Related Functions
	//
	this.listSampleTypes = function(callbackFunction) {
		this.openbisServer.listSampleTypes(callbackFunction);
	}
	
	this.listVocabularies = function(callbackFunction) {
		this.openbisServer.listVocabularies(callbackFunction);
	}

	this.listSpacesWithProjectsAndRoleAssignments = function(somethingOrNull, callbackFunction) {
		this.openbisServer.listSpacesWithProjectsAndRoleAssignments(somethingOrNull, callbackFunction);
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

	this.fileUpload = function(fileComponentId, callbackFunction) {
		//File
		var file = document.getElementById(fileComponentId).files[0];
		
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

	//
	// Sample Create/Update Functions
 	//
 	this.createReportFromAggregationService = function(dataStoreCode, parameters, callbackFunction) {
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
		var finalSamples = [];
		
		for(var i = 0; i < result.length; i++) {
			var sampleOrId = result[i];
			if (isNaN(sampleOrId))
			{
				sampleOrId = samplesById[sampleOrId["@id"]];
			} else
			{
				sampleOrId = samplesById[sampleOrId]; 
			}
			
			//Fill Parents - Only 1 Level without recursion
			if(sampleOrId.parents) {
				for(var j = 0; j < sampleOrId.parents.length; j++) {
					var parentOrId = sampleOrId.parents[j];
					if(!isNaN(parentOrId)) { //If is an Id get the reference
						sampleOrId.parents[j] = samplesById[parentOrId];
					}
				}
			}
			
			//Fill Children - Only 1 Level without recursion
			if(sampleOrId.children) {
				for(var j = 0; j < sampleOrId.children.length; j++) {
					var childOrId = sampleOrId.children[j];
					if(!isNaN(childOrId)) { //If is an Id get the reference
						sampleOrId.children[j] = samplesById[childOrId];
					}
				}
			}
			
			finalSamples.push(sampleOrId);
		}
	
		return finalSamples;
	}
	
	this.searchWithUniqueId = function(sampleIdentifier, callbackFunction)
	{	
		var matchClauses = [
				{
					"@type":"AttributeMatchClause",
					fieldType : "ATTRIBUTE",			
					attribute : "PERM_ID",
					desiredValue : sampleIdentifier 
				}		
		]
		
		var sampleCriteria = 
		{
			matchClauses : matchClauses,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		var localReference = this;
		this.openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
			callbackFunction(localReference.getInitializedSamples(data.result));
		});
	}
	
	this.searchWithType = function(sampleType, sampleCode, callbackFunction)
	{	
		var matchClauses = [ {"@type":"AttributeMatchClause",
					fieldType : "ATTRIBUTE",			
					attribute : "TYPE",
					desiredValue : sampleType
				}
		]
		
		if(sampleCode){
		  matchClauses.push(
			  		{
				  	"@type":"AttributeMatchClause",
					fieldType : "ATTRIBUTE",			
					attribute : "CODE",
					desiredValue : sampleCode 
				}		
		  );
		}
		
		var sampleCriteria = 
		{
			matchClauses : matchClauses,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		var localReference = this;
		this.openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
			callbackFunction(localReference.getInitializedSamples(data.result));
		});
	}
	
	this.searchWithProperties = function(propertyTypeCodes, propertyValues, callbackFunction)
	{	
		var matchClauses = [];
		
		for(var i = 0; i < propertyTypeCodes.length ;i++) {
			matchClauses.push(
				{	
					"@type":"PropertyMatchClause",
					fieldType : "PROPERTY",			
					propertyCode : propertyTypeCodes[i],
					desiredValue : propertyValues[i]
				}
			);
		}
		
		var sampleCriteria = 
		{
			matchClauses : matchClauses,
			operator : "MATCH_ALL_CLAUSES"
		};
		
		var localReference = this;
		this.openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES"], function(data) {
			callbackFunction(data.result);
		});
	}
	
	
	
	this.searchWithText = function(freeText, callbackFunction)
	{	
		var sampleCriteria = {
			matchClauses: [{
				"@type": "AnyFieldMatchClause",
				fieldType: "ANY_FIELD",
				desiredValue: "*" + freeText + "*"
			}],
			operator: "MATCH_ANY_CLAUSES"
		};
		
		var localReference = this;
		this.openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
			callbackFunction(localReference.getInitializedSamples(data.result));
		});
	}
}