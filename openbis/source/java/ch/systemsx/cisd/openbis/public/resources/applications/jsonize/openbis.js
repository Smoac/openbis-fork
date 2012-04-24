/*!
 * OpenBIS API (jsonize)
 *
 * An API for accessing openBIS. Depends on jQuery.
 */
 
var jsonRequestData = function(params) {
	// KE: generate unique ids ? Hardcoded "id" seems to work too for now
	params["id"] = "1"
	params["jsonrpc"] = "2.0"
	return JSON.stringify(params)
}
 
var ajaxRequest = function(settings) {
	settings.type = "POST"
	settings.processData = false
	settings.dataType = "json"
	settings.data = jsonRequestData(settings.data)
	$.ajax(settings)
}

/* 
 * Functions for working with cookies.
 *
 * These are from http://www.quirksmode.org/js/cookies.html
 */
function createCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function readCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name) {
	createCookie(name,"",-1);
}

function openbis(openbisHost, openbisContext, dssHost) {
	this.openbisHost = openbisHost;
	this.openbisContext = openbisContext;
	
	// these services always use 'openbis' context
	this.generalInfoServiceUrl = openbisHost + "/openbis/openbis/rmi-general-information-v1.json";
	this.webInfoServiceUrl = openbisHost + "/openbis/openbis/rmi-web-information-v1.json";
	this.queryServiceUrl = openbisHost + "/openbis/openbis/rmi-query-v1.json";
	this.screeningServiceUrl = openbisHost + "/openbis/openbis/rmi-screening-api-v1.json";
	this.dssScreeningServiceUrl = dssHost + "/rmi-datastore-server-screening-api-v1.json";
}

openbis.prototype.login = function(username, password, action) {
	openbisObj = this
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "tryToAuthenticateForAllServices",
				"params" : [ username, password ] 
			  },
		success: 
			function(data) {
			   openbisObj.sessionToken = data.result;
			   action(data)
			},
		error: function() {
		  alert("Login failed")
		}
	 });
}

/*
 * Screening API methods
 */

openbis.prototype.screening_tryLoginScreening = function(userId, userPassword, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "tryLoginScreening",
				"params" : [ userId, userPassword ] 
			  },
		success:  function(data){
			openbisObj.sessionToken = data.result;
			action(data);
		}
	 });
}

openbis.prototype.screening_logoutScreening = function(action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "logoutScreening",
				"params" : [ this.sessionToken ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listPlates = function(action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listPlates",
				"params" : [ this.sessionToken ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listPlatesForExperiment = function(experiment, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listPlates",
				"params" : [ this.sessionToken, experiment ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_getPlateMetadataList = function(plates, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "getPlateMetadataList",
				"params" : [ this.sessionToken, plates ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listExperiments = function(action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.sessionToken ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listExperimentsForUser = function(userId, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.sessionToken, userId ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listFeatureVectorDatasets = function(plates, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listFeatureVectorDatasets",
				"params" : [ this.sessionToken, plates ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listImageDatasets = function(plates, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listImageDatasets",
				"params" : [ this.sessionToken, plates ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listRawImageDatasets = function(plates, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listRawImageDatasets",
				"params" : [ this.sessionToken, plates ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listSegmentationImageDatasets = function(plates, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listSegmentationImageDatasets",
				"params" : [ this.sessionToken, plates ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_getDatasetIdentifiers = function(datasetCodes, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "getDatasetIdentifiers",
				"params" : [ this.sessionToken, datasetCodes ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listPlateWellsForExperimentAndMaterial = function(experimentIdentifier, materialIdentifier, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listPlateWells",
				"params" : [ this.sessionToken, experimentIdentifier, materialIdentifier, true ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listPlateWellsForMaterial = function(materialIdentifier, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listPlateWells",
				"params" : [ this.sessionToken, materialIdentifier, true ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listPlateWellsForPlate = function(plateIdentifier, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listPlateWells",
				"params" : [ this.sessionToken, plateIdentifier ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_getWellSample = function(wellIdentifier, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "getWellSample",
				"params" : [ this.sessionToken, wellIdentifier ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_getPlateSample = function(plateIdentifier, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "getPlateSample",
				"params" : [ this.sessionToken, plateIdentifier ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_listPlateMaterialMapping = function(plates, materialTypeIdentifierOrNull, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "listPlateMaterialMapping",
				"params" : [ this.sessionToken, plates, materialTypeIdentifierOrNull ] 
			  },
		success:  action
	 });
}

openbis.prototype.screening_getExperimentImageMetadata = function(experimentIdentifier, action) {
	openbisObj = this
	ajaxRequest({
		url: this.screeningServiceUrl,
		data: { "method" : "getExperimentImageMetadata",
				"params" : [ this.sessionToken, experimentIdentifier ] 
			  },
		success:  action
	 });
}

/*
 * DSS Screening API methods
 */

openbis.prototype.dss_listAvailableFeatureNames = function(featureDatasetIdentifiers, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listAvailableFeatureNames",
				"params" : [ this.sessionToken, featureDatasetIdentifiers ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listAvailableFeatureCodes = function(featureDatasetIdentifiers, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listAvailableFeatureCodes",
				"params" : [ this.sessionToken, featureDatasetIdentifiers ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listAvailableFeatures = function(featureDatasetIdentifiers, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listAvailableFeatures",
				"params" : [ this.sessionToken, featureDatasetIdentifiers ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_loadFeatures = function(featureDatasetReferences, featureCodes, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "loadFeatures",
				"params" : [ this.sessionToken, featureDatasetReferences, featureCodes ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_loadFeaturesForDatasetWellReferences = function(datasetWellReferences, featureCodes, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "loadFeaturesForDatasetWellReferences",
				"params" : [ this.sessionToken, datasetWellReferences, featureCodes ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listPlateImageReferencesForChannel = function(datasetIdentifier, wellPositions, channel, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listPlateImageReferences",
				"params" : [ this.sessionToken, datasetIdentifier, wellPositions, channel ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listPlateImageReferencesForChannels = function(datasetIdentifier, wellPositions, channels, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listPlateImageReferences",
				"params" : [ this.sessionToken, datasetIdentifier, wellPositions, channels ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listImageReferencesForChannel = function(datasetIdentifier, channel, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listImageReferences",
				"params" : [ this.sessionToken, datasetIdentifier, channel ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listImageReferencesForChannels = function(datasetIdentifier, channels, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listImageReferences",
				"params" : [ this.sessionToken, datasetIdentifier, channels ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listImageMetadata = function(datasetIdentifiers, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listImageMetadata",
				"params" : [ this.sessionToken, datasetIdentifiers ] 
			  },
		success:  action
	 });
}

openbis.prototype.dss_listAvailableImageRepresentationFormats = function(datasetIdentifiers, action) {
	openbisObj = this
	ajaxRequest({
		url: this.dssScreeningServiceUrl,
		data: { "method" : "listAvailableImageRepresentationFormats",
				"params" : [ this.sessionToken, datasetIdentifiers ] 
			  },
		success:  action
	 });
}

openbis.prototype.logout = function(action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "logout",
				"params" : [ this.sessionToken ] 
			  },
		success: action
	 });
}

openbis.prototype.getSessionToken = function(action){
	ajaxRequest({
		url: this.webInfoServiceUrl,
		data: { "method" : "getSessionToken" },
		success: action
	 });
}

openbis.prototype.listSpacesWithProjectsAndRoleAssignments = function(databaseInstanceCodeOrNull, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listSpacesWithProjectsAndRoleAssignments",
				"params" : [ this.sessionToken,  databaseInstanceCodeOrNull ] 
			  },
		success: action
	 });
}

openbis.prototype.listProjects = function(action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listProjects",
				"params" : [ this.sessionToken ] 
			  },
		success: action
	 });
}

openbis.prototype.listExperiments = function(projects, experimentType, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.sessionToken, projects, experimentType ] 
			  },
		success: action
	 });
}

openbis.prototype.listSamplesForExperiment = function(experimentIdentifier, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listSamplesForExperiment",
				"params" : [ this.sessionToken, experimentIdentifier ] 
			  },
		success: action
	 });
}

openbis.prototype.searchForSamples = function(searchCriteria, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForSamples",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	 });
}

openbis.prototype.searchForSamples = function(searchCriteria, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForSamples",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	 });
}

openbis.prototype.searchForDataSets = function(searchCriteria, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForDataSets",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	 });
}

openbis.prototype.listQueries = function(action) {
	 ajaxRequest({
		url: this.queryServiceUrl,
		data: { "method" : "listQueries",
				"params" : [ this.sessionToken ] },
		success: action
	 });
}

openbis.prototype.executeQuery = function(queryId, parameterBindings, action) {
	 ajaxRequest({
		url: this.queryServiceUrl,
		data: { "method" : "executeQuery",
				"params" : [ this.sessionToken, queryId, parameterBindings ] },
		success: action
	 });
}
