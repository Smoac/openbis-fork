/**
 * openbis-dsu.js
 * OpenBIS-DSU API
 *
 * A DSU-specific API for accessing openBIS. Depends on openbis.js.
 * @author Chandrasekhar Ramakrishnan
 */

/**
 * The openbis_dsu object provides a dsu-specific interface to openbis. 
 * 
 * It creates objects for projects, experiments, samples, and datasets. These objects
 * are designed to be passed on to GUI libraries (like d3). The openbis version of each
 * object is stored in the bis variable.
 * @constructor
 */
function openbis_dsu(url, dssUrl) {
	this.server = new openbis(url, dssUrl);
}

/**
 * Request the sequencing samples for a project
 */
openbis_dsu.prototype.retrieveSequencingSamples = function(action)
{	
	var sampleCriteria = 
	{
		matchClauses : 
			[ {"@type":"AttributeMatchClause",
				attribute : "TYPE",
				fieldType : "ATTRIBUTE",
				desiredValue : "ILLUMINA_SEQUENCING" 
			} ],
		operator : "MATCH_ALL_CLAUSES"
	};

	this.server.searchForSamples(sampleCriteria, action);
}

/**
 * Get the flow lanes for a sequencing sample.
 */
openbis_dsu.prototype.retrieveFlowLanesForSequencingSample = function(sample, action)
{
	var projectCode = null;
	
	if(sample.bis.experimentIdentifierOrNull){
		var experimentIdentifierRegexp = /\/(.*)\/(.*)\/(.*)/g;
		var experimentIdentifierMatch = experimentIdentifierRegexp.exec(sample.bis.experimentIdentifierOrNull);
		projectCode = experimentIdentifierMatch[2];
	}else{
		action(null);
		return;
	}
	
	var experimentCriteria = 
	{
		targetEntityKind : "EXPERIMENT",
		criteria : { 
			matchClauses : 
			[ {"@type":"AttributeMatchClause",
				"attribute":"PROJECT",
				"fieldType":"ATTRIBUTE",
				"desiredValue": projectCode
			} ]
		}
	};
	
	var parentCriteria = 
	{
		targetEntityKind : "SAMPLE_PARENT",
		criteria : { 
			matchClauses : 
			[ {"@type":"AttributeMatchClause",
				"attribute":"CODE",
				"fieldType":"ATTRIBUTE",
				"desiredValue": sample.bis.code 
			} ]
		}
	};
	
	var sampleCriteria = 
	{
		subCriterias : [ experimentCriteria, parentCriteria ],
		matchClauses : 
			[ {"@type":"AttributeMatchClause",
				attribute : "TYPE",
				fieldType : "ATTRIBUTE",
				desiredValue : "ILLUMINA_FLOW_LANE" 
			} ],
		operator : "MATCH_ALL_CLAUSES"
	};

	this.server.searchForSamples(sampleCriteria, action)
}

/**
 * Get all data sets connected to a sequencing sample
 */
openbis_dsu.prototype.retrieveDataSetsForSequencingSample = function(sequencing, action)
{
	this.server.listDataSetsForSample(sequencing.bis, false, action);
}
