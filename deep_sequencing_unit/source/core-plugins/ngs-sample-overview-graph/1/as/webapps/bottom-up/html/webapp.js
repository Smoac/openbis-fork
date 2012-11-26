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
 * The model that manages state and implements the operations for the bottom-up sample graph.
 */
function SampleGraphModel() {
	this.initializeModel();
}

SampleGraphModel.prototype.initializeModel = function() {
	this.sampleIdentifier = webappContext.getEntityIdentifier();
	this.samplePermId = webappContext.getEntityPermId();
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

	this.didCreateVis = true;
}

/**
 * Display the data returned by the server
 */
SampleGraphPresenter.prototype.showGraph = function(data)
{
	if (data.error) {
		console.log(data.error);
		this.root.append("p").text("Could not retrieve data.");
		return;
	}
	
	// This will show the object in the log -- helpful for debugging
	// console.log(data.result);
	var tableData = data.result;
	
	// Display the rows in a table
	var table = this.root.selectAll("table").data([tableData]);
	// Code under enter is run if there is no HTML element for a data element	
	table.enter().append("table").attr("class", "table");
	showTableHeader(table);
	showTableData(table);
}

SampleGraphPresenter.prototype.selectEntity = function(d) {

}

/// The model that manages state and implements the operations
var model;
model = new SampleGraphModel();

// The presenter tranlsates the model into visual elements
var presenter;


/**
 * Construct the table header.
 */
function showTableHeader(table)
{
	var header = table.selectAll("thead").data(function(d) { return [d.columns] });
	header.enter().append("thead");
	var headerRows = header.selectAll("tr").data(function(d) { return [d] });
	headerRows.enter().append("tr");
	var headerData = headerRows.selectAll("th").data(function(d) { return d; });
	headerData.enter().append("th");
	headerData.text(function (d) { return d.title})
}

/**
 * Construct the table data.
 */
function showTableData(table)
{
	var tableBody = table.selectAll("tbody").data(function(d) { return [d.rows] });
	tableBody.enter().append("tbody");
	var dataRows = tableBody.selectAll("tr").data(function(d) { return d });
	dataRows.enter().append("tr").on("click", function (d) { presenter.selectEntity(d); });
	dataRows.exit().remove();

	var dataData = dataRows.selectAll("td").data(function(d) { return d });
	dataData.enter().append("td");
	dataData.text(function (d) { return d.value});
}


function displayRoot(data)
{
	presenter.showGraph(data)
}

/**
 * Request samples matching some criteria from the server and show them in the Page.
 */
function listRootLevelEntities()
{
	var parameters = {requestKey : 'ROOT'};

	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displayRoot);
}

function enterApp(data)
{
	presenter = new SampleGraphPresenter(model);
    listRootLevelEntities();
}