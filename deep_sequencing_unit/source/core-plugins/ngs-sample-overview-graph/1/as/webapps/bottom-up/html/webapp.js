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
	{ type : "FLOWLANE", label : "Flowlane", width : 200 },
	{ type : "MULTIPLEX", label : "Multiplex", width : 300 },
	{ type : "LIBRARY", label : "Library", width : 240 },
	{ type : "ALIQUOT", label : "Aliquot", width : 400 },
	{ type : "SAMPLE", label : "Sample", width : 300 }
];

// The height of each line in the display
var LINE_HEIGHT = 20;

// The first N columns are shown expanded when the webapp is started. The next are initially collapsed.
// N probably should not be less than 2 and certainly should not be less than 1.
var FIRST_COLLAPSED_COLUMN = 2;

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
	this.parents = [];
	this.serverSample = sample;
	this.arrayIndex = -1;
}

/**
 * The link stores the information necessary for drawing connections between nodes
 */
function SampleGraphLink(sourceNode, targetNode) {
	this.sourceNode = sourceNode;
	this.targetNode = targetNode;
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
		node.children.forEach(function(c) { c.parents.push(node)} );
	}

	samples.forEach(convertSampleToNode);
	samples.forEach(resolveParents);
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

	// Calculate the offsets for the columns -- only need to do this once
	var xOffset = 0;
	COLUMNS.forEach(function(column) { column.xOffset = xOffset; xOffset += column.width });
	this.vizWidth = xOffset;

	// Function used to draw paths between elements
	var yLinkOffset = LINE_HEIGHT * 0.33;

	var lexicalParent = this;
	function bboxForNode(node) { 
		var bbox = lexicalParent.columns.selectAll("text.sample")[node.col][node.row].getBBox();
		// Correct for the column
		bbox.x += node.colOffset;
		return bbox;
	}
	function source(d) {
		// Find the associated text node in the DOM and use that as a basis for creating the links
		var bbox = bboxForNode(d.sourceNode);
		return { x : bbox.x + bbox.width + 5, y  : bbox.y + yLinkOffset };
	}
	function target(d) {
		var bbox = bboxForNode(d.targetNode);
		return { x : bbox.x, y  : bbox.y + yLinkOffset }
	}

	this.useLineLinkPath(source, target);
	this.didCreateVis = true;
}

/**
 * Draw links using the diagonal function
 */
SampleGraphPresenter.prototype.useDiagonalLinkPath = function(source, target) {
	var diagonal = d3.svg.diagonal();
	diagonal.source(source);
	diagonal.target(target);	
	this.path = diagonal;
}

/**
 * Draw links using the line function
 */
SampleGraphPresenter.prototype.useLineLinkPath = function(source, target) {
	var line = d3.svg.line();
	this.path = function(d) {
		var src = source(d);
		var dst = target(d);
		return line([[src.x, src.y], [dst.x, dst.y]]); }
}

/**
 * Initialize the sample nodes
 */
SampleGraphPresenter.prototype.initializeGraphSamples = function()
{
	var colors = d3.scale.category10();
	var nodes = COLUMNS.map(function(c) { return model.samplesByType[c.type] });
	// Compute the x/y coordinates for each sample
	for (var col = 0; col < nodes.length; ++col) {
		var colData = nodes[col];
		var xOffset = COLUMNS[col].xOffset;
		var width = COLUMNS[col].width;
		for (row = 0; row < colData.length; ++row) {
			var sampleData = colData[row];
			sampleData.col = col;
			sampleData.row = row;
			sampleData.colOffset = xOffset;
			var oneChildOrLess = sampleData.children.length < 2;
			var childrenWithMultipleParents = sampleData.children.filter(function(c) { return c.parents.length > 1 });
			var oneToOne = oneChildOrLess && childrenWithMultipleParents.length == 0;
			sampleData.color = (!oneToOne) ? colors(row) : "#ccc";
			sampleData.visible = col < FIRST_COLLAPSED_COLUMN;
		}
	}
	this.allNodes = nodes;

	this.vizHeight = d3.max(nodes, function(d) { return d.length}) * LINE_HEIGHT
}

SampleGraphPresenter.prototype.computeLinks = function() {
	var links = [];
	this.allNodes.forEach(function(samples) {
		samples.forEach(function(d) { 
			if (!d.visible) return;
			d.children.forEach(function(c) { if (c.visible) links.push(new SampleGraphLink(d, c))});
		})
	});

	this.links = links;
}

/**
 * Display the sample nodes.
 */
SampleGraphPresenter.prototype.showGraphSamples = function()
{
	var nodes = this.allNodes.map(function(d) { return d.filter(function(n) { return n.visible })});
	this.computeLinks();
	var vizWidth = this.vizWidth;
	var vizHeight = this.vizHeight;

	// Display the graph in an SVG element
	this.viz = this.root.selectAll("svg").data([nodes]);
	// Code under enter is run if there is no HTML element for a data element	
	this.viz.enter().append("svg:svg").attr("class", "viz");
	this.viz.attr("width", vizWidth);
	this.viz.attr("height", vizHeight);
	// Columns
	this.columns = this.viz.selectAll("g").data(function(d) { return d });
	this.columns.enter().append("svg:g").attr("class", "column");
	this.columns.attr("transform", function(d, i) { return "translate(" + COLUMNS[i].xOffset + ", 0)"});
	this.showHeaders();
	this.showNodes();
	this.showLinks();
}

/**
 * Draw the headers
 */
SampleGraphPresenter.prototype.showHeaders = function()
{
	var header = this.columns.selectAll("text.header").data(function(d, i) { return [COLUMNS[i]] });
	header.enter().append("svg:text").attr("class", "header");
	header
		.attr("x", "0")
		.attr("y", LINE_HEIGHT)
		.attr("text-anchor", "begin")
		.style("font-weight", "bold")
		.text(function(d) { return d.label });
}

/**
 * Draw the nodes
 */
SampleGraphPresenter.prototype.showNodes = function()
{
	var lexicalParent = this;
	var sample = this.columns.selectAll("text.sample").data(function(d) { return d.filter(function(s) { return s.visible; }) });
	sample.enter().append("svg:text").attr("class", "sample");
	sample.exit().remove();
	sample
		.attr("x", "0")
		.attr("y", function(d, i) { return LINE_HEIGHT * (i+2)})
		.attr("text-anchor", "begin")
		.style("cursor", "pointer")
		.on("click", function(d) { lexicalParent.clickedNode(this, d) })
		.text(function(d) { return d.identifier });
}

/**
 * Draw the links
 */
SampleGraphPresenter.prototype.showLinks = function()
{
	var link = this.viz.selectAll("path.link").data(this.links);
	link.enter().append("svg:path").attr("class", "link");
	link.exit().remove();
	link
		.style("fill", "none")
		.style("stroke", function(d) { return d.sourceNode.color})
		.style("stroke-width", "1.5px")
		.attr("d", this.path);
}

SampleGraphPresenter.prototype.clickedNode = function(svgNode, d) {
	// toggle visiblity
	d.children.forEach(function(c) { c.visible = !c.visible });
	this.showGraphSamples();
}

/// The model that manages state and implements the operations
var model;
model = new SampleGraphModel();

// The presenter tranlsates the model into visual elements
var presenter;


function enterApp(data)
{
	presenter = new SampleGraphPresenter(model);
    model.requestGraphData(function() { presenter.initializeGraphSamples(); presenter.showGraphSamples() });
}