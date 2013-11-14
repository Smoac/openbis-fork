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
var FLOWLANE_SAMPLE_TYPE = "FLOWLANE";

// The view is organized in columns that correspond to a sample type. The columns are defined here.
// The order of the sample types here matches the bottom-up order of the sample types.
var COLUMNS = [
	{ type : FLOWCELL_SAMPLE_TYPE, label : "Flowcell", width : 200 },
	{ type : FLOWLANE_SAMPLE_TYPE, label : "Flowlane", width : 200 },
	{ type : "MULTIPLEX", label : "Multiplex", width : 300 },
	{ type : "LIBRARY", label : "Library", width : 240 },
	{ type : "ALIQUOT", label : "Aliquot", width : 400 },
	{ type : "SAMPLE", label : "Sample", width : 400 }
];

// The height of each line in the display
var LINE_HEIGHT = 20;

// The first N columns are shown expanded when the webapp is started. The next are initially collapsed.
// N probably should not be less than 2 and certainly should not be less than 1.
var FIRST_COLLAPSED_COLUMN = 2;

// The colors used for the different samples. The colors are used when one sample has multiple parents or children to disambiguate.
//var sampleColors = d3.scale.category10();
var sampleColors = d3.scale.ordinal().range(['#999']);

// The color used when the connection between two samples is unambiguous
var oneToOneColor = "#999"

// END CONFIGURATION PARAMTERS

/// The openbisServer we use for our data
var webappContext = new openbisWebAppContext();

// openbisServer is global -- index.html needs to refer to it.
openbisServer = new openbis('/openbis/openbis', '/datastore_server');
openbisServer.useSession(webappContext.getSessionId());

function parseJson(jsonString) { return eval('(' + jsonString + ')'); }

function flowcellIdFromFlowlaneId(flowlaneId) { return flowlaneId.split(":")[0]; }

/**
 * The node stores the information necessary for presenting the sample data in the graph.
 */
function SampleGraphNode(sample) {
	this.identifier = sample.identifier;
	this.properties = sample.properties;
	this.permId = sample.permId;
	this.nodeId = sample["@id"];
	this.sampleType = sample.sampleTypeCode;
	this.children = [];
	this.parents = [];
	this.serverSample = sample;
	this.arrayIndex = -1;
}

SampleGraphNode.prototype.toString = function() {
	return "[" + this.sampleType + ", " + this.identifier + ", " + this.permId + "]";
};

/**
 * The link stores the information necessary for drawing connections between nodes
 */
function SampleGraphEdge(source, target) {
	this.source = source;
	this.target = target;
}

/** Break an identifier into a code and space */
function codeAndSpaceFromIdentifier(identifier) {
	var identifierTokens = identifier.split("/");
	var space, code;
	if (identifierTokens.length > 2) {
		space = identifierTokens[1];
		code = identifierTokens[2];
	} else {
		space = null;
		code = identifierTokens[1];
	}
	return {space : space, code : code};
}


/**
 * The model that manages state and implements the operations for the sample graph.
 */
function SampleGraphModel() {
	this.initializeModel();
}

SampleGraphModel.prototype.initializeModel = function() {
	this.sampleIdentifier = webappContext.getEntityIdentifier();
	var codeAndSpace = codeAndSpaceFromIdentifier(this.sampleIdentifier);
	this.sampleSpace = codeAndSpace.space;
	this.sampleCode = codeAndSpace.code;
	this.samplePermId = webappContext.getEntityPermId();
	this.sampleType = webappContext.getEntityType();
	var samplesByType = {};
	COLUMNS.forEach(function(column) { samplesByType[column.type] = [] });
	this.samplesByType = samplesByType;
	this.flowcellsByIdentifier = {};
}

/**
 * Request the data necessary to display the graph.
 */
SampleGraphModel.prototype.requestGraphData = function(callback)
{
	var matchClauses = [ {"@type" : "AttributeMatchClause",
		fieldType : "ATTRIBUTE",
		attribute : "CODE",
		desiredValue : (FLOWCELL_SAMPLE_TYPE == this.sampleType) ? this.sampleCode + "*" : this.sampleCode
	}];

	if (this.sampleSpace) {
		matchClauses.push({"@type" : "AttributeMatchClause",
			fieldType : "ATTRIBUTE",
			attribute : "SPACE",
			desiredValue : this.sampleSpace
		})
	}


	var sampleCriteria = {
		matchClauses : matchClauses,
		operator : "MATCH_ALL_CLAUSES"
	};

	var lexicalParent = this;
	function coalesceResult(data) {
		lexicalParent.coalesceGraphData(data);
		lexicalParent.requestFlowcellData(callback)
	}

	openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], coalesceResult);
}

/**
 * In most cases, we need an additional server request to get the flowcell data. Make that call here.
 */
SampleGraphModel.prototype.requestFlowcellData = function(callback) {
	if (FLOWCELL_SAMPLE_TYPE == this.sampleType) {
		callback();
		return;
	}

	// Collect the Flowcell code and spaces
	var flowlaneIds = [];
	this.samplesByType[FLOWLANE_SAMPLE_TYPE].forEach(function(flowlane) {
		var flowcellId = flowcellIdFromFlowlaneId(flowlane.identifier);
		var codeAndSpace = codeAndSpaceFromIdentifier(flowcellId);
		flowlaneIds.push(codeAndSpace);
	});

	var matchClauses = [];
	flowlaneIds.forEach(function(cs) {
		matchClauses.push({"@type" : "AttributeMatchClause",
			fieldType : "ATTRIBUTE",
			attribute : "CODE",
			desiredValue : cs.code
		})
	})

	var sampleCriteria = {
		matchClauses : matchClauses,
		operator : "MATCH_ANY_CLAUSES"
	};

	var lexicalParent = this;
	function coalesceResult(data) {
		lexicalParent.coalesceGraphData(data);
		callback();
	}

	openbisServer.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES"], coalesceResult);

};


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
		// This has already been converted
		if (nodesById[sample.nodeId]) return;

		var node = new SampleGraphNode(sample);
		nodesById[node.nodeId] = node;
		sampleTypeArray = lexicalParent.samplesByType[node.sampleType]
		if (sampleTypeArray) {
			node.arrayIndex = sampleTypeArray.length;
			sampleTypeArray.push(node);
		}
		if (FLOWCELL_SAMPLE_TYPE == node.sampleType) {
			lexicalParent.flowcellsByIdentifier[node.identifier] = node;
		}

		if (sample.parents) sample.parents.forEach(convertSampleToNode);
		if (sample.children) sample.children.forEach(convertSampleToNode);
	}

	function resolveParents(sample) {
		// This is just a nodeId, it will be resolved elsewhere
		if (isPureId(sample)) return;
		if (!sample.parents) return;

		sample.parents.forEach(resolveParents);

		var node = nodeForSample(sample);
		node.parents = sample.parents.map(nodeForSample);
		node.parents.forEach(function(p) { p.children = [node]});
	}

	function resolveChildren(sample) {
		// This is just a nodeId, it will be resolved elsewhere
		if (isPureId(sample)) return;
		if (!sample.children) return;

		sample.children.forEach(resolveChildren);

		var node = nodeForSample(sample);
		node.children = sample.children.map(nodeForSample);
		node.children.forEach(function(p) { p.parents = [node]});
	}

	samples.forEach(convertSampleToNode);
	samples.forEach(resolveParents);
	samples.forEach(resolveChildren);

	// The parents of the flowlanes should become the parents of the flowcell and the
	// flowcell should become the parent of the flowlanes.
	var flowcellsByIdentifier = this.flowcellsByIdentifier;
	this.samplesByType[FLOWLANE_SAMPLE_TYPE].forEach(function(flowlane) {
		var flowcellId = flowcellIdFromFlowlaneId(flowlane.identifier);
		var flowcell = flowcellsByIdentifier[flowcellId];
		if (flowcell) {
			flowcell.parents.push(flowlane);
			flowlane.children.push(flowcell);
		}
	});
}


/**
 * The presenter that shows the model.
 */
function SampleGraphPresenter(model) {
	this.model = model;
	this.renderer = new DagreGraphRenderer();
	this.selectedNode = null;
	this.didCreateVis = false;
	this.useBottomUpMode();
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

SampleGraphPresenter.prototype.calcuateVisibleColumnOffsets = function() {
	// Calculate the offsets for the columns -- only need to do this once
	var xOffset = 0;

	// Depending on whether or not we are viewing top down or bottom up, the visible columns are those
	// before and including the selected sample type, or those after and including the sample type, respectively.
	var querySampleType = model.sampleType;
	var seenQuerySampleType = false;
	var bottomUpMode = this.bottomUpMode;
	var visibleColumns = [];
	COLUMNS.forEach(function(col, i) {
		var atQuerySampleType = col.type == querySampleType;
		if (atQuerySampleType) {
			visibleColumns.push(col);
		} else {
			var includeCol = seenQuerySampleType && bottomUpMode;
			includeCol = includeCol || !seenQuerySampleType && !bottomUpMode;
			if (includeCol) visibleColumns.push(col)
		}
		seenQuerySampleType = seenQuerySampleType || atQuerySampleType;
	})

	if (!bottomUpMode) visibleColumns = visibleColumns.reverse();
	this.visibleColumns = visibleColumns;
	this.visibleColumns.forEach(function(column) { column.xOffset = xOffset; xOffset += column.width });
};

/**
 * Put the presenter in the bottom-up display mode (compared to top-down)
 */
SampleGraphPresenter.prototype.useBottomUpMode = function() {
	this.bottomUpMode = true;
	this.calcuateVisibleColumnOffsets();
};

/**
 * Put the presenter in the top-down display mode (compared to bottom-up)
 */
SampleGraphPresenter.prototype.useTopDownMode = function() {
	this.bottomUpMode = false;
	this.calcuateVisibleColumnOffsets();
};

/**
 * Return a function that gives the outgoing edges for a sample.
 *
 * The outgoing edges depends on whether the view is top-down or bottom-up
 */
SampleGraphPresenter.prototype.outEdgesFunction = function() {
	var bottomUpMode = this.bottomUpMode;
	return function(samp) { return bottomUpMode ? samp.parents : samp.children }
}

/**
 * Initialize the sample nodes
 */
SampleGraphPresenter.prototype.initializeGraphSamples = function()
{
	var colors = sampleColors;
	var nodes = this.visibleColumns.map(function(c) { return model.samplesByType[c.type] });
	var outEdgesGetter = this.outEdgesFunction();
	// Compute the x/y coordinates for each sample
	for (var col = 0; col < nodes.length; ++col) {
		var colData = nodes[col];
		var xOffset = this.visibleColumns[col].xOffset;
		var width = this.visibleColumns[col].width;
		for (row = 0; row < colData.length; ++row) {
			var sampleData = colData[row];
			sampleData.col = col;
			sampleData.row = row;
			sampleData.colOffset = xOffset;
			var outEdges = outEdgesGetter(sampleData);
			var oneEdgeOrLess = outEdges.length < 2;
			var connectedNodesWithMultipleEdges = outEdges.filter(function(c) { return outEdgesGetter(c).length > 1 });
			var oneToOne = oneEdgeOrLess && connectedNodesWithMultipleEdges.length == 0;
			sampleData.color = (!oneToOne) ? colors(row) : oneToOneColor;
			sampleData.userEdgesVisible = null;
			sampleData.edgesVisible = col + 1 < FIRST_COLLAPSED_COLUMN;
		}
	}
	this.allNodes = nodes;
}

SampleGraphPresenter.prototype.updateVisibility = function() {
	// Turn off visibility on all nodes except the root node
	this.allNodes.forEach(function(samps) {
		samps.forEach(function(s) { s.visible = s.identifier == model.sampleIdentifier;	})
	});
	var outEdgesGetter = this.outEdgesFunction();
	// Figure out if the nodes should be visible
	this.allNodes.forEach(function(samps) {
		samps.forEach(function(sample) {
			if (null != sample.userEdgesVisible) sample.edgesVisible = sample.userEdgesVisible;
			var showChildren = sample.visible && sample.edgesVisible;
			if (!showChildren) return;
			outEdgesGetter(sample).forEach(function(c) {
				c.visible = true;
				// Nodes with only one outgoing edge should show their connected node as well, unless the user requests otherwise
				if (outEdgesGetter(c).length == 1) {
					c.edgesVisible = (null == c.userEdgesVisible) ? true : c.userEdgesVisible;
				}
			});
		})
	});
}

SampleGraphPresenter.prototype.updateEdges = function() {
	var edges = [];
	var outEdgesGetter = this.outEdgesFunction();
	this.allNodes.forEach(function(samps) {
		samps.forEach(function(d) { 
			if (!d.visible) return;
			outEdgesGetter(d).forEach(function(c) { if (c.visible) edges.push(new SampleGraphEdge(d, c))});
		})
	});

	this.edges = edges;
}

SampleGraphPresenter.prototype.updateNodes = function() {
	var nodes = this.allNodes.map(function(d) { return d.filter(function(n) { return n.visible })});
	nodes.forEach(function(samps) { samps.forEach(function(s, i) { s.visibleIndex = i })});
	this.nodes = nodes;
}

SampleGraphPresenter.prototype.updateState = function()
{
	// These need to be done in this order
	this.updateVisibility();
	this.updateNodes();
	this.updateEdges();
}

/**
 * Display the sample nodes.
 */
SampleGraphPresenter.prototype.draw = function()
{
	this.renderer.draw();
}


SampleGraphPresenter.prototype.toggleExpand = function(svgNode, d) {
	// toggle visiblity
	d.userEdgesVisible = (null == d.userEdgesVisible) ? !d.edgesVisible :!d.userEdgesVisible;
	this.selectedNode = svgNode.parentNode;
	this.draw();
}

SampleGraphPresenter.prototype.openSample = function(svgNode, d) {
	var url = "/openbis/index.html?viewMode=SIMPLE#entity=SAMPLE&permId=" + d.permId;
	window.open(url, '_blank');
}

function displayActiveMode(active, inactive) {
	active.parent().addClass("active");
	inactive.parent().removeClass("active");
}

function clickedBottomUp() {
	if (presenter.bottomUpMode) return;

	displayActiveMode($('#bottom-up'), $('#top-down'));
	presenter.useBottomUpMode();
	presenter.initializeGraphSamples();
	presenter.draw();
}

function clickedTopDown() {
	if (!presenter.bottomUpMode) return;

	displayActiveMode($('#top-down'), $('#bottom-up'));
	presenter.useTopDownMode();
	presenter.initializeGraphSamples();
	presenter.draw();
}


function textBBoxForGraphNode(node) {
	var bbox = presenter.renderer.columns.selectAll("text.sample")[node.col][node.visibleIndex].getBBox();
	// Correct for the column
	bbox.x += node.colOffset;
	return bbox;
}

function textBBoxForDomNode(node) {
	var bbox = node.parentNode.childNodes[0].getBBox();
	return bbox;
}

function translate(x, y) {
	return "translate(" + x + "," + y + ")";
}

var yLinkOffset = LINE_HEIGHT * 0.33;


/**
 * A class that renders the graph
 */
function SimpleGraphRenderer() {
	// Function used to draw paths between elements
	function source(d) {
		// Find the associated text node in the DOM and use that as a basis for creating the edges
		var bbox = textBBoxForGraphNode(d.source);
		return { x : bbox.x + bbox.width + 7, y  : bbox.y + yLinkOffset };
	}
	function target(d) {
		var bbox = textBBoxForGraphNode(d.target);
		return { x : bbox.x, y  : bbox.y + yLinkOffset }
	}

	this.useLineLinkPath(source, target);
}

/**
 * Draw edges using the diagonal function
 */
SimpleGraphRenderer.prototype.useDiagonalLinkPath = function(source, target) {
	var diagonal = d3.svg.diagonal();
	diagonal.source(source);
	diagonal.target(target);
	this.path = diagonal;
}

/**
 * Draw edges using the line function
 */
SimpleGraphRenderer.prototype.useLineLinkPath = function(source, target) {
	var line = d3.svg.line();
	this.path = function(d) {
		var src = source(d);
		var dst = target(d);
		return line([[src.x, src.y], [dst.x, dst.y]]); }
}

/**
 * Display the sample nodes.
 */
SimpleGraphRenderer.prototype.draw = function()
{
	presenter.updateState();
	var nodes = presenter.nodes;

	// Display the graph in an SVG element
	this.viz = presenter.root.selectAll("svg").data([nodes]);
	// Code under enter is run if there is no HTML element for a data element	
	this.viz.enter().append("svg:svg").attr("class", "viz");

	// Columns
	this.columns = this.viz.selectAll("g").data(function(d) { return d });
	this.columns.enter().append("svg:g").attr("class", "column");
	this.columns.exit().remove();
	this.columns.attr("transform", function(d, i) { return "translate(" + presenter.visibleColumns[i].xOffset + ", 0)"});
	this.drawHeaders();
	this.drawNodes();
	this.drawEdges();

	var vizHeight = (d3.max(nodes, function(d) { return d.length}) + 1) * LINE_HEIGHT + 5;
	var vizWidth = d3.sum(presenter.visibleColumns, function(column) { return column.width });
	this.viz.attr("width", vizWidth);
	this.viz.attr("height", vizHeight);
}

/**
 * Draw the headers
 */
SimpleGraphRenderer.prototype.drawHeaders = function()
{
	var header = this.columns.selectAll("text.header").data(function(d, i) { return [presenter.visibleColumns[i]] });
	header.enter().append("svg:text")
		.attr("class", "header")
		.attr("x", "0")
		.attr("y", LINE_HEIGHT)
		.attr("text-anchor", "begin")
		.style("font-weight", "bold");
	header
		.text(function(d) { return d.label });
}

/**
 * Draw the nodes
 */
SimpleGraphRenderer.prototype.drawNodes = function()
{
	var outEdgesGetter = presenter.outEdgesFunction();
	var sample = this.columns.selectAll("text.sample").data(function(d) { return d });
	sample.enter().append("svg:text")
		.attr("class", "sample")
		.attr("x", "0")
		.attr("y", LINE_HEIGHT)
		.attr("text-anchor", "begin")
		.style("cursor", "pointer")
		.on("click", function(d) { presenter.openSample(this, d) })
		.transition()
			.style("opacity", 1);
	sample.exit()
		.transition()
			.style("opacity", 0).remove();
	sample
		.attr("x", "0")
		.attr("y", function(d, i) { return LINE_HEIGHT * (i+2)})

		.text(function(d) { return d.identifier });

	var ring = this.columns.selectAll("circle.ring").data(function(d) { return d });
	ring.enter().append("svg:circle")
		.attr("class", "ring")
		.attr("pointer-events", "all")
		.attr("r", 0)
		.style("cursor", "pointer")
		.style("stroke-width", "2px")
	ring.exit()
		.transition()
			.style("opacity", 0).remove();
	ring
		.attr("cx", function(d) { return textBBoxForGraphNode(d).width + 7 })
		.attr("cy", function(d, i) { return LINE_HEIGHT * (i+2) - yLinkOffset})
		.style("fill", function(d) { return d.edgesVisible ? "none" : d.color})
		.style("stroke", function(d) { return d.color})
		.on("click", function(d) { presenter.toggleExpand(this, d) });
	ring
		.transition()
			.style("opacity", function(d) { return outEdgesGetter(d).length > 0 ? 1 : 0 })
			.attr("r", 5);
}

/**
 * Draw the edges
 */
SimpleGraphRenderer.prototype.drawEdges = function()
{
	var link = this.viz.selectAll("path.link").data(presenter.edges);
	link.enter().append("svg:path")
		.attr("class", "link")
		.attr("pointer-events", "none")
		.style("fill", "none")
		.style("stroke-width", "1.5px")
		.transition()
			.style("opacity", 1);
	link.exit()
		.transition()
			.style("opacity", 0).remove();
	link
		.style("stroke", function(d) { return d.source.color})
		.attr("d", this.path);
}

/**
 * A class that renders the graph using dagre.
 */
function DagreGraphRenderer() {
	// Function used to draw paths between elements
	function source(d) {
		var dagre = d.source.dagre;
		return { x : dagre.x + (dagre.width / 2), y  : dagre.y + dagre.height + 5};
	}
	function target(d) {
		var dagre = d.target.dagre;
		return { x : dagre.x + (dagre.width / 2), y  : dagre.y };
	}

	this.useLineLinkPath(source, target);
}

/**
 * Draw edges using the diagonal function
 */
DagreGraphRenderer.prototype.useDiagonalLinkPath = function(source, target) {
	var diagonal = d3.svg.diagonal();
	diagonal.source(source);
	diagonal.target(target);
	this.path = diagonal;
}

/**
 * Draw edges using the line function
 */
DagreGraphRenderer.prototype.useLineLinkPath = function(source, target) {
	var line = d3.svg.line();
	this.path = function(d) {
		var src = source(d);
		var dst = target(d);
		return line([[src.x, src.y], [dst.x, dst.y]]); }
}

var RANK_SEPARATION = 50;

DagreGraphRenderer.prototype.normalizeNodeYPos = function() 
{
	var nodes = presenter.nodes;
	function dagrey(node) { return node.dagre.y }
	// Look at all the nodes and the edges and fix the height so that nodes of the same type form a row
	minmaxy = nodes.map(function(d) { return [ d3.min(d, dagrey), d3.max(d, dagrey) ]});
	miny = [minmaxy[0][0]];
	// Look the levels as pairs and ensure that the min of level i+1 is > min of level i
	minmaxy.reduce(function(a,b) {
		var bcopy = b.slice(0);
		if (bcopy[0] < a[1] + RANK_SEPARATION) {
			bcopy[0] = a[1] + RANK_SEPARATION;
			if (bcopy[1] < bcopy[0]) bcopy[1] = bcopy[0] + RANK_SEPARATION;
		}
		miny.push(bcopy[0]);
		return bcopy;
	});
	nodes.forEach(function(group, i) { group.forEach(function(node) {
		// look at the nodes in this group. Leave the ones that have parents/children within the group alone.
		// Change the y position for the other ones
		if (node.dagre.y < miny[i]) node.dagre.y = miny[i];
	})});
}

/**
 * Display the sample nodes.
 */
DagreGraphRenderer.prototype.draw = function()
{
	presenter.updateState();
	var nodes = presenter.nodes;

	var viz;
	// Display the graph in an SVG element
	viz = presenter.root.selectAll("svg").data([nodes]);
	this.viz = viz;
	// Code under enter is run if there is no HTML element for a data element
	viz.enter().append("svg:svg")
		.attr("class", "viz")
		// Create an arrowhead to put to the lines
		.append("svg:defs").append("svg:marker")
			.attr("id", "arrowhead")
			.attr("viewBox", "0 0 10 10")
			.attr("refX", "8")
			.attr("refY", "5")
			.attr("markerUnits", "strokeWidth")
			.attr("markerHeight", "5")
			.attr("orient", "auto")
			.style("fill", "#333")
			.append("svg:path").attr("d", "M 0 0 L 10 5 L 0 10 z");

	// Edges -- this has to come before the nodes so that the edges are under the nodes
	this.edges = this.viz.selectAll("g.edge").data([0]);
	this.edges.enter().append("svg:g").attr("class", "edge");
	this.edges.exit().remove();

	// Columns
	this.columns = this.viz.selectAll("g.column").data(function(d) { return d });
	this.columns.enter().append("svg:g").attr("class", "column");
	this.columns.exit().remove();

	// Draw the nodes so we get the bounding boxes for the nodes
	this.initialDrawNodes();

	var dagreNodes = []
	presenter.nodes.forEach(function(nodeGroup) { nodeGroup.forEach(function(d) { dagreNodes = dagreNodes.concat(d) }) });

	dagre.layout()
		.nodeSep(RANK_SEPARATION)
		.edgeSep(10)
		.rankSep(RANK_SEPARATION)
		.nodes(dagreNodes)
		.edges(presenter.edges)
	    .run();
	this.normalizeNodeYPos();
	this.redrawNodes();
	this.drawHeaders();
	this.drawEdges();

	var vizWidth = d3.max(dagreNodes, function(d) {	return d.dagre.x + d.dagre.width; })
	var vizHeight = d3.max(dagreNodes, function(d) { return d.dagre.y + d.dagre.height; })

	// Resize the visualization
	viz.attr("width", vizWidth + 20); // add space for the ring at the end
	viz.attr("height", vizHeight + LINE_HEIGHT); // add a space to make it look less cramped

	// If the user clicked on a node, scroll to make it visible
	if (presenter.selectedNode) {
		// Figure out which element we need to scroll
		var scrollWindow = (d3.select("#root").style("overflow") == "visible")
		if (scrollWindow) {
			var scrolledElt = $(window);
			var left = $(presenter.selectedNode).position().left - 50;
			scrolledElt.scrollLeft(left);
		} else {
			var scrolledElt = $("#root");
			var left = $(presenter.selectedNode).position().left + scrolledElt.scrollLeft() - 50;
			scrolledElt.scrollLeft(left);
		}

		// WebKit only
		// presenter.selectedNode.scrollIntoViewIfNeeded(true);

	}
}

/**
 * Draw the headers
 */
DagreGraphRenderer.prototype.drawHeaders = function()
{
	var sampleNodeGroup = this.columns.selectAll("g.sample").data(function(d) { return d });
	var header = this.viz.selectAll("text.header").data(function(d, i) { return presenter.visibleColumns });
	header.enter().append("svg:text")
		.attr("class", "header")
		.attr("x", "0")
		.attr("text-anchor", "begin")
		.style("font-weight", "bold");
	header.exit().remove();
	header
		.attr("y", function(d, i) {
			var nodesAtLevel = sampleNodeGroup[i];
			if (nodesAtLevel.length < 1) return 0;
			if (!nodesAtLevel[0]) return 0;
			var dagre = nodesAtLevel[0].__data__.dagre;
			return dagre.y + dagre.height - 1;
		})
		.attr("opacity", function(d, i) {
			var nodesAtLevel = sampleNodeGroup[i];
			if (nodesAtLevel.length < 1) return 0;
			if (!nodesAtLevel[0]) return 0;
			return 1;
		})
		.text(function(d) { return d.label });
}

/**
 * Draw the nodes
 */
DagreGraphRenderer.prototype.initialDrawNodes = function()
{
	var outEdgesGetter = presenter.outEdgesFunction();
	var sampleNodeGroup = this.columns.selectAll("g.sample").data(function(d) { return d });
	sampleNodeGroup.enter().append("svg:g")
		.attr("class", "sample")
	sampleNodeGroup
		sampleNodeGroup.attr("transform", function(d, i) { return translate(0, LINE_HEIGHT * (i+1)) });
	sampleNodeGroup.exit()
		.transition()
			.style("opacity", 0).remove();

	var sample = sampleNodeGroup.selectAll("text.sample").data(function(d) { return [d] });
	sample.enter().append("svg:text")
		.attr("class", "sample")
		.attr("x", "0")
		.attr("y", LINE_HEIGHT)
		.attr("text-anchor", "begin")
		.style("cursor", "pointer")
		.on("click", function(d) { presenter.openSample(this, d) })
		.transition()
			.style("opacity", 1);
	sample
		.text(function(d) { return d.identifier });

	var ring = sampleNodeGroup.selectAll("circle.ring").data(function(d) { return [d] });
	ring.enter().append("svg:circle")
		.attr("class", "ring")
		.attr("pointer-events", "all")
		.attr("r", 0)
		.style("cursor", "pointer")
		.style("stroke-width", "2px")
	ring
		.attr("cx", function(d, i) { return textBBoxForDomNode(this).width + 7 })
		.attr("cy", function(d, i) { return textBBoxForDomNode(this).height - 1 })
		.style("fill", function(d) { return d.edgesVisible ? "none" : d.color})
		.style("stroke", function(d) { return d.color})
		.on("click", function(d) { presenter.toggleExpand(this, d) });
	ring
		.transition()
			.style("opacity", function(d) { return outEdgesGetter(d).length > 0 ? 1 : 0 })
			.attr("r", 5);

	// Update the bounding boxes
	sampleNodeGroup.each(function(d) {
		var bbox = this.getBBox();
		d.width = bbox.width;
		d.height = bbox.height;
	});
}

/**
 * Move the nodes in their new positions.
 */
DagreGraphRenderer.prototype.redrawNodes = function()
{
	var sampleNodeGroup = this.columns.selectAll("g.sample").data(function(d) { return d });
	sampleNodeGroup
		sampleNodeGroup.attr("transform", function(d, i) { return translate(d.dagre.x, d.dagre.y) });
}

/**
 * Draw the edges
 */
DagreGraphRenderer.prototype.drawEdges = function()
{
	var link = this.edges.selectAll("path.link").data(presenter.edges);
	link.enter().append("svg:path")
		.attr("class", "link")
		.attr("pointer-events", "none")
		.style("fill", "none")
		.style("stroke-width", "1.5px")
		.attr("marker-end", "url(#arrowhead)")
		.transition()
			.style("opacity", 1);
	link.exit()
		.transition()
			.style("opacity", 0).remove();
	link
		.style("stroke", function(d) { return d.source.color})
		.attr("d", this.path);
}



/// The model that manages state and implements the operations
var model;
model = new SampleGraphModel();

// The presenter tranlsates the model into visual elements
var presenter;


function enterApp(data)
{
	$('#bottom-up').click(clickedBottomUp);
	$('#top-down').click(clickedTopDown);
	presenter = new SampleGraphPresenter(model);
	presenter.useBottomUpMode()
    model.requestGraphData(function() { presenter.initializeGraphSamples(); presenter.draw() });
}