/**
 * OpenBIS-BaSynthec API
 *
 * A BaSynthec-specific API for accessing openBIS. Depends on openbis.js.
 */

STRAIN_PROP_NAME = "STRAIN_NAMES";

// A date formatter used to display data sets
var timeformat = d3.time.format("%Y-%m-%d %H:%M");

	
/**
 * The openbis_basynthec object provides a basynthec-specific interface to openbis. 
 * 
 * It creates objects for projects, experiments, samples, and datasets. These objects
 * are designed to be passed on to GUI libraries (like d3). The openbis version of each
 * object is stored in the bis variable.
 * 
 * @constructor
 */
function openbis_basynthec(url, dssUrl) {
	this.server = new openbis(url, dssUrl);
	// Initial projects
	var dataSets = [{code: "No Projects"}];
}

/**
 * List all data sets from the openBIS server.
 */
openbis_basynthec.prototype.listAllDataSets = function(action)
{
	var dataSetCriteria = 
	{
		matchClauses : [ 
			{"@type":"AttributeMatchClause",
				attribute : "CODE",
				fieldType : "ATTRIBUTE",
				desiredValue : "*" 
			}
		],
		operator : "MATCH_ALL_CLAUSES"
	};

	this.server.searchForDataSets(dataSetCriteria, action);
}

/**
 * Return a list of strains for this data set. List could be empty.
 */
openbis_basynthec.prototype.getStrains = function(dataSet)
{
	var strains = dataSet.properties[STRAIN_PROP_NAME]

	if (strains) 
	{
		return strains.split(",");
	} 
	else 
	{
		return [];
	}
}

openbis_basynthec.prototype.getStrainsPhenotypesAndPredictions = function(action){
	this.server.createReportFromAggregationService("DSS1","chicago", null, function(response){
		var result = [];
		
		if(response.result && response.result.rows){
			$.each(response.result.rows, function(index, row){
				strain = eval("(" + row[0].value + ")");
				strain.name = strain.id;
				strain.hasPredictions = strain.predictions != null && strain.predictions.length > 0;
				strain.hasPhenotypes = strain.phenotypes != null && strain.phenotypes.length > 0;
				result[strain.id] = strain;
			});
		}
		
		action(result);
	});
}
