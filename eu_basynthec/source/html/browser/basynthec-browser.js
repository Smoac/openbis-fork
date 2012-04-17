// The width of the visualization
var w = 500;

/**
 * Abstract superclass for the wrapper classes.
 *
 * @constructor
 */
function AbstractThingWrapper() {
	this.isDataSet = function() { return false; }
	this.isStrain = function() { return false; }
}

/**
 * Create a wrapper on a data set designed to be displayed as a data set.
 * 
 * @constructor
 */
function DataSetWrapper(dataSet) {
	var strainNames;
	if (dataSet.properties[STRAIN_PROP_NAME] != null)
		strainNames = dataSet.properties[STRAIN_PROP_NAME].split(",");
	else
		strainNames = [dataSet.properties["STRAIN_NAME"]];
				
	this.strainNames = strainNames;
	this.dataSet = dataSet;
	this.dateString = timeformat(new Date(dataSet.registrationDetails.registrationDate));
	this.strainString = 
							(strainNames.length < 3) ?
								strainNames.join(" ") :
								"" + strainNames.length + " strain(s)";
	this.userEmail = dataSet.registrationDetails.userEmail;
	this.name = dataSet.code;
}

DataSetWrapper.prototype = new AbstractThingWrapper();
DataSetWrapper.prototype.constructor = DataSetWrapper;
DataSetWrapper.prototype.isDataSet = function() { return true; }

/**
 * Create a wrapper that represents a strain to be displayed
 *
 * @constructor
 */
function StrainWrapper(strainName)
{
	this.strainName = strainName;
	this.dataSets = [];
}

StrainWrapper.prototype = new AbstractThingWrapper();
StrainWrapper.prototype.constructor = StrainWrapper;
StrainWrapper.prototype.isStrain = function() { return true; }

/**
 * An object responsible for managing the view
 */ 
function AppPresenter() {
	this.didCreateVis = false;
}

/** Hides the explanation and shows the element to display the explanation again */
AppPresenter.prototype.hideExplanation = function() {
	$('#explanation').hide();
	$('#explanation-show').show();	
}

/** Display the explanation again */
AppPresenter.prototype.showExplanation = function() {
	$('#explanation-show').hide();		
	$('#explanation').show();
}

/** Show the data sets grouped by type */
AppPresenter.prototype.switchToDataSetTypeView = function()
{
	this.hideExplanation();
	this.toggleDisplayedVisualizations(dataSetTypeVis, strainVis);
}

/** Show the data sets by strain*/
AppPresenter.prototype.switchToStrainView = function()
{
	this.hideExplanation();
	this.toggleDisplayedVisualizations(strainVis, dataSetTypeVis);
}

/** Show the data sets by strains with OD600 data*/
AppPresenter.prototype.switchToOD600View = function()
{
	this.hideExplanation();
	this.toggleDisplayedVisualizations(strainVis, dataSetTypeVis);
}

/** Utility function to gracefully switch from one visualization to another */
AppPresenter.prototype.toggleDisplayedVisualizations = function(visToShow, visToHide)
{
	// TODO: Only include visToShow and hide all other visualizations, since we know what they are
	visToShow
	.style("display", "inline")
		.transition()
	.duration(1000)
	.style("opacity", 1);
	
	visToHide
		.transition()
	.duration(1000)
	.style("opacity", 0)
	.style("display", "none");
}

/**
 * Shows the list of data sets retrieved from the openBIS server.
 */
AppPresenter.prototype.showDataSets = function(bisDataSets) {
	if (null == bisDataSets) return;
	
	basynthec.dataSetList = bisDataSets.filter(function(dataSet) { 
		return IGNORED_DATASET_TYPES.indexOf(dataSet.dataSetTypeCode) == -1;
  });
	
	// sort data sets
	var sortByTypeAndRegistration = function(a, b) {
		if (a.dataSetTypeCode == b.dataSetTypeCode) {
			return b.registrationDetails.registrationDate - a.registrationDetails.registrationDate;
		}
		return (a.dataSetTypeCode < b.dataSetTypeCode) ? -1 : 1;
	};
	
	basynthec.dataSetList.sort(sortByTypeAndRegistration);
	
	model.initializeDataSetsByType();
	model.initializeDataSetsByStrain();
	
	presenter.refreshDataSetTypeTables();
	presenter.refreshStrainTables();
}

AppPresenter.prototype.refreshStrainTables = function() {
	model.initializeStrainGroups();
	
  this.createVis();
 	strainView.updateView(1000);
}

AppPresenter.prototype.refreshDataSetTypeTables = function() {
  this.createVis();

	od600View.updateView();
	metabolomicsView.updateView();
	transcriptomicsView.updateView();
	proteomicsView.updateView();
}

AppPresenter.prototype.createVis = function()
{
	if (this.didCreateVis) return;
	
	var top = d3.select("#main");
	var tableRoot = top.append("div").attr("id", "table-root").style("width", w + 5 + "px").style("float", "left");
	
	
	// An element for the inspectors.
	inspectors = top.append("span")
		.style("width", "500px")
		.style("position", "relative")
		.style("overflow", "auto")
		.style("float", "left")
		.style("left", "20px");

	// Create the dataSetType visualization
	dataSetTypeVis = tableRoot.append("div");
	dataSetTypeVis.style("width", w + "px");
	od600View = new DataSummaryView(dataSetTypeVis, "OD600", "od600", "OD600");
	metabolomicsView = new DataSummaryView(dataSetTypeVis, "Metabolomics", "metabolomics", "METABOLITE_INTENSITIES");
	transcriptomicsView = new DataSummaryView(dataSetTypeVis, "Transcriptomics", "transcriptomics", "TRANSCRIPTOMICS");
	proteomicsView = new DataSummaryView(dataSetTypeVis, "Proteomics", "proteomics", "PROTEIN_QUANTIFICATIONS");
	
	// Initially hide the strain view -- it is activated by the radio button
	strainVis = tableRoot.append("div").style("display", "none");
	strainVis.style("width", w + "px");
	strainView = new StrainView();
	
	inspectorView = new InspectorView();
	
	this.didCreateVis = true;
}

AppPresenter.prototype.updateInspectors = function(duration) 
{
	inspectorView.updateView(duration);
}

/** Download a file referenced in a table. */
AppPresenter.prototype.downloadTableFile = function(d)
{
	// If there is no dataset, this is just a marker for loading
	if (!d.dataset) return;
	
	var action = function(data) { 
		try {
			document.location.href = data.result
		} catch (err) {
			// just ignore errors		
		} 
	};
	basynthec.server.getDownloadUrlForFileForDataSet(d.dataset.bis.code, d.pathInDataSet, action);
}

AppPresenter.prototype.toggleInspected = function(d, connectedNode) {
	this.hideExplanation();

	var lexicalParent = this;
	
	function retrieveFilesForDataSets(dataSets) { 
		dataSets.forEach(function(ds) { lexicalParent.retrieveFilesForDataSet(ds); });
	}

	if (d.inspected) {
		var index = inspected.indexOf(d) 
		if (index > -1)	inspected.splice(index, 1);
		d.inspected = false;
	} else {
		d.inspected = true;
		d.connectedNode = connectedNode;
		inspected.push(d);
		if (d instanceof DataSetWrapper) {
			d.dataSets = [{ bis : d.dataSet }];
			retrieveFilesForDataSets(d.dataSets);
		} else if (!d.dataSets) {
			d.dataSets = model.dataSetsByStrain[d.name].dataSets.map(function(ds){ return {bis : ds} }); 
			retrieveFilesForDataSets(d.dataSets);
		}
	}
	
	d3.select(d.connectedNode).attr("class", classForNode(d))
  this.updateInspectors(500);
}

AppPresenter.prototype.retrieveFilesForDataSet = function(ds)
{
	if (ds.files) {
		// already retrieved
		return;
	}
	
	ds.loadingFiles = true;
	ds.files = [];
	
	var lexicalParent = this;

	basynthec.server.listFilesForDataSet(ds.bis.code, "/", true, function(data) {					
		if (!data.result) { 
			return;
		}
		data.result.forEach(function (file) { file.dataset = ds });
		ds.files = ds.files.concat(data.result);
		
		ds.loadingFiles = false; 
		presenter.updateInspectors(500);
		
		if (isOd600DataSet(ds)) {
			lexicalParent.retrieveOd600DataForDataSet(ds)
		}
				
	});
}

/** Load the OD600 data from the server. This function assumes that the files are already known. */
AppPresenter.prototype.retrieveOd600DataForDataSet = function(ds)
{
	if (ds.od600Rows) {
		// already retrieved
		return;
	}
	
	ds.loadingOd600 = true;
	ds.od600Rows = [];
	
	// Figure out the path to the multistrain TSV file -- this path ends with "xls.tsv".
	var tsvPathInDataSet = "";
	ds.files.forEach(function (file) { if (endsWith(file.pathInDataSet, "xls.tsv")) tsvPathInDataSet = file.pathInDataSet});
		
	var tsvUrl = dssUrl + "/" + ds.bis.code + "/" + tsvPathInDataSet + "?sessionID=" + basynthec.server.sessionToken;
	
	var lexicalParent = this;

	d3.text(tsvUrl, "text/tsv", function(text) {
		var rows = d3.tsv.parseRows(text, "\t");
		ds.od600Rows = rows;
		lexicalParent.initializeOd600Map(ds);
		ds.loadingOd600 = false;
		presenter.updateInspectors(500);
	});	
}

/** 
 * Initialize a map of the OD600 data, where the key is the strain and the value is an array 
 * 
 * Assumes that ds.od600Rows has already been set
 */
AppPresenter.prototype.initializeOd600Map = function(ds) 
{
	ds.od600Map = {};
	// The first line of data contains the timepoints
	ds.od600Timepoints = ds.od600Rows[0].slice(2);
	var i;
	for (i = 1; i < ds.od600Rows.length; ++i) {
		var line = ds.od600Rows[i];
		var strain = line[0].toUpperCase();
		var data = line.slice(2);
		ds.od600Map[strain] = data;
	}
}


function classForNode(d) { return  (d.inspected) ? "inspected" : ""; }


/**
 * An object responsible for managing the data to show
 */
function AppModel() {
	// a map holding data sets by strain
	this.dataSetsByStrain = { };

	// Groups of strains to be displayed  
	this.strainGroups = [];

	// A map holding data sets by type
	this.dataSetsByType = { };
}

/** Compute the dataSetsByType variable */
AppModel.prototype.initializeDataSetsByType = function() {
	// Group data sets by type
	this.dataSetsByType = basynthec.dataSetList.reduce(
		function(result, dataSet) {
			var listForType = result[dataSet.dataSetTypeCode];
			if (listForType == null) {
				listForType = [];
				result[dataSet.dataSetTypeCode] = listForType;
			}
			listForType.push(new DataSetWrapper(dataSet));
			return result;
		}, {});
}

/** Compute the dataSetsByStrain variable */
AppModel.prototype.initializeDataSetsByStrain = function() {
	// group dataSets
	this.dataSetsByStrain = basynthec.dataSetList.reduce(
		function(result, dataSet) { 
			var uniqueStrains = uniqueElements(basynthec.getStrains(dataSet).sort());
			
			uniqueStrains.forEach(function(strain) {
					if (!result[strain]) {
						result[strain] = new StrainWrapper(strain);
					}
					result[strain].dataSets.push(dataSet);
			});
			return result;
		}, {});
}

AppModel.prototype.initializeStrainGroups = function() {
	var strains = []
	for (strainName in this.dataSetsByStrain) {
		strains.push(strainName)
	}
	this.strainGroups = createStrainGroups(strains);
}

/**
 * A utility function that groups strains together based on strain name
 */ 
function createStrainGroups(strains) {
	// prefixes of strain names to be grouped togehter
	var STRAIN_GROUP_PREFIXES = [ "JJS-DIN", "JJS-MGP" ];

	// The names to show the user for the strain groups
	var STRAIN_GROUP_PREFIXES_DISPLAY_NAME = {"JJS-DIN" : "JJS-DIn", "JJS-MGP" : "JJS-MGP" };
	
	var groups = STRAIN_GROUP_PREFIXES.map(
			function(strainPrefix) {
				var filtered = strains.filter(function(strain) { 
			    return strain.indexOf(strainPrefix) >= 0
			  });
				var groupStrains = filtered.map(function(strain) {
					return { name : strain, label : strain.substring(strainPrefix.length)};
			  });
				
				return {groupName : STRAIN_GROUP_PREFIXES_DISPLAY_NAME[strainPrefix], strains : groupStrains};
	});
	
	var otherStrains = strains.filter(function(strain) {
      return false == STRAIN_GROUP_PREFIXES.some(function(prefix) { return strain.indexOf(prefix) >=0; } );
	});
	otherStrains = otherStrains.map(function(strain) { return {name:strain, label:strain}});
	groups.push({groupName : "Other strains", strains : otherStrains});
	
	var sortFunction = sortByProp("name")
	groups.forEach(function(group) { group.strains.sort(sortFunction); });

	// only return groups that have strains
	return groups.filter(function(group) { return group.strains.length > 0 });
}

/**
 * View that groups data sets by type.
 */
function DataSummaryView(group, typeName, id, type) {
	this.group = group;
	this.dataSetTypeName = typeName;
	this.dataSetType = type;
	this.nodeId = id;
	this.viewNode = this.createDataSetSummaryViewNode();
}

DataSummaryView.prototype.createDataSetSummaryViewNode = function()
{
	var container, result;
	container = this.group.append("div");

	container.append("h2").attr("class", "datasetsummarytable").text(this.dataSetTypeName);
	
	result =  
		container.append("div")
			.attr("id", this.nodeId)
			.attr("class", "datasummaryview");
			
	return result;
}

DataSummaryView.prototype.updateView = function()
{
	var dataSetsForType = model.dataSetsByType[this.dataSetType];
	
	if (dataSetsForType == null) {
		this.viewNode.selectAll("p")
			.data(["No Data"])
		.enter()
			.append("p")
			.text("No Data");
		return;
	}
	
	this.viewNode.selectAll("table")
		.data([dataSetsForType])
	.enter()
		.append("table")
		.attr("class", "datasetsummarytable")
		.selectAll("tr")
			.data(function (d) { return d})
		.enter()
			.append("tr")
			.on("click", toggleInspected)
				.selectAll("td")
					.data(function (d) { return [d.dateString, d.userEmail, d.strainString] })
				.enter()
					.append("td")
					.style("width", "33%")
					.text(function (d) { return d});
}

function toggleInspected(d) { presenter.toggleInspected(d, this) }

function StrainView() {
	
}

StrainView.prototype.updateView = function(duration)
{
	var strainDiv = strainVis.selectAll("div.strains").data(model.strainGroups)
		.enter()
	.append("div")
		.attr("class", "strains");

	strainDiv
		.append("h2")
			.text(function(d) { return d.groupName });
	strainDiv
		.append("table")
			.selectAll("tr").data(function(d) { 
					// Group the different sets of strains differently
					if (d.groupName.indexOf("Other") == 0) return d.strains.reduce(groupBy(3), []);
					if (d.groupName.indexOf("JJS-MGP") == 0) return d.strains.reduce(groupBy(10), []);
				
					// Group the JJS-DIn strains by runs
					return d.strains.reduce(groupByRuns(10), []) })
				.enter()
			.append("tr")
			.selectAll("td").data(function(d) { return d })
				.enter()
			.append("td")
			.on("click", toggleInspected)
			.text(function(d) { return d.label });
}

function InspectorView() {
	
}

InspectorView.prototype.updateView = function(duration)
{	
	var inspector = inspectors.selectAll("div.inspector").data(inspected, function (d) { return d.name });
	
	var box = inspector.enter().append("div")
		.attr("class", "inspector")
		.text(function(d) { return d.name });

	box.append("span")
		.attr("class", "close")
		.on("click", toggleInspected)
		.text("x");
	
	var dataSetList = inspector.selectAll("ul").data(function (d) { return [d] });
	dataSetList.enter()
	  .append("ul")
	  .attr('class', 'dataSets');
	
	
	var dataSetElt = dataSetList.selectAll("li").data(function (d) { return d.dataSets });
	dataSetElt.enter()
	  .append("li")
	  .text(function(d) { return dataSetLabel(d) });
	
	var dataSetDetailsElt = dataSetElt.selectAll("div.dataSetDetails").data(function(d) { return [d]; });
	dataSetDetailsElt
	  .enter()
	    .append("div")
	      .attr("class", "dataSetDetails"); 
	
	var propsTable = dataSetDetailsElt.selectAll("table.properties").data(function(d) {return [d]});
	
	propsTable.enter()
	  .append("table")
	  .attr("class", "properties");
	
	propsTable.selectAll("tr").data(function(d) { return props_to_pairs(d.bis.properties) })
		.enter()
			.append("tr")
			.selectAll("td").data(function(d) { return d } ).enter()
				.append("td")
				.attr("class", function(d, i) { return (i == 0) ? "propkey" : "propvalue"})
				.style("opacity", "0")
				.text(function(d) { return d })
			.transition()
				.style("opacity", "1");
	
	var downloadTable = dataSetDetailsElt.selectAll("table.downloads").data(function(d) { return [d] });
	
	downloadTable
		.enter()
			.append("table")
				.attr("class", "downloads")
			
	// Add a caption, but make sure there is just one (this does not work with select())
	downloadTable.selectAll("caption").data(["Files"])
		.enter()
			.append("caption").text(function(d) { return d; });
			
	// We just want to see non-directories here
	var downloadTableRow = downloadTable.selectAll("tr").data(filesForDataSet, function(d) { return d.pathInDataSet });
	downloadTableRow
		.enter()
			.append("tr")
				.append("td")
				.on("click", downloadTableFile)
				.text(function(d) { return d.pathInListing });
	downloadTableRow
		.exit()
			.transition()
				.duration(duration)
				.style("opacity", "0")
				.remove();
				
	var height = 200, width = 200;
	var dataDisplay = dataSetDetailsElt.selectAll("svg").data(od600DataForDataSet);
	dataDisplay
		.enter()
	.append("svg:svg")
		.attr("height", height)
		.attr("width", width);
	// Reinitialize the variable
	dataDisplay = dataSetDetailsElt.selectAll("svg").data(od600DataForDataSet);
	var aCurve = dataDisplay.selectAll("g").data(function(d) { return [d[1]]; })
				.enter()
			.append("svg:g");
	// Reinitialize the variable
	aCurve = dataDisplay.selectAll("g").data(curveData);
		// The first two columns of data are the strain name and human-readable desc
	aCurve.selectAll("line").data(lineData)
		.enter()
	.append("svg:line")
		.attr("x1", function(d, i) { return (i / (this.parentNode.__data__.length)) * width; })
		.attr("y1", function(d, i) { return height - (d[0] * height); })
		.attr("x2", function(d, i) { return ((i + 1) / (this.parentNode.__data__.length)) * width;})
		.attr("y2", function(d) { return height - (d[1] * height); })
		.style("stroke", "rgb(0,0,0)")
		.style("stroke-width", "1");
	
	inspector.exit().transition()
		.duration(duration)
		.style("opacity", "0")
		.remove();
}

function dataSetLabel(d) { return d.bis.dataSetTypeCode + " registered on " + timeformat(new Date(d.bis.registrationDetails.registrationDate));  }

function downloadTableFile(d) { presenter.downloadTableFile(d) }

function filesForDataSet(d)
{
	if (d.loadingFiles) return [{ pathInListing : "Loading..." }];
	
	var fileFilter = function(file) {
		if (!file.isDirectory) {
			if (endsWith(file.pathInDataSet, "xls")) {
				return true;
			}
			if (endsWith(file.pathInDataSet, "xls.tsv")) {
				return true;
			}
		}
		return false;
	};
	
	return (d.files) ? d.files.filter(fileFilter) : [];
}


var model = new AppModel();
var presenter = new AppPresenter();

// The data set type visualization
var dataSetTypeVis, od600View, metabolomicsView, transcriptomicsView, proteomicsView;

// The strain visualization
var strainVis, strainView;

var IGNORED_DATASET_TYPES = [ "EXCEL_ORIGINAL", "TSV_EXPORT", "UNKNOWN" ];

//The inspected strains
var inspected = [];

//The node inspectors
var inspectors, inspectorView;


function isOd600DataSet(d) { return "OD600" == d.bis.dataSetTypeCode}

function od600DataForDataSet(d)
{
	if (!isOd600DataSet(d)) return [];

	if (undefined == d.od600Rows) return [[]];
	
	return [d.od600Rows.slice(1)];
}

function curveData(d)
{
	if (!d) return [];
	if (d.length < 2) return [];
	var data = d[1].slice(2);
	return [{length : data.length, max : d3.max(data), values: data}]
}

function lineData(d)
{
	if (!d) return [];
	
	var data = d.values;
	// convert the data into pairs
	var pairs = data.reduce(function(sum, elt) {
		// initialization
		if (sum.length < 1) {
			sum.push([elt / d.max]);
			return sum;
		}
		
		// add the current elt as the second in the last pair and the first in the new pair
		sum[sum.length - 1].push(elt / d.max);
		// don't add the very last element
		if (sum.length < data.length - 1) sum.push([elt / d.max]);
		return sum;
	}, []);
	
	return pairs;
}

function shouldRenderProperty(prop, value) {
	// strain properties are dealt with separately	
	if (prop == STRAIN_PROP_NAME) return false;

	// do not show properties with no values	
	if (!value) return false;
	return true;
}

/**
 * Convert properties to pairs
 */
function props_to_pairs(d)
{
	var pairs = [];
	
	var dataSetStrains = basynthec.getStrains({properties:d});
	var strainGroups = createStrainGroups(dataSetStrains);
	
	strainGroups.forEach(function(group) {
		var shortedStrains = group.strains.map(function(elt) { return elt.label; });
		shortedStrains = uniqueElements(shortedStrains.sort())
		var pair = [ group.groupName, shortedStrains.join(" ") ];
		pairs.push(pair)
	});
	
	for (var prop in d) {
		if (shouldRenderProperty(prop, d[prop])) {
			var pair = [prop, d[prop]];
			pairs.push(pair);
	  }
	}
	pairs.sort(function(a, b) { 
		if (a[0] == b[0]) return 0;
		// Sort in reverse lexicographical
		return (a[0] < b[0]) ? -1 : 1;
	});
	return pairs;
}

function enterApp()
{
	$("#login-form-div").hide();
	$("#main").show();
	basynthec.listAllDataSets(function(data) { 
		presenter.showDataSets(data.result); 
	});
	
	$('#openbis-logo').height(50)
	
}
