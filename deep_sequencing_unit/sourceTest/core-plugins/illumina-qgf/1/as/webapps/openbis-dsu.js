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
 * Request a Sample using the code
 */
openbis_dsu.prototype.retrieveSample = function(sampleIdentifier, action)
{
        var sampleCriteria =
        {
                matchClauses :
                        [ {"@type":"AttributeMatchClause",
                                attribute : "CODE",
                                fieldType : "ATTRIBUTE",
                                desiredValue : sampleIdentifier
                        } ],
                operator : "MATCH_ALL_CLAUSES"
        };

        this.server.searchForSamples(sampleCriteria, action);
}


/**
 * Request a Sample with Properties using the code
 */
openbis_dsu.prototype.retrieveSampleWithPropertiesCode = function(sampleIdentifier, action)
{
        var sampleCriteria =
        {
                matchClauses :
                        [ {"@type":"AttributeMatchClause",
                                attribute : "CODE",
                                fieldType : "ATTRIBUTE",
                                desiredValue : sampleIdentifier
                        } ],
                operator : "MATCH_ALL_CLAUSES"
        };

        var fetchOptions = ["PROPERTIES"];

        var thisDsu = this;
        this.server.searchForSamplesWithFetchOptions(sampleCriteria, fetchOptions, function(response){
                response.result = thisDsu.getInitializedSamples(response.result);
                action(response);
        });
}


/**
 * Request a Sample with Properties using the permID
 */
openbis_dsu.prototype.retrieveSampleWithProperties = function(samplePermId, action)
{
        var sampleCriteria =
        {
                matchClauses :
                        [ {"@type":"AttributeMatchClause",
                                attribute : "PERM_ID",
                                fieldType : "ATTRIBUTE",
                                desiredValue : samplePermId
                        } ],
                operator : "MATCH_ALL_CLAUSES"
        };

        var fetchOptions = ["PROPERTIES"];

        var thisDsu = this;
        this.server.searchForSamplesWithFetchOptions(sampleCriteria, fetchOptions, function(response){
                response.result = thisDsu.getInitializedSamples(response.result);
                action(response);
        });
}



/**
 * Request Parent Samples including properties
 */

openbis_dsu.prototype.retrieveSampleParents = function(samplePermId, action)
{
    var sampleCriteria =
    {
        targetEntityKind : "SAMPLE_CHILD",
        criteria : {
            matchClauses :
            [ {"@type":"AttributeMatchClause",
               "attribute":"PERM_ID",
               "fieldType":"ATTRIBUTE",
               "desiredValue": samplePermId
                }]
        }
    };

    var parentCriteria =
    {
        subCriterias : [ sampleCriteria ],
        matchClauses :
        [ 
             ],
        operator : "MATCH_ALL_CLAUSES"
    };

    var fetchOptions = ["PROPERTIES"];

    var thisDsu = this;
    this.server.searchForSamplesWithFetchOptions(parentCriteria, fetchOptions, function(response){
      response.result = thisDsu.getInitializedSamples(response.result);
      action(response);
    });
}


/**
 * Request Child Samples including properties
 */

openbis_dsu.prototype.retrieveSampleChildren = function(samplePermIds, action)
{
	var permIdClauses = [];
	  
	  samplePermIds.forEach(function(samplePermId){
		var clause = {"@type":"AttributeMatchClause",
	               "attribute":"PERM_ID",
	               "fieldType":"ATTRIBUTE",
	               "desiredValue": samplePermId
	                }
		permIdClauses.push(clause)
	});
	
    var sampleCriteria =
    {
        targetEntityKind : "SAMPLE_PARENT",
        criteria : {
            matchClauses : permIdClauses,
            operator : "MATCH_ANY_CLAUSES"
        }
    };

    var childCriteria =
    {
        subCriterias : [ sampleCriteria ],
        matchClauses :
        [
             ],
        operator : "MATCH_ALL_CLAUSES"
    };

    var fetchOptions = ["PROPERTIES", "PARENTS"];

    var thisDsu = this;
    this.server.searchForSamplesWithFetchOptions(childCriteria, fetchOptions, function(response){
      response.result = thisDsu.getInitializedSamples(response.result);
      action(response);
    });
}

/**
 * Get the data sets for a sample specified by sample permId
 */
openbis_dsu.prototype.retrieveDataSetsForSampleWithPermIds = function(samplePermIds, action)
{
    var clauses = [];

    samplePermIds.forEach(function(samplePermId){
      var clause = {"@type":"AttributeMatchClause",
               "attribute":"PERM_ID",
               "fieldType":"ATTRIBUTE",
               "desiredValue": samplePermId
                }
       clauses.push(clause)
    });

    var sampleCriteria =
    {
        targetEntityKind : "SAMPLE",
        criteria : {
            matchClauses : clauses,
            operator: "MATCH_ANY_CLAUSES"
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

openbis_dsu.prototype.getInitializedSamples = function(result) {
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
