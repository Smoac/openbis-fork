/**
 * OpenBIS-BaSynthec API
 *
 * A BaSynthec-specific API for accessing openBIS. Depends on openbis.js.
 */

STRAIN_PROP_NAME = "STRAIN_NAMES";
	
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