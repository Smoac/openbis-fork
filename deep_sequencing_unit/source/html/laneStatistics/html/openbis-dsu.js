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
 * Get the data sets for a sample specified by sample identifier
 */
openbis_dsu.prototype.retrieveDataSetsForSample = function(sampleIdentifier, action)
{
    var sampleIdentifierTokens = sampleIdentifier.split("/");
    var sampleCriteria = 
    {
	targetEntityKind : "SAMPLE",
	criteria : { 
	    matchClauses : 
	    [ {"@type":"AttributeMatchClause",
	       "attribute":"CODE",
	       "fieldType":"ATTRIBUTE",
	       "desiredValue": sampleIdentifierTokens[2]
		}]
	}
    };
	
    var dataSetCriteria = 
    {
	subCriterias : [ sampleCriteria ],
	matchClauses : 
	[ {"@type":"AttributeMatchClause",
	   attribute : "TYPE",
	   fieldType : "ATTRIBUTE",
	   desiredValue : "FASTQ_GZ" 
	    } ],
	operator : "MATCH_ALL_CLAUSES"
    };

    this.server.searchForDataSets(dataSetCriteria, action)
}

/**
 * Get all data sets connected to a sequencing sample
 */
openbis_dsu.prototype.retrieveDataSetsForSequencingSample = function(sequencing, action)
{
	this.server.listDataSetsForSample(sequencing.bis, false, action);
}

