/// The openbisServer we use for our data
openbisServer = new openbis('/openbis/openbis', '/datastore_server');

function getAppHeight(){
	return Math.max($(window).height() - 50, getVisibleLeafsCountForNode(root) * 30);
}

function getAppWidth(){
	return $(window).width();
}


var didCreateVis = false;


/**
 * The model that manages state and implements the operations.
 */
function CrudModel() {
	this.clearModel();
}

CrudModel.prototype.clearModel = function() {
	this.operation = "INSERT";
	this.infotext = "";
	this.identifier = "";
}

CrudModel.prototype.callOperation = function() {
	var parameters = {};
	parameters["info"] = this.infotext;
	parameters["id"] = this.identifier;
	parameters["operation"] = this.operation;
}

/// The model that manages state and implements the operations
var model;
model = new CrudModel();


/// The visualization, referenced by functions that display content
var root;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
function createVis()
{ 
	if (didCreateVis) return;
	
	// Create a div to house the tree visualization and the inspectors
	root = d3.select("#root");

	didCreateVis = true;
}

/**
 * Construct the table header.
 */
function showTableHeader(table)
{
	var header = table.selectAll("thead").data(function(d) { return [d.columns] });
	header.enter().append("thead");
	var headerRows = header.selectAll("tr.header").data(function(d) { return [d] });
	headerRows.enter().append("tr").attr("class", "header");
	var headerData = headerRows.selectAll("th.header").data(function(d) { return d; });
	headerData.enter().append("th").attr("class", "header");
	headerData.text(function (d) { return d.title})
}

/**
 * Construct the table data.
 */
function showTableData(table)
{
	var dataRows = table.selectAll("tr.data").data(function(d) { return d.rows });
	dataRows.enter().append("tr").attr("class", "data");
	dataRows.exit().remove();

	var dataData = dataRows.selectAll("td.data").data(function(d) { return d });
	dataData.enter().append("td").attr("class", "data");
	dataData.text(function (d) { return d.value});
}


/**
 * Display the samples returned by the server
 */
function displayRoot(data)
{
	if (data.error) {
		console.log(data.error);
		root.append("p").text("Could not retrieve data.");
		return;
	}
	
	// This will show the object in the log -- helpful for debugging
	// console.log(data.result);
	var tableData = data.result;
	
	// Display the rows in a table
	var table = root.selectAll("table").data([tableData]);
	// Code under enter is run if there is no HTML element for a data element	
	table.enter().append("table");
	showTableHeader(table);
	showTableData(table);
}

/**
 * Request samples matching some criteria from the server and show them in the Page.
 */
function callAggregationService()
{
	var parameters = {requestKey : 'ROOT'};

	openbisServer.createReportFromAggregationService("DSS1", "ipad-read-service-v1", parameters, displayRoot);
}


function enterApp(data)
{
	if(data.result == null){
		alert("Login or password incorrect");
		$("#username").focus();
		return;
	}

	$('#login').hide();
    $('#main').show();

    createVis();
    callAggregationService();
}