/**
 * Sample-Relationship Bottom Up Webapp
 *
 * This webapp draws a graph emanating from a flowcell and terminating at the biological sample.
 *
 * Because the structure is rigid, it draws each sample type in its own column.
 */

//
// BEGIN CONFIGURATION PARAMTERS
// 
// The following parameters must be configured for the webapp

var FLOWCELL_SAMPLE_TYPE = "FLOWCELL";

// The view is organized in columns that correspond to a sample type. The columns are defined here.
var COLUMNS = [
	{ type : "FLOWLANE", label : "Flowlane", width : 120 },
	{ type : "MULTIPLEX", label : "Multiplex", width : 120 }
];

// END CONFIGURATION PARAMTERS

/// The openbisServer we use for our data
var webappContext = new openbisWebAppContext();

// openbisServer is global -- index.html needs to refer to it.
openbisServer = new openbis('/openbis/openbis', '/datastore_server');
openbisServer.useSession(webappContext.getSessionId());

function getAppHeight(){
	return Math.max($(window).height() - 50, getVisibleLeafsCountForNode(root) * 30);
}

function getAppWidth(){
	return $(window).width();
}


function parseJson(jsonString) { return eval('(' + jsonString + ')'); }

/**
 * The node stores the information necessary for presenting the sample data in the graph.
 */
function SampleGraphNode(sample) {
	this.identifier = sample.identifier;
	this.properties = sample.properties;
	this.nodeId = sample["@id"];
	this.sampleType = sample.sampleTypeCode;
	this.children = [];
	this.serverSample = sample;
	this.arrayIndex = -1
}


/**
 * The model that manages state and implements the operations for the bottom-up sample graph.
 */
function SampleGraphModel() {
	this.initializeModel();
}

SampleGraphModel.prototype.initializeModel = function() {
	this.sampleIdentifier = webappContext.getEntityIdentifier();
	var identifierTokens = this.sampleIdentifier.split("/");
	this.sampleSpace = identifierTokens[1];	
	this.sampleCode = identifierTokens[2];
	this.samplePermId = webappContext.getEntityPermId();
	var samplesByType = {};
	COLUMNS.forEach(function(column) { samplesByType[column.type] = [] });
	this.samplesByType = samplesByType;
}

/**
 * Request the data necessary to display the graph.
 */
SampleGraphModel.prototype.requestGraphData = function(callback)
{
	var containerCriteria = {
		matchClauses : 
			[ {"@type" : "AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "TYPE",
				desiredValue : FLOWCELL_SAMPLE_TYPE 
			}, {"@type" : "AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "CODE",
				desiredValue : this.sampleCode 
			}, {"@type" : "AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "SPACE",
				desiredValue : this.sampleSpace 
			} ],
		operator : "MATCH_ALL_CLAUSES"		
	};

	var sampleCriteria = {
		subCriterias : 
		[ {"@type" : "SearchSubCriteria",
		 	criteria : containerCriteria,
		 	targetEntityKind : "SAMPLE_CONTAINER"
		} ]
	};

	var lexicalParent = this;
	function coalesceResult(data) {
		lexicalParent.coalesceGraphData(data);
		callback();
	}

	openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], coalesceResult);
}

/**
 * Request the data necessary to display the graph.
 */
SampleGraphModel.prototype.coalesceGraphData = function(data, callback) {
	var samples = data.result;
	var nodesById = {};

	function isPureId(sample) { return null == sample["@id"]; }

	function nodeForSample(sample) { return isPureId(sample) ? nodesById[sample] : nodesById[sample["@id"]]; }

	var lexicalParent = this;
	function convertSampleToNode(sample) {
		// This is just a nodeId, it will be converted elsewhere
		if (isPureId(sample)) return;

		sample.parents.forEach(convertSampleToNode);

		var node = new SampleGraphNode(sample);
		nodesById[node.nodeId] = node;
		sampleTypeArray = lexicalParent.samplesByType[node.sampleType]
		if (sampleTypeArray) {
			node.arrayIndex = sampleTypeArray.length;
			sampleTypeArray.push(node);
		}
	}

	function resolveParents(sample) {
		// This is just a nodeId, it will be resolved elsewhere
		if (isPureId(sample)) return;

		sample.parents.forEach(resolveParents);

		// Sample parents become node children
		var node = nodeForSample(sample);
		node.children = sample.parents.map(nodeForSample);
	}

	samples.forEach(convertSampleToNode);
	samples.forEach(resolveParents);

	this.graphData = samples.map(nodeForSample);
}


/**
 * The presenter that shows the model.
 */
function SampleGraphPresenter(model) {
	this.model = model;
	this.didCreateVis = false;
	this.initializePresenter();
}

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
SampleGraphPresenter.prototype.initializePresenter = function()
{ 
	if (this.didCreateVis) return;
	
	// Create a div to house the tree visualization and the inspectors
	this.root = d3.select("#root");
	this.rootLabel = d3.select("#root-label");
	this.rootLabel.text(this.model.sampleIdentifier);

	this.didCreateVis = true;
}

/**
 * Display the sample nodes.
 */
SampleGraphPresenter.prototype.showGraphSamples = function()
{
	// This will show the object in the log -- helpful for debugging
	// console.log(nodes);

	// Display the rows in a table
	var table = this.root.selectAll("table").data([model.graphData]);
	// Code under enter is run if there is no HTML element for a data element	
	table.enter().append("table").attr("class", "table");
	this.showEntityTableHeader(table);
	this.showEntityTableData(table);	
}

/**
 * Construct the table header.
 */
SampleGraphPresenter.prototype.showEntityTableHeader = function(table)
{
	var header = table.selectAll("thead").data(function(d) { return [["Flowlane", "Multiplex", "Library Count"]] });
	header.enter().append("thead");
	var headerRows = header.selectAll("tr").data(function(d) { return [d] });
	headerRows.enter().append("tr");
	var headerData = headerRows.selectAll("th").data(function(d) { return d; });
	headerData.enter().append("th");
	headerData.text(function (d) { return d})
}

/**
 * Construct the table data.
 */
SampleGraphPresenter.prototype.showEntityTableData = function(table)
{
	var tableBody = table.selectAll("tbody").data(function(d) { return d });
	tableBody.enter().append("tbody");
	var dataRows = tableBody.selectAll("tr").data(function(d) { return [d] });
	dataRows.enter().append("tr").on("click", function (d) { presenter.selectEntity(d); });
	dataRows.exit().remove();

	var dataData = dataRows.selectAll("td").data(function(d) { return [d.identifier, d.children[0].identifier, "" + d.children[0].children.length] });
	dataData.enter().append("td");
	dataData.text(function (d) { return d });
}

SampleGraphPresenter.prototype.selectEntity = function(d) {

}

/// The model that manages state and implements the operations
var model;
model = new SampleGraphModel();

// The presenter tranlsates the model into visual elements
var presenter;


function enterApp(data)
{
	presenter = new SampleGraphPresenter(model);
    model.requestGraphData(function() { presenter.showGraphSamples() });
}