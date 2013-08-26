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