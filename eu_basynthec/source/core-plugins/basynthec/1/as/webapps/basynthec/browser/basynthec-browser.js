// The width of the visualization
var w = 500;

var curveColors = d3.scale.category10().domain([0, 9]);

/**
 * Abstract superclass for the wrapper classes.
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

var presenterModeTypeDataSet = "DATA_SET", presenterModeTypeStrain = "STRAIN";

/**
 * An object responsible for managing the view
 */ 
function AppPresenter() {
	this.didCreateVis = false;
	this.presenterMode = presenterModeTypeDataSet;
	this.visualizationContainers = [];
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
	this.presenterMode = presenterModeTypeDataSet;
	this.hideExplanation();
	this.toggleDisplayedVisualizations(dataSetTypeVis);
	od600InspectorView.removeAll(250);
	dataSetInspectorView.updateView();	
}

/** Show the data sets by strain*/
AppPresenter.prototype.switchToStrainView = function()
{
	this.presenterMode = presenterModeTypeDataSet;
	this.hideExplanation();
	this.toggleDisplayedVisualizations(strainVis);
	od600InspectorView.removeAll(250);	
	dataSetInspectorView.updateView();		
}

/** Show the data sets by strains with OD600 data*/
AppPresenter.prototype.switchToOD600View = function()
{
	this.presenterMode = presenterModeTypeStrain;
	this.hideExplanation();
	this.toggleDisplayedVisualizations(od600StrainVis);
	dataSetInspectorView.removeAll(250);
	od600InspectorView.updateView();
}

/** This view is very similar to the OD600 view, but the data is also divided into two main groups:
 * - strains for which there are phenotypes, predictions, and data in the openBIS database
 * - strains for which there are phenotypes or predictions, but no data in the openBIS database
 *   (in this group strains with phenotypes and predictions should be marked green; strains with 
 *   phenotypes only should be marked blue; strains with predictions only should be yellow) 
 * Information about the phenotypes and predictions is retrieved from UChicago strain database
 * (http://pubseed.theseed.org/model-prod/StrainServer.cgi) and cached at the server-side in OpenBIS.
 */
AppPresenter.prototype.switchToOD600WithPhenotypesAndPredictionsView = function()
{
	this.presenterMode = presenterModeTypeStrain;
	this.hideExplanation();
	this.toggleDisplayedVisualizations(od600StrainWithPhenotypesAndPredictionsVis);
	dataSetInspectorView.removeAll(250);
	od600InspectorView.updateView();
}

/** Utility function to gracefully switch from one visualization to another */
AppPresenter.prototype.toggleDisplayedVisualizations = function(visToShow)
{
	this.visualizationContainers.forEach(function(vis) {
		if (vis == visToShow) {
			if (od600StrainVis == vis || od600StrainWithPhenotypesAndPredictionsVis == vis) {
				// So that scrolling works
				vis.style("display", "block");
			} else {
				vis.style("display", "inline");
			}
			vis
				.transition()
			.duration(1000)
			.style("opacity", 1);
		} else {
			// change to "inline" element to eliminate jumping of two "block" elements during transition
			vis.style("display","inline");
			vis
				.transition()
			.duration(1000)
			.style("opacity", 0)
			.style("display", "none")
		}
	});
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

	model.initialize(function(){
		presenter.refreshDataSetTypeTables();
		presenter.refreshStrainTables();
		presenter.refreshOd600StrainTables();
		presenter.refreshOd600StrainWithPhenotypesAndPredictionsTables();
	});
}

AppPresenter.prototype.refreshStrainTables = function() {
  this.createVis();
 	strainView.updateView(1000);
}

AppPresenter.prototype.refreshDataSetTypeTables = function() {
  this.createVis();

	od600View.updateView();
	metabolomicsView.updateView();
	transcriptomicsView.updateView();
	proteomicsView.updateView();
	sequencesCm5View.updateView();
	sequencesGenbankView.updateView();
	intervalAnnotationsView.updateView();
	metabolicModelView.updateView();		
}

AppPresenter.prototype.refreshOd600StrainTables = function() {
  this.createVis();
 	od600StrainView.updateView(1000);
}

AppPresenter.prototype.refreshOd600StrainWithPhenotypesAndPredictionsTables = function() {
  this.createVis();
  od600StrainWithPhenotypesAndPredictionsView.updateView(1000);
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
	sequencesCm5View = new DataSummaryView(dataSetTypeVis, "Sequences (CM5)", "sequencesCm5", "SEQUENCE_CM5");
	sequencesGenbankView = new DataSummaryView(dataSetTypeVis, "Sequences (Genbank)", "sequencesGenbank", "SEQUENCE_GENBANK");
	intervalAnnotationsView = new DataSummaryView(dataSetTypeVis, "Interval Annotations", "intervalAnnotations", "INTERVAL_ANNOTATIONS");
	metabolicModelView = new DataSummaryView(dataSetTypeVis, "Metabolic Model", "metabolicModel", "METABOLIC_MODEL");
	
	// Initially hide the strain view -- it is activated by the radio button
	strainVis = tableRoot.append("div").style("display", "none");
	strainVis.style("width", w + "px");
	strainVis.style("opacity", "0");
	strainView = new StrainView();
	
	od600StrainVis = tableRoot.append("div").style("display", "none");
	od600StrainVis.style("width", w + "px");
	od600StrainVis.style("height",w + "px");
	od600StrainVis.style("overflow-y", "scroll");
	od600StrainVis.style("opacity", "0");
	od600StrainView = new Od600StrainView();
	
	od600StrainWithPhenotypesAndPredictionsVis = tableRoot.append("div").style("display", "none");
	od600StrainWithPhenotypesAndPredictionsVis.style("width", w + "px");
	od600StrainWithPhenotypesAndPredictionsVis.style("opacity", "0");
	
	od600StrainWithPhenotypesAndPredictionsVisScroll = od600StrainWithPhenotypesAndPredictionsVis.append("div");
	od600StrainWithPhenotypesAndPredictionsVisScroll.style("height", ($(window).height() - 250) + "px");
	od600StrainWithPhenotypesAndPredictionsVisScroll.style("overflow-y", "scroll");
	
	od600StrainWithPhenotypesAndPredictionsVisLegend = od600StrainWithPhenotypesAndPredictionsVis.append("div");
	
	od600StrainWithPhenotypesAndPredictionsView = new Od600StrainWithPhenotypesAndPredictionsView();
	
	this.visualizationContainers = [dataSetTypeVis, strainVis, od600StrainVis, od600StrainWithPhenotypesAndPredictionsVis];
	
	dataSetInspectorView = new DataSetInspectorView();
	od600InspectorView = new Od600InspectorView();
	
	this.didCreateVis = true;
}

AppPresenter.prototype.updateInspectors = function(duration) 
{
	if (this.presenterMode == presenterModeTypeDataSet)
	{
		dataSetInspectorView.updateView(duration);
	} else {
		od600InspectorView.updateView(duration);
	}
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
	
	function classForNode(d) { return  (d.inspected) ? "inspected" : "" };

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

AppPresenter.prototype.toggleOd600Inspected = function(d) {
	this.hideExplanation();

	var lexicalParent = this;
	
	function retrieveFilesForDataSets(dataSets) { 
		dataSets.forEach(function(ds) { lexicalParent.retrieveFilesForDataSet(ds); });
	}
	
	function classForNode(d) { return  (d.od600Inspected) ? "inspected" : "" };

	var inspectedItem = null;
	var inspectedIndex = null;
	
	$.each(od600Inspected, function(index, item){
		if(item.name == d.name){
			inspectedItem = item;
			inspectedIndex = index;
			return false;
		}
	});
	
	if (inspectedItem) {
		od600Inspected.splice(inspectedIndex, 1);
	} else {
		od600Inspected.push(d);
		if (d instanceof DataSetWrapper) {
			d.dataSets = [{ bis : d.dataSet }];
			retrieveFilesForDataSets(d.dataSets);
		} else if (!d.dataSets && d.data.isKnown) {
			d.dataSets = model.dataSetsByStrain[d.name].dataSets.map(function(ds){ return {bis : ds} });
			retrieveFilesForDataSets(d.dataSets);
		}
	}
	
    this.updateInspectors(500);
    od600StrainView.updateView(1);
    od600StrainWithPhenotypesAndPredictionsView.updateView(1);
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
		var data = line.slice(2).map(function(str) { return parseFloat(str)});
		if (null == ds.od600Map[strain]) ds.od600Map[strain] = [];
		ds.od600Map[strain].push(data);
	}
}


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

	// A set of all known growth media 
	this.growthMediaSet = {};
}

AppModel.prototype.initialize = function(callback) {
	this.initializeDataSetsByType();
	this.initializeDataSetsByStrain();
	this.initializeStrainGroups();
	this.initializeOd600Model();
	this.initializeOd600WithPhenotypesAndPredictionsModel(callback);
}

AppModel.prototype.addGrowthMedium = function(medium, strain) {
	var mediumList = this.growthMediaSet[medium];
	if (null == mediumList) {
		mediumList = [];
		this.growthMediaSet[medium] = mediumList;
	}
	mediumList.push(strain);
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
		strains.push({name: strainName});
	}
	this.strainGroups = createStrainGroups(strains);
}

/** Initialize the state necessary for the OD600 visualization. This must be run after initializeDataSetsByType */
AppModel.prototype.initializeOd600Model= function() {
	var colors = d3.scale.category20c();
	var dataSets = this.od600DataSets();
	var od600ExperimentIdentifiers = dataSets.map(function(ds) {
		return ds.dataSet.experimentIdentifier;
	});
	
	od600ExperimentIdentifiers.sort();
	this.od600Experiments = od600ExperimentIdentifiers.map(function(expId, i) {
		return { identifier: expId, color: colors(i) };
	});
	
	// Filter the strain groups to those that have OD600 data
	this.od600StrainGroups = this.strainGroups.map(function(group) {
		var strains = group.strains.filter(function(strain) {
			return model.dataSetsByStrain[strain.name].dataSets.some(function(ds) {
				return "OD600" == ds.dataSetTypeCode;
			})
		});
		return {groupName : group.groupName, strains : strains };
	});
}

AppModel.prototype.initializeOd600WithPhenotypesAndPredictionsModel = function(callback){
	var model = this;
	
	basynthec.getStrainsPhenotypesAndPredictions(function(strainDataMap){
		
		var strainsKnownToOpenbisWithPhenotypesOrPredictions = [];
		var strainsUnknownToOpenbisWithPhenotypesOrPredictions = [];
		
		for(strainName in model.dataSetsByStrain){
			var strainData = strainDataMap[strainName];
			
			if(!strainData){
				continue;
			}
			
			var strainDatasets = model.dataSetsByStrain[strainName];
			strainData.phenotypeMap = {};
			if (strainData.hasPhenotypes) {
				strainData.phenotypes.forEach(function(each) { 
					strainData.phenotypeMap[each.media] = each.relativeGrowth;
					model.addGrowthMedium(each.media, strainName);
				});
			}
			
			var hasPhenotypesOrPredictions = strainData && (strainData.hasPhenotypes || strainData.hasPredictions);
		    var hasOd600Datasets = strainDatasets && strainDatasets.dataSets.some(function(dataset){
		    	return "OD600" == dataset.dataSetTypeCode;
		    });
			
			if (hasOd600Datasets) {
			  if(hasPhenotypesOrPredictions){
				  strainData.isKnown = true;
				  strainsKnownToOpenbisWithPhenotypesOrPredictions.push(strainData);
			  } else {
				  strainData.isKnown = true;
				  strainsKnownToOpenbisWithPhenotypesOrPredictions.push(strainData);
			  }
			} else {
				if (strainDatasets && strainDatasets.dataSets.length > 0) {
				  strainData.isKnown = true;
				  strainsKnownToOpenbisWithPhenotypesOrPredictions.push(strainData);
				}
			}
		}
		
		for(strainName in strainDataMap){
			var strainData = strainDataMap[strainName];
			var strainDatasets = model.dataSetsByStrain[strainName];
			
			var hasPhenotypesOrPredictions = strainData && (strainData.hasPhenotypes || strainData.hasPredictions);
			var hasDatasets = strainDatasets && strainDatasets.dataSets.length > 0;
			
			if(hasPhenotypesOrPredictions && !hasDatasets) {
				strainData.isKnown = false;
				strainsUnknownToOpenbisWithPhenotypesOrPredictions.push(strainData);
			}

			strainData.phenotypeMap = {};
			if (strainData.hasPhenotypes) {
				strainData.phenotypes.forEach(function(each) {
					strainData.phenotypeMap[each.media] = each.relativeGrowth;
					model.addGrowthMedium(each.media, strainName);
				});
			}
		}
		
		model.od600StrainsWithPhenotypesAndPredictionsGroups = [];
		model.od600StrainsWithPhenotypesAndPredictionsGroups.push({
			"mainGroupName" : "Strains with data in openBIS",
			"groups" : createStrainGroups(strainsKnownToOpenbisWithPhenotypesOrPredictions)
		});
		model.od600StrainsWithPhenotypesAndPredictionsGroups.push({
			"mainGroupName" : "Strains without data in openBIS",
			"groups" : createStrainGroups(strainsUnknownToOpenbisWithPhenotypesOrPredictions)
		});

		var commonGrowthMedia = []
		var allGrowthMedia = []
		for (medium in model.growthMediaSet) {
			allGrowthMedia.push(medium);
			if (model.growthMediaSet[medium].length > 20) {
				commonGrowthMedia.push(medium);
			}
		}
		allGrowthMedia.sort();
		commonGrowthMedia.sort();
		model.allGrowthMedia = allGrowthMedia;
		model.commonGrowthMedia = commonGrowthMedia;
		
		callback();
	});
	
}

AppModel.prototype.od600DataSets = function() {
	var ds = this.dataSetsByType["OD600"];
        return ds ? ds : [];
}

/**
 * A utility function that groups strains together based on strain name
 */ 
function createStrainGroups(strains) {
	if(!strains || strains.length == 0){
		return [];
	}
	
	// prefixes of strain names to be grouped togehter
	var STRAIN_GROUP_PREFIXES = [ "JJS-DIN", "JJS-MGP" ];

	// The names to show the user for the strain groups
	var STRAIN_GROUP_PREFIXES_DISPLAY_NAME = {"JJS-DIN" : "JJS-DIn", "JJS-MGP" : "JJS-MGP" };
	
	var groups = STRAIN_GROUP_PREFIXES.map(
			function(strainPrefix) {
				var filtered = strains.filter(function(strain) { 
			    return strain.name.indexOf(strainPrefix) >= 0
			  });
				var groupStrains = filtered.map(function(strain) {
					return { name : strain.name, label : strain.name.substring(strainPrefix.length), data: strain};
			  });
				
				return {groupName : STRAIN_GROUP_PREFIXES_DISPLAY_NAME[strainPrefix], strains : groupStrains};
	});
	
	var otherStrains = strains.filter(function(strain) {
      return false == STRAIN_GROUP_PREFIXES.some(function(prefix) { return strain.name.indexOf(prefix) >=0; } );
	});
	otherStrains = otherStrains.map(function(strain) { return {name:strain.name, label:strain.name, data: strain}});
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


function StrainView() {
	
}

function toggleInspected(d) { presenter.toggleInspected(d, this) }

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

function toggleOd600Inspected(d) { presenter.toggleOd600Inspected(d, this) }

function isOd600Inspected(d) {
	return od600Inspected.some(function(elem, index){
		return elem.name == d.name;
	});
}

/**
 * The view for the OD600 strains
 *
 * @constructor
 */
function Od600StrainView() {
	
}

Od600StrainView.prototype.updateView = function(duration)
{
	var strainDiv = od600StrainVis.selectAll("div.strains").data(model.od600StrainGroups)
	strainDiv.enter().append("div").attr("class", "strains").append("h2").text(function(d) { 
		return d.groupName 
	})
	
	var tables = strainDiv.selectAll("table").data(function(d){
		return [d];
	})
	tables.enter().append("table");
	
	var trs = tables.selectAll("tr").data(function(d) {
					// Group the different sets of strains differently
					if (d.groupName.indexOf("Other") == 0) return d.strains.reduce(groupBy(3), []);
					if (d.groupName.indexOf("JJS-MGP") == 0) return d.strains.reduce(groupBy(10), []);
				
					// Group the JJS-DIn strains by runs
					return d.strains.reduce(groupByRuns(10), []) })
    trs.enter().append("tr")
			
    var tds = trs.selectAll("td").data(function(d) { return d });
	tds.enter()
			.append("td")
			.on("click", toggleOd600Inspected)
			.text(function(d) { return d.label })
	tds.attr("class", function(d){
		if(isOd600Inspected(d)){
			return "inspected"
		}else{
			return "";
		}
	})
}

/**
 * The view for the OD600 strains with phenotypes and predictions
 *
 * @constructor
 */
function Od600StrainWithPhenotypesAndPredictionsView(){
}

Od600StrainWithPhenotypesAndPredictionsView.prototype.updateView = function(duration)
{
	var mainGroupDiv = od600StrainWithPhenotypesAndPredictionsVisScroll.selectAll("div.strainsMainGroup").data(model.od600StrainsWithPhenotypesAndPredictionsGroups);
	mainGroupDiv.enter().append("div").attr("class", "strainsMainGroup").append("h2").text(function(d) { 
		return d.mainGroupName 
	});	

	var strainDiv = mainGroupDiv.selectAll("div.strains").data(function(d){ 
		return d.groups; 
	})
	strainDiv.enter().append("div").attr("class", "strains").append("h3").text(function(d) { 
		return d.groupName 
	})
	
	var tables = strainDiv.selectAll("table").data(function(d){
		return [d];
	})
	tables.enter().append("table");
	
	var trs = tables.selectAll("tr").data(function(d) {
					// Group the different sets of strains differently
					if (d.groupName.indexOf("Other") == 0) return d.strains.reduce(groupBy(3), []);
					if (d.groupName.indexOf("JJS-MGP") == 0) return d.strains.reduce(groupBy(10), []);
				
					// Group the JJS-DIn strains by runs
					return d.strains.reduce(groupByRuns(10), []) })
    trs.enter().append("tr")
			
    var tds = trs.selectAll("td").data(function(d) { return d });
	tds.enter()
			.append("td")
			.on("click", function(d) { return toggleOd600Inspected(d); })
			.text(function(d) { return d.label })
			.style("color", function(d){
				if(d.data.hasPhenotypes && d.data.hasPredictions){
					return "green";
				}else if(d.data.hasPhenotypes){
					return "blue";
				} else if(d.data.hasPredictions){
					return "red";
				} else {
				  return "black"
				}
			})
	tds.attr("class", function(d){
		var classes = [];
		
		if(isOd600Inspected(d)){
			classes.push("inspected");
		}
		if(!d.data.isKnown){
			classes.push("unknown");
		}
		
		return classes.join(" ");
	})
	
	var legend = od600StrainWithPhenotypesAndPredictionsVisLegend.selectAll("div.legend").data([model.od600StrainsWithPhenotypesAndPredictionsGroups]);
	legend.enter().append("div").attr("class","legend").append("h3").style("font-style","italic").text("Legend");
	var legendList = legend.selectAll("ul").data(function(d){ return [d] }).enter().append("ul");
	legendList.append("li").append("span").text("strain with phenotypes and predictions").style("color","green");
	legendList.append("li").append("span").text("strain with phenotypes only").style("color","blue");
	legendList.append("li").append("span").text("strain with predictions only").style("color","red");
	legendList.append("li").append("span").text("strain with no phenotypes or predictions").style("color","black");	
}

/**
 * The view that shows data sets.
 *
 * @constructor
 */
function DataSetInspectorView() {
	
}

DataSetInspectorView.prototype.updateView = function(duration)
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
	
	inspector.exit().transition()
		.duration(duration)
		.style("opacity", "0")
		.remove();
}

/** Removes all nodes from the view, without affecting the model */
DataSetInspectorView.prototype.removeAll = function(duration) 
{
	var inspector = inspectors.selectAll("div.inspector").data([]);
	inspector.exit().transition()
		.duration(duration)
		.style("opacity", "0")
		.remove();
}

/**
 * The view that shows growth curves
 *
 * @constructor
 */
function Od600InspectorView() {
	
}

Od600InspectorView.prototype.updateView = function(duration)
{	
	var GRAPH_SMALL_WIDTH = 160;
	var GRAPH_SMALL_HEIGHT = 100;
	
	var GRAPH_LARGE_WIDTH = 480;
	var GRAPH_LARGE_HEIGHT = 300;

	// Give duration a default
	duration = duration ? duration : 1000;
	
	var inspector = inspectors.selectAll("div.od600inspector").data(od600Inspected, function (d) { return d.name });
	
	// create
	var inspectorEnter = inspector.enter()
		.append("div")
			.attr("class", "od600inspector")
			
	inspectorEnter
		.append("div")
			.text(function(d) { return d.name })
		.append("span")
			.attr("class", "close")
			.on("click", toggleOd600Inspected)
			.text("x")
			
	inspectorEnter.append("svg:svg")
		.attr("width", function(d){
			d.showSmall = true;
			return GRAPH_SMALL_WIDTH
		})
		.attr("height", GRAPH_SMALL_HEIGHT)
		.on("click", function(d){
			d.showSmall = !d.showSmall;
			od600InspectorView.updateView();
		});

	appendGrowthSection(inspectorEnter);
	appendParentSection(inspectorEnter);
	appendIntervalsSection(inspectorEnter);
	
	appendObjectSection({
		getSectionContainer: function(){
			return inspectorEnter;
		},
		getSectionName: function(){
			return "Predictions";
		},
		getSectionClass: function(){
			return "predictionSection";
		},
		getSectionObjects: function(d){
			return d.data.predictions ? d.data.predictions : [];
		},
		getSectionObjectProperties: function(d){
			return prediction_props_to_pairs(d);
		}
	});

	function graphWidth(d) { return d.showSmall ? GRAPH_SMALL_WIDTH : GRAPH_LARGE_WIDTH }
	function graphHeight(d) { return d.showSmall ? GRAPH_SMALL_HEIGHT : GRAPH_LARGE_HEIGHT }

	// update
	inspector.select("svg").transition().duration(duration)
		.attr("width", graphWidth)
		.attr("height", graphHeight);
	
	var dataDisplay = inspector.select("svg").selectAll("g.curve").data(od600DataForStrain);
	dataDisplay.enter()
		.append("svg:g")
			.attr("class", "curve");
	// Reinitialize the variable
	dataDisplay = inspector.select("svg").selectAll("g.curve").data(od600DataForStrain);

	function x1(d, i) {
		var graph = this.parentNode.parentNode.parentNode.__data__;
		var lines = this.parentNode.__data__;
		return (i / (lines.length)) * graphWidth(graph);
	};

	function x2(d, i) {
		var graph = this.parentNode.parentNode.parentNode.__data__;
		var lines = this.parentNode.__data__;
		return ((i + 1) / (lines.length)) * graphWidth(graph);
	};

	// Draw the curves
	function colorForLine(node) { return this.parentNode.__data__.color; }
	var aCurve = dataDisplay.selectAll("g.lines").data(curveData);
	aCurve.enter().append("svg:g").attr("class", "lines");
		// The first two columns of data are the strain name and human-readable desc
	var line = aCurve.selectAll("line").data(lineData);
	line.enter().append("svg:line")
		.style("stroke-width", "1")
		.style("stroke", colorForLine)
		.attr("x1", x1).attr("y1", "0").attr("x2", x2).attr("y2", "0");

	line.transition().duration(duration)
		.attr("x1", x1)
		.attr("y1", function(d, i) { 
			var graph = this.parentNode.parentNode.parentNode.__data__;
			return graphHeight(graph) - (d[0] * graphHeight(graph));
		})
		.attr("x2", x2)
		.attr("y2", function(d) { 
			var graph = this.parentNode.parentNode.parentNode.__data__;
			return graphHeight(graph) - (d[1] * graphHeight(graph));
		})
		.style("stroke", function(d) {
			var lines = this.parentNode.__data__;
			return lines.color;
		});

	// Draw the scale
	function showScale(node) { return !node.parentNode.parentNode.parentNode.__data__.showSmall };
	function axisY(node) { return graphHeight(this.parentNode.parentNode.parentNode.__data__) } ;
	var scaleg = dataDisplay.selectAll("g.scale").data(scaleData);
	scaleg.enter().append("svg:g").attr("class", "scale");
	scaleg.style("visibility", function(d) { return showScale(this) ? "visible" : "hidden" })
		// The first two columns of data are the strain name and human-readable desc
	var scale = scaleg.selectAll("line").data([[1,0], [0,1]]);
	scale.enter().append("svg:line")
		.style("stroke-width", "1")
		.style("stroke", "black")
		.attr("x1", "0")
		.attr("y1", axisY)
		.attr("x2", "0")
		.attr("y2", axisY);
	scale.transition().duration(duration)
		.attr("y1", axisY)
		.attr("x2", function(d) {
			var graph = this.parentNode.parentNode.parentNode.__data__;
			return d[0] * graphWidth(graph)
		})
		.attr("y2", function(d) {
			var graph = this.parentNode.parentNode.parentNode.__data__;
			return graphHeight(graph) - (d[1] * graphHeight(graph))
		});
	var yscale = scaleg.selectAll("text.y").data([1, 0.5]);
	yscale.enter().append("svg:text")
		.attr("class", "y")
		.attr("x", "0").attr("dx", "5")
		.attr("y", "0").attr("dy", "15");
	yscale
		.attr("y", function(d) {
			var graph = this.parentNode.parentNode.parentNode.__data__;
			return graphHeight(graph) - (d * graphHeight(graph));
		})
		.text(function(d) { return d * Y_MAX});
	var xscale = scaleg.selectAll("text.x").data(function(d, idx) { 
		var numPointsToShow = 5;
		var len = d.timepoints.length;
		var delta = len / numPointsToShow;
		var points = [];
		for (var i = 0; i < numPointsToShow; ++i) {
			var position = Math.floor(i*delta);
			points.push({label : d.timepoints[position], pos : position / len, index : d.index });
		}
		return points;
	});
	xscale.enter().append("svg:text")
		.style("fill", colorForLine)		
		.attr("class", "x")
		.attr("x", "0").attr("dx", "5")
		.attr("y", axisY)
		.attr("dy", function(d) { return -1 * (3 + 10 * d.index)});

	xscale
		.attr("x", function(d) { 
			var graph = this.parentNode.parentNode.parentNode.__data__;
			return d.pos * graphWidth(graph);
		})
		.attr("y", axisY)	
		.text(function(d) { return d.label});
	
	// remove
	inspector.exit().transition()
		.duration(duration)
		.style("opacity", "0")
		.remove();
}

function appendParentSection(inspectorEnter) {
	var parentSection = inspectorEnter.append("div");
	var parents = parentSection.selectAll("div.parent")
		.data(function(d) { return [d.data.parent]; });
	parents.enter().append("div")
		.attr("class", "parent")
		.text("Parent: ");
	parents.selectAll("span")
		.data(function(d) { return [d] })
	.enter().append("span")
		.text(function (d) { return (d) ? d : "None" });
}

function appendIntervalsSection(inspectorEnter) {
	var intervalsSection = inspectorEnter.append("div");
	var intervals = intervalsSection.selectAll("div.intervals")
		.data(function(d) {	return [d.data.intervals]; });
	intervals.enter().append("div")
		.attr("class", "intervals")
		.text("Intervals: ");
	intervals.selectAll("span")
		.data(function(d) { return d; })
	.enter().append("span")
		.text(function (d) { return d + " " });
}

function appendGrowthSection(inspectorEnter) {
	var growthMediaSection = inspectorEnter.append("div");
	var growsSection = growthMediaSection.selectAll("div.growth")
		.data(function(d) { var growsOn = [];
			model.allGrowthMedia.forEach(function(m) { 
				var value = d.data.phenotypeMap[m]
				if (value && value > 0)	growsOn.push(m);
			});
			return growsOn.length > 0 ? [growsOn] : [];
		});
	growsSection.enter().append("div")
		.attr("class", "growth")
		.text("Grows on: ");
	growsSection.selectAll("span")
		.data(function(d) { return d })
	.enter().append("span")
		.text(function (d) { return d + " "});

	var doesNotGrowSection = growthMediaSection.selectAll("div.nogrow")
		.data(function(d) { var doesNotGrowOn = [];
			model.allGrowthMedia.forEach(function(m) { 
				var value = d.data.phenotypeMap[m]
				if (value && value < 0.01)	doesNotGrowOn.push(m);
			});
			return doesNotGrowOn.length > 0 ? [doesNotGrowOn] : [];
		});
	doesNotGrowSection.enter().append("div")
		.attr("class", "nogrow")
		.text("No growth on: ");
	doesNotGrowSection.selectAll("span")
		.data(function(d) { return d })	
	.enter().append("span")
		.text(function (d) { return d + " "});


	var unknownGrowthSection = growthMediaSection.selectAll("div.unknown")
		.data(function(d) { var unknownGrowOn = [];
			model.commonGrowthMedia.forEach(function(m) { 
				var value = d.data.phenotypeMap[m]
				if (!value)	unknownGrowOn.push(m);
			});
			return unknownGrowOn.length > 0 ? [unknownGrowOn] : [];
		});
	unknownGrowthSection.enter().append("div")
		.attr("class", "unknown")
		.text("Unknown growth on: ");

	unknownGrowthSection.selectAll("span")
		.data(function(d) { return d})
	.enter().append("span")
		.text(function (d) { return d + " "});
}

function appendObjectSection(config){
	
	var section = config.getSectionContainer().append("div")
	.attr("class", config.getSectionClass())
	.style("display", function(d){ 
		var objects = config.getSectionObjects(d);
		if(objects && objects.length > 0){
			return "block";
		}else{
			return "none";
		}
	});
	
	var header = section.append("h3")
		.text(function(d){
			var name = config.getSectionName();
			var objects = config.getSectionObjects(d);
			return name + " (" + objects.length + ")";
		});
	
	var toggle = header.append("a")
		.text("show");
	
	var list = section.append("ul")
		.style("display","none");
	
	var items = list.selectAll("li")
		.data(function(d){ return config.getSectionObjects(d) });
	
	var item = items.enter()
		.append("li").append("table");
	
	item.selectAll("tr").data(function(d){ return config.getSectionObjectProperties(d) })
		.enter()
			.append("tr")
			.selectAll("td").data(function(d) { return d } ).enter()
				.append("td")
				.attr("class", function(d, i) { return (i == 0) ? "propkey" : "propvalue"})
				.style("opacity", "0")
				.text(function(d) { return d })
			.transition()
				.style("opacity", "1");
	
	toggle.on("click", function(){
		if(list.style("display") == "none"){
			list.style("display","block");
			toggle.text("hide");
		}else{
			list.style("display", "none");
			toggle.text("show");
		}
	});

}

/** Removes all nodes from the view, without affecting the model */
Od600InspectorView.prototype.removeAll = function(duration) 
{
	var inspector = inspectors.selectAll("div.od600inspector").data([]);
	inspector.exit().transition()
		.duration(duration)
		.style("opacity", "0")
		.remove();
}

function od600DataForStrain(d) {
	if (null == d.dataSets) return [];
	
	var dataSets = d.dataSets.filter(function(ds) { return isOd600DataSet(ds) });
	var idx = -1;
	var data = [];
	dataSets.map(function(ds) { 
		if (null == ds.od600Map) return {};
		var strainData = ds.od600Map[d.name];
		if (null == strainData) return {};
		strainData.map(function(curve) {
			if (curve) { 
				idx = idx + 1;
				data.push({ strain: d, index: idx, values: curve, timepoints : ds.od600Timepoints});
			}
		});
	})
	return data;
}

function od600DataProperties(d){
	return d;
}

var Y_MAX = 2;

function curveData(d, i)
{
	var color = curveColors(d.index);	
	// Don't normalize -- use 2 as the global max value
	// var maxValue = (0 == d.values.length) ? 0 : d3.max(d.values);
	return [{length : d.values.length, max : Y_MAX, values: d.values, color : color, timepoints : d.timepoints}]
}

function scaleData(d, i)
{
	var color = curveColors(d.index);	
	// Don't normalize -- use 2 as the global max value
	// var maxValue = (0 == d.values.length) ? 0 : d3.max(d.values);
	return [{length : d.timepoints.length, index : d.index, color : color, timepoints : d.timepoints}]
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

function dataSetLabel(d) { return d.bis.dataSetTypeCode + " registered on " + timeformat(new Date(d.bis.registrationDetails.registrationDate));  }

function downloadTableFile(d) { presenter.downloadTableFile(d) }

function filesForDataSet(d)
{
	if (d.loadingFiles) return [{ pathInListing : "Loading..." }];
	
	var fileFilter = function(file) {
		if (file.isDirectory) return false;
		return true;
/*		
		if (!file.isDirectory) {
			if (endsWith(file.pathInDataSet, "xls")) {
				return true;
			}
			if (endsWith(file.pathInDataSet, "xls.tsv")) {
				return true;
			}
		}
		return false;
*/
	};
	
	return (d.files) ? d.files.filter(fileFilter) : [];
}


var model = new AppModel();
var presenter = new AppPresenter();

// The data set type visualization
var dataSetTypeVis, od600View, metabolomicsView, transcriptomicsView, proteomicsView, sequencesCm5View, sequencesGenbankView, intervalAnnotationsView, metabolicModelView;

// The strain visualization
var strainVis, strainView;

// The OD600 strain visualization
var od600StrainVis, od600StrainView;

var IGNORED_DATASET_TYPES = [ "EXCEL_ORIGINAL", "TSV_EXPORT", "TSV_MULTISTRAIN_EXPORT", "UNKNOWN" ];

//The inspected strains and data sets
var inspected = [];
var od600Inspected = [];

//The node inspectors
var inspectors, dataSetInspectorView, od600InspectorView;


function isOd600DataSet(d) { return "OD600" == d.bis.dataSetTypeCode }

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
	dataSetStrains = dataSetStrains.map(function(strain){
		return { name : strain };
	});
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

function prediction_props_to_pairs(d){
	return object_props_to_pairs(d);
}

function phenotype_props_to_pairs(d){
	return object_props_to_pairs(d);
}

function object_props_to_pairs(d){
	var pairs = [];

	for (var prop in d) {
		var propValue = d[prop];
		if (propValue) {
			var pair = [prop, propValue];
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

/**
 * Return true if str ends with suffix.
 */
function endsWith(str, suffix)
{
    var lastIndex = str.lastIndexOf(suffix);
    return (lastIndex != -1) && (lastIndex + suffix.length == str.length);
}

/**
 * A sort function that uses properties.
 *
 * @param propName a name of property
 * @returns {Function} a function that can be passed to sort
 */
function sortByProp(propName) 
{
	var sortFunction = function(a, b) { if (a[propName] == b[propName]) return 0; return (a[propName] < b[propName]) ? -1 : 1};
	return sortFunction;
}


/**
 * @param arr an array
 * @returns the unique elements of an array
 */
function uniqueElements(arr) 
{
	var reduceFunc = function(list, elt) {
		var size = list.length;
		if (size == 0 || list[size - 1] != elt) 
		{ 
			list.push(elt); 
		}
		return list;
	};
	return arr.reduce(reduceFunc, []);
}

/**
 * A function that groups a collection into collections of length numElts.
 * 
 * @param numElts Number of elements
 * @returns {Function} a function that can be passed to reduce
 */
function groupBy(numElts)
{
	var groupBy = function(groups, elt) {
		if (groups.length < 1) {
			groups.push([elt]);
			return groups;
		}
		
		var lastGrp = groups[groups.length - 1];
		if (lastGrp.length < numElts) {
			lastGrp.push(elt);
		} else {
			groups.push([elt]);
		}
	
		return groups;
	}
	return groupBy;
}

/**
 * Check if the current elt number = last elt number + 1
 */
function isRun(lastElt, currentElt) {
	var lastNumber = Number(lastElt.label);
	var currentNumber = Number(currentElt.label);
	// Assume that non numeric values are runs
	if (lastNumber == NaN || currentNumber == NaN) return true;
	
	return currentNumber == (lastNumber + 1);
}

/**
 * A function that groups a collection into sequential runs of length maxNumEltsPerGroup
 * 
 * @param maxNumEltsPerGroup Number of elements
 * @returns {Function} a function that can be passed to reduce
 */
function groupByRuns(maxNumEltsPerGroup)
{
  var lastSeen = "";
	var groupBy = function(groups, elt) {
		// Initialize the groups
		if (groups.length < 1) { groups.push([elt]); return groups; }

		// Check if we should append to the last group or create a new one
		var lastGrp = groups[groups.length - 1];
		var createNewGroup = false;
		if (lastGrp.length >= maxNumEltsPerGroup) {
			// We've reached the size limit of the group
			createNewGroup = true;
		} else {
			// See if this is a run, if not create a new group
			createNewGroup = !isRun(lastGrp[lastGrp.length - 1], elt);
		}

		(createNewGroup) ? groups.push([elt]) : lastGrp.push(elt);
	
		return groups;
	}
	return groupBy;
}