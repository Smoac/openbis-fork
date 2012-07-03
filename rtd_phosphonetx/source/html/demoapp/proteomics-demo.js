/// How much horizontal space do the inspectors take
var inspectorsWidth = 100;

/// A helper function for drawing the lines between nodes
var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x] }); 

/// The model for the visualization 
var model = null;

/// The visualization, referenced by functions that display content
var vis;

var didCreateVis = false;

var indexOfLinkedColumn = 3;

/**
 * Create the DOM elements to store the visualization (tree + inspectors)
 */
function createVis()
{ 
    if (didCreateVis) return;
    
    // Create a div to house the tree visualization and the inspectors
    vis = d3.select("#main").append("div").attr("id", "vis");

    didCreateVis = true;
}

function displayReturnedResults(data)
{
    $("#waiting").hide();

    if (data.error) {
        console.log(data.error);
        vis.append("p").text("Could not retrieve data.");
        return;
    }

    displayResultsAsGraph(createDataModel(data));
}

function displayResultsAsGraph(data) 
{
    clearVis();
    if (data.length == 0)
    {
        vis.append("p").text("Nothing found.");
        return;
    }
    
    var delayStep = 20,
        xOffset = 70,
        yOffset = 40,
        xStep = 200,
        yStep = 30,
        xLabel = -30,
        yLabel = 18,
        columnNames = ['Biological Experiment', 'Biological Sample', 'MS Injection Sample', 'Search Experiment', 'Protein'];
        
    var graph = vis.selectAll("svg").data([data]).enter().append("svg:svg")
        .attr("width", $(window).width() - 50)
        .attr("height", $(window).height() - 50);

    var g = graph.selectAll("g").data(function(d) { return d; })
        .enter().append("svg:g")
        .attr("transform", function(d,i) { return "translate(" + (xOffset + i * xStep) + ", "
                                                               + yOffset + ")"; });
    
    g.append("svg:text")
        .text(function(d,i) { return columnNames[i]; })
        .attr("class", "columnHeader")
        .attr("text-anchor", "middle")
        .attr("x", 0)
        .attr("y", -10);
        
    g.selectAll("path").data(function(d) { return d[1]; })
        .enter().append("svg:path")
        .attr("class", "line")
        .transition().delay(function(d,i) { return i * delayStep; })
        .attr("d", function(d,i) { 
            var y0 = d[0] * yStep;
            var y1 = d[1] * yStep;
            return "M0," + y0 
                 + "Q" + xStep/4 + "," + y0 + "," + xStep/2 + "," + (y0+y1)/2 
                 + "L" + xStep/2 + "," + (y0+y1)/2 
                 + "Q" + 3*xStep/4 + "," + y1 + "," + xStep + "," + y1
                 + "L" + xStep + "," + y1; 
            })

    var node = g.selectAll("g").data(function(d) { return d[0]; })
        .enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function(d,i) { return "translate(0, " + (i * yStep) + ")"; });
        
    node.append("svg:circle")        
        .transition().delay(function(d,i) { return i * delayStep; })
        .attr("r", 5);
            
    node.append("svg:text")
        .on("click", function(d) { if (d.level == indexOfLinkedColumn) {
                                       window.open(openbisUrl + "/?#entity=EXPERIMENT&permId=" + d.permId, '_blank');
                                   }
                                 })
        .transition().delay(function(d,i) { return i * delayStep; })
        .text(function(d) { return d.label; })
        .attr("class", function(d) { return d.level == indexOfLinkedColumn ? "linked" : "notLinked";})
        .attr("text-anchor", "left")
        .attr("x", xLabel)
        .attr("y", yLabel)
}


function Node(level, label, permId) {
    this.level = level;
    this.label = label;
    this.permId = permId;
}


function createDataModel(tableModel)
{
    var rows = tableModel.result.rows
    var maps = [];
    var permIds = {};
    
    for (n = 0; n < rows.length; n++) 
    {
        row = rows[n];
        permIds[row[indexOfLinkedColumn].value] = row[4].value;
        var accessionNumber = row[5].value;
        var splitted = accessionNumber.split("|");
        if (splitted.length > 1)
        {
            accessionNumber = splitted[1];
        }
        row[5].value = accessionNumber + ": " + row[6].value; // concatenate accession number with description
        row.splice(6, 1); // remove description
        row.splice(4, 1); // remove search experiment perm id
        for (i = 0; i < row.length; i++) {
            map = maps[i];
            if (map == null) {
                map = {};
                maps[i] = map;
            }
            cell = row[i].value;
            links = map[cell];
            if (links == null) {
                links = {};
                map[cell] = links;
            }
            if (i < row.length - 1) {
                links[row[i + 1].value] = 0;
            }
        }
    }
    var data = [];
    for (i = 0; i < maps.length; i++) {
        var map = maps[i];
        var elements = [];
        for (element in map) {
            var permId = null;
            if (i == indexOfLinkedColumn)
            {
                permId = permIds[element];
            }
            elements.push(new Node(i, element, permId));
        }
        elements.sort();
        var indexMap = {};
        for (j = 0; j < elements.length; j++) {
            indexMap[elements[j].label] = j;
        }
        data.push([elements, [], indexMap]);
    }
    
    for (i = 0; i < maps.length - 1; i++) {
        var map = maps[i];
        var from = data[i][0];
        var links = data[i][1];
        var indexMap = data[i + 1][2];
        for (j = 0; j < from.length; j++) {
            element = from[j];
            linkedElements = map[element.label];
            for (linkedElement in linkedElements) {
                links.push([j, indexMap[linkedElement]]);
            }
        }
    }
    
    return data;
}

function clearVis()
{
    $('#vis > *').remove();
}

/**
 * Request samples matching some criteria from the server and show them in the Page.
 */
function queryForResults(parameters)
{
    $("#waiting").show();
    openbisServer.createReportFromAggregationService("DSS1", "demo-proteomics-aggregation", parameters, displayReturnedResults);
//    openbisServer.createReportFromAggregationService("STANDARD", "demo-proteomics-aggregation", parameters, displayReturnedResults);
}


function enterApp(data)
{
    if(data.result == null)
    {
        alert("Login or password incorrect");
        $("#username").focus();
        return;
    }
    
    $("#login-form-div").hide();
    $("#main").show();
    
    $('#openbis-logo').height(30);
    
    createVis();
}