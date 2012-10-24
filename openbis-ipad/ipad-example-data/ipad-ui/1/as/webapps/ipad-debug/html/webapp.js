/// The openbisServer we use for our data
openbisServer = new openbis('/openbis/openbis', '/datastore_server');

function getAppHeight(){
	return Math.max($(window).height() - 50, getVisibleLeafsCountForNode(root) * 30);
}

function getAppWidth(){
	return $(window).width();
}


var didCreateVis = false;

function parseJson(jsonString) { return eval('(' + jsonString + ')'); }


/**
 * The model that manages state and implements the operations.
 */
function IpadModel() {
	this.initializeModel();
}

IpadModel.prototype.initializeModel = function() {
	this.selectionStack = [];
}

IpadModel.prototype.selectEntity = function(d) {
	this.selectionStack.push(d);
	var permId = d[0].value;
	var refcon = parseJson(d[1].value);
	var children = parseJson(d[5].value);
	if (children.length > 0) {
		console.log(["DETAILS", permId, refcon]);		
	} else {
		console.log(["DRILL", permId, refcon]);
	}
}

/// The model that manages state and implements the operations
var model;
model = new IpadModel();


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
	dataRows.enter().append("tr").on("click", function (d) { model.selectEntity(d); });
	dataRows.exit().remove();

	var dataData = dataRows.selectAll("td").data(function(d) { return d });
	dataData.enter().append("td");
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
	table.enter().append("table").attr("class", "table");
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