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
	// Initial projects
	var projects = [{code: "No Projects"}];
}

/**
 * Request the experiments for the project
 */
openbis_dsu.prototype.retrieveExperimentsForProject =  function(project, action)
{
	// Initialize the experiments
	this.server.listExperiments([project.bis], null, action);
}

/**
 * Request the sequencing samples for a project
 */
openbis_dsu.prototype.retrieveSequencingSamplesForProject = function(project, action)
{	
	// To serach for samples by project, we need to go through the experiment
	var experimentCriteria = 
	{
		targetEntityKind : "EXPERIMENT",
		criteria : { 
			matchClauses : 
			[ {"@type":"AttributeMatchClause",
				"attribute":"PROJECT",
				"fieldType":"ATTRIBUTE",
				"desiredValue": project.bis.code
			} ]
		}
	};
	
	var sampleCriteria = 
	{
		subCriterias : [ experimentCriteria ],
		matchClauses : 
			[ {"@type":"AttributeMatchClause",
				attribute : "TYPE",
				fieldType : "ATTRIBUTE",
				desiredValue : "ILLUMINA_SEQUENCING" 
			} ],
		operator : "MATCH_ALL_CLAUSES"
	};

	this.server.searchForSamples(sampleCriteria, action)
}

/**
 * Request the sequencing samples for an experiment
 */
openbis_dsu.prototype.retrieveSequencingSamplesForExperiment = function(experiment, action)
{	
	var experimentCriteria = 
	{
		targetEntityKind : "EXPERIMENT",
		criteria : { 
			matchClauses : 
			[ {"@type":"AttributeMatchClause",
				"attribute":"CODE",
				"fieldType":"ATTRIBUTE",
				"desiredValue": experiment.bis.code 
			},
			{"@type":"AttributeMatchClause",
				"attribute":"PROJECT",
				"fieldType":"ATTRIBUTE",
				"desiredValue": experiment.project.bis.code
			} ]
		}
	};
	
	var sampleCriteria = 
	{
		subCriterias : [ experimentCriteria ],
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
 * Request the replicas for the grid
 */
openbis_dsu.prototype.retrieveFlowLanesForSequencingSample = function(sample, action)
{
	var experimentCriteria = 
	{
		targetEntityKind : "EXPERIMENT",
		criteria : { 
			matchClauses : 
			[ {"@type":"AttributeMatchClause",
				"attribute":"PROJECT",
				"fieldType":"ATTRIBUTE",
				"desiredValue": sample.project.bis.code
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
 * Request the replicas for the grid
 */
openbis_dsu.prototype.retrieveFastqGzDataSetsForFlowLane = function(flowlane, action)
{
	var flowLaneCriteria = 
	{
		targetEntityKind : "SAMPLE",
		criteria : { 
			matchClauses : [ 
				{"@type":"AttributeMatchClause",
					"attribute":"CODE",
					"fieldType":"ATTRIBUTE",
					"desiredValue": flowlane.bis.code 
				}, {"@type":"AttributeMatchClause",
					"attribute":"SPACE",
					"fieldType":"ATTRIBUTE",
					"desiredValue": flowlane.project.bis.spaceCode
				}
			]
		}
	};
	
	var dataSetCriteria = 
	{
		subCriterias : [ flowLaneCriteria ],
		matchClauses : [ 
			{"@type":"AttributeMatchClause",
				attribute : "TYPE",
				fieldType : "ATTRIBUTE",
				desiredValue : "FASTQ_GZ" 
			}
		],
		operator : "MATCH_ALL_CLAUSES"
	};

	this.server.searchForDataSets(dataSetCriteria, action);
}