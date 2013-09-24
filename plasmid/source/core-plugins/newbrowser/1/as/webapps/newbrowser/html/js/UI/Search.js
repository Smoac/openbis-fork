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
 * Utility class Search, created as anonimous.
 *
 * Contains methods used to search for samples.
 */
var Search = new function() {
	
	this.getInitializedSamples = function(result) {
		var samplesById = {};
		var finalSamples = [];
		
		//
		// Fill Map
		//
		function storeSamplesById(referredSample)
		{
			if (isNaN(referredSample)) {
				samplesById[referredSample["@id"]] = referredSample;
				if (referredSample.parents) {
					referredSample.parents.forEach(storeSamplesById);
				}
				if (referredSample.children) {
					referredSample.children.forEach(storeSamplesById);
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
		for(var i = 0; i < result.length; i++) {
			var sampleOrId = result[i];
			if (isNaN(sampleOrId))
			{
				sampleOrId = samplesById[sampleOrId["@id"]];
			} else
			{
				sampleOrId = samplesById[sampleOrId]; 
			}
			
			//Fill Parents - 1 Level
			if(sampleOrId.parents) {
				for(var j = 0; j < sampleOrId.parents.length; j++) {
					var parentOrId = sampleOrId.parents[j];
					if(!isNaN(parentOrId)) { //If is an Id get the reference
						sampleOrId.parents[j] = samplesById[parentOrId];
					}
				}
			}
			
			//Fill Children - 1 Level
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
		openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
			callbackFunction(profile.searchSorter(localReference.getInitializedSamples(data.result)));
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
		openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
			callbackFunction(profile.searchSorter(localReference.getInitializedSamples(data.result)));
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
		openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES"], function(data) {
			callbackFunction(profile.searchSorter(data.result));
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
		openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
			callbackFunction(localReference.getInitializedSamples(data.result));
		});
	}
	
}