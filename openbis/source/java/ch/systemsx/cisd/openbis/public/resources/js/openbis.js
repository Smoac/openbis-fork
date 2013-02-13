// Comments follow the yuidoc convenions: http://developer.yahoo.com/yui/yuidoc/
/**
 * The openBIS module provides objects for communicating with openBIS.
 *
 * @module openbis
 * @requires jquery
 */
 
var jsonRequestData = function(params) {
	// KE: generate unique ids ? Hardcoded "id" seems to work too for now
	params["id"] = "1";
	params["jsonrpc"] = "2.0";
	return JSON.stringify(params)
}
 
var ajaxRequest = function(settings) {
	settings.type = "POST";
	settings.processData = false;
	settings.dataType = "json";
	settings.data = jsonRequestData(settings.data);
	$.ajax(settings)
}

// Functions for working with cookies.
//
// These are from http://www.quirksmode.org/js/cookies.html
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

/**
 * A lightweight facade for interacting with openBIS. It provides access
 * to the following openBIS APIs:
 * 
 * 	ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService
 * 	ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer
 * 	ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric
 *
 * The Javascript API is not yet exhaustive in its coverage of the above APIs;
 * there are methods in the API that do not have Javascript equivelants. 
 *
 * 
 * @class
 */
function openbis(openbisUrl, dssUrl) {
	this.openbisUrl = openbisUrl;
	this.dssUrl = dssUrl;
	
	this.generalInfoServiceUrl = openbisUrl + "/rmi-general-information-v1.json";
	this.generalInfoChangingServiceUrl = openbisUrl + "/rmi-general-information-changing-v1.json";
	this.queryServiceUrl = openbisUrl + "/rmi-query-v1.json";
	this.dssApiUrl = dssUrl + "/rmi-dss-api-v1.json";
	this.webInfoServiceUrl = openbisUrl + "/rmi-web-information-v1.json"
    this.screeningUrl = openbisUrl + "/rmi-screening-api-v1.json"
	this.dssScreeningUrl = dssUrl + "/rmi-datastore-server-screening-api-v1.json"
}


/**
 * Log into openBIS.
 *
 * @method
 */
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
				openbisObj.rememberSession();
				action(data)
			},
		error: function() {
			alert("Login failed")
		}
	 });
}

openbis.prototype.rememberSession = function() {
	// Store the result in a cookie, so the user doesn't need to log in every time.
	createCookie('openbis', this.sessionToken, 1);
}

openbis.prototype.restoreSession = function() {
	this.sessionToken = readCookie('openbis');
}

openbis.prototype.useSession = function(sessionToken){
	this.sessionToken = sessionToken;
}

openbis.prototype.isSessionActive = function(action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "isSessionActive",
				"params" : [ this.sessionToken ] 
				},
		success: action
	 });
}

/**
 * Restore the session from a cookie and check that it is still valid.
 * 
 * @method
 */
openbis.prototype.ifRestoredSessionActive = function(action) {
	this.restoreSession();
	this.isSessionActive(function(data) { if (data.result) action(data) });
}

openbis.prototype.getSessionTokenFromServer = function(action) {
	ajaxRequest({
		url: this.webInfoServiceUrl,
		data: { "method" : "getSessionToken" },
		success: action
	 });
}

/**
 * Log out of openBIS
 * 
 * @method
 */
openbis.prototype.logout = function(action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "logout",
				"params" : [ this.sessionToken ] 
			  },
		success: action
	 });
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.listSpacesWithProjectsAndRoleAssignments(String, String)
 * 
 * @method
 */
openbis.prototype.listSpacesWithProjectsAndRoleAssignments = function(databaseInstanceCodeOrNull, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listSpacesWithProjectsAndRoleAssignments",
				"params" : [ this.sessionToken,  databaseInstanceCodeOrNull ] 
			  },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.listProjects(String)
 * 
 * @method
 */
openbis.prototype.listProjects = function(action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listProjects",
				"params" : [ this.sessionToken ] 
			  },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.listExperiments(String, List<Project>, String)
 * 
 * @method
 */
openbis.prototype.listExperiments = function(projects, experimentType, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.sessionToken, projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.listSamplesForExperiment(String, String)
 * 
 * @method
 */
openbis.prototype.listSamplesForExperiment = function(experimentIdentifier, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listSamplesForExperiment",
				"params" : [ this.sessionToken, experimentIdentifier ] 
		},
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.searchForSamples(String, SearchCriteria)
 * 
 * @method
 */
openbis.prototype.searchForSamples = function(searchCriteria, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForSamples",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.searchForSamples(String, SearchCriteria, EnumSet<SampleFetchOption>)
 * 
 * @method
 */
openbis.prototype.searchForSamplesWithFetchOptions = function(searchCriteria, fetchOptions, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { 
				"method" : "searchForSamples",
				"params" : [ 
					this.sessionToken,
					searchCriteria,
					fetchOptions ] 
		},
		success: action
	 });
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.searchForDataSets(String, SearchCriteria)
 * 
 * @method
 */
openbis.prototype.searchForDataSets = function(searchCriteria, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForDataSets",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService.listDataSetsForSample(String, Sample, boolean)
 * 
 * @method
 */
openbis.prototype.listDataSetsForSample = function(sample, restrictToDirectlyConnected, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForSample",
				"params" : [ this.sessionToken, sample, restrictToDirectlyConnected ] 
		},
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric.listFilesForDataSet(String, String, String, boolean)
 * 
 * @method
 */
openbis.prototype.listFilesForDataSet = function(dataSetCode, path, recursive, action) {
	ajaxRequest({
			url: this.dssApiUrl,
			data: { "method" : "listFilesForDataSet",
							"params" : [ this.sessionToken, dataSetCode, path, recursive ]
						 },
			success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric.getDownloadUrlForFileForDataSet(String, String, String)
 * 
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSet = function(dataSetCode, filePath, action) {
	ajaxRequest({
			url: this.dssApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSet",
							"params" : [ this.sessionToken, dataSetCode, filePath ]
						 },
			success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer.listQueries(String)
 * 
 * @method
 */
openbis.prototype.listQueries = function(action) {
	ajaxRequest({
		url: this.queryServiceUrl,
		data: { "method" : "listQueries",
				"params" : [ this.sessionToken ] },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer.executeQuery(String, long, Map<String, String>)
 * 
 * @method
 */
openbis.prototype.executeQuery = function(queryId, parameterBindings, action) {
	ajaxRequest({
		url: this.queryServiceUrl,
		data: { "method" : "executeQuery",
				"params" : [ this.sessionToken, queryId, parameterBindings ] },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer.createReportFromAggregationService(String, String, String, Map<String, Object>)
 */
openbis.prototype.createReportFromAggregationService = function(dataStoreCode, serviceKey, parameters, action) {
	ajaxRequest({
		url: this.queryServiceUrl,
		data: { "method" : "createReportFromAggregationService",
		params : [ this.sessionToken, dataStoreCode, serviceKey, parameters ] },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService.getWebAppSettings(String, String)
 */
openbis.prototype.getWebAppSettings = function(webappId, action) {
	ajaxRequest({
		url: this.generalInfoChangingServiceUrl,
		data: { "method" : "getWebAppSettings",
		params : [ this.sessionToken, webappId ] },
		success: action
	});
}

/**
 * See ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService.setWebAppSettings(String, WebAppSettings)
 */
openbis.prototype.setWebAppSettings = function(webappSettings, action) {
	ajaxRequest({
		url: this.generalInfoChangingServiceUrl,
		data: { "method" : "setWebAppSettings",
		params : [ this.sessionToken, webappSettings ] },
		success: action
	});
}

/**
 * Creates a session workspace file uploader inside the specified uploaderContainer element.
 * 
 * @method
 */
openbis.prototype.createSessionWorkspaceUploader = function(uploaderContainer){
	var uploaderSupported = window.File && window.FileReader && window.XMLHttpRequest;

	if(!uploaderSupported){
		alert("Uploader is not supported by your browser.");
		return;
	}
	
	var $this = this;
	
	// figure out what is the location of the openbis.js script and assume that uploader resources are served by the same server
	var openbisScriptLocation = $('script[src*=openbis\\.js]').attr('src');
	var uploaderDirectoryLocation = jsFileLocation = openbisScriptLocation.replace(/js\/openbis\.js/g, 'uploader');
	
	$('head').append('<link rel="stylesheet" media="screen" type="text/css" href="' + uploaderDirectoryLocation + '/css/src/upload.css" />');
	$('head').append('<script charset="utf-8" type="text/javascript" src="' + uploaderDirectoryLocation + '/js/src/upload.js" />');
	
	$(uploaderContainer).load(uploaderDirectoryLocation + "/index.html", function(){
		Uploader.init({
		       smart_mode: true,
		       chunk_size: 1000*1024,
		       file_upload_url: $this.dssUrl + "/session_workspace_file_upload",
		       form_upload_url: $this.dssUrl + "/session_workspace_form_upload",
		       file_download_url: $this.dssUrl + "/session_workspace_file_download",
		       sessionID: $this.sessionToken
		});
	});	
}

/**
 * Creates a session workspace download url for a file with the specified filePath.
 * 
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadUrl = function(filePath){
	return this.dssUrl + "/session_workspace_file_download?sessionID=" + this.sessionToken + "&filePath=" + filePath; 
}

/**
 * Create a session workspace download link for a file with the specified filePath.
 * 
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadLink = function(filePath, linkText){
	return $("<a href='" + this.createSessionWorkspaceDownloadUrl(filePath) + "'>" + (linkText ? linkText : filePath) + "</a>"); 
}

/**
 * Downloads a session workspace file with the specified filePath.
 * 
 * @method
 */
openbis.prototype.downloadSessionWorkspaceFile = function(filePath, action) {
	$.ajax({
		type: "GET",
		url: this.createSessionWorkspaceDownloadUrl(filePath),
		success: action
	});
}

/**
 * Deletes a session workspace file with the specified filePath.
 * 
 * @method
 */
openbis.prototype.deleteSessionWorkspaceFile = function(filePath, action) {
	ajaxRequest({
			url: this.dssApiUrl,
			data: { "method" : "deleteSessionWorkspaceFile",
							"params" : [ this.sessionToken, filePath ]
						 },
			success: action
	});
}

/**
 * A utility class for deferring an action until all of some kind of action has completed
 *
 * @argument dependencies An array of the keys for the dependencies.
 */
function actionDeferrer(pendingAction, dependencies) {
	this.pendingAction = pendingAction;
	this.dependencies = {};
	var newme = this;
	dependencies.forEach(function(key) {
		newme.dependencies[key] = false;
	});
}

/**
 * Note that a dependency completed. Execute the pending action if appropriate.
 */
actionDeferrer.prototype.dependencyCompleted = function(key) {
	this.dependencies[key] = true;
	var shouldExecute = true;
	for (prop in this.dependencies) {
		if (false == this.dependencies[prop]) {
			shouldExecute = false;
			break;
		}
	}
	if (shouldExecute) {
		this.pendingAction();
	}
}


/**
 * See ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening.loadImagesBase64(String sessionToken, List<PlateImageReference> imageReferences, boolean convertToPng)
 * 
 * @method
 */
openbis.prototype.loadImagesBase64 = function(imageReferences, convertToPng, action) {
	ajaxRequest({
		url: this.dssScreeningUrl,
		data: { "method" : "loadImagesBase64",
				"params" : [ this.sessionToken, imageReferences, convertToPng ] },
		success: action
	});
}

openbis.prototype.listPlates = function(action) {
    ajaxRequest({
            url: this.screeningUrl,
            data: { "method" : "listPlates",
                    "params" : [ this.sessionToken ] },
            success: action
    });
}

openbis.prototype.listRawImageDatasets = function(plates, action) {
    ajaxRequest({
            url: this.screeningUrl,
            data: { "method" : "listRawImageDatasets",
                    "params" : [ this.sessionToken, plates ] },
            success: action
    });
}

openbis.prototype.listPlateImageReferences = function(dataSetIdentifier, wellPositions, channel, action) {
    ajaxRequest({
            url: this.dssScreeningUrl,
            data: { "method" : "listPlateImageReferences",
                    "params" : [ this.sessionToken, dataSetIdentifier, wellPositions, channel ] },
            success: action
    });
}


/**
 * Provides a context information for webapps that are embedded inside the OpenBIS UI.
 */
function openbisWebAppContext(){
	this.webappCode = this.getParameter("webapp-code");
	this.sessionId = this.getParameter("session-id");
	this.entityKind = this.getParameter("entity-kind");
	this.entityType = this.getParameter("entity-type");
	this.entityIdentifier = this.getParameter("entity-identifier");
	this.entityPermId = this.getParameter("entity-perm-id");
}

openbisWebAppContext.prototype.getWebappCode = function(){
	return this.webappCode;
}

openbisWebAppContext.prototype.getSessionId = function(){
	return this.sessionId;
}

openbisWebAppContext.prototype.getEntityKind = function(){
	return this.entityKind;
}

openbisWebAppContext.prototype.getEntityType = function(){
	return this.entityType;
}

openbisWebAppContext.prototype.getEntityIdentifier = function(){
	return this.entityIdentifier;
}

openbisWebAppContext.prototype.getEntityPermId = function(){
	return this.entityPermId;
}

openbisWebAppContext.prototype.getParameter = function(parameterName){
	var match = location.search.match(RegExp("[?|&]"+parameterName+'=(.+?)(&|$)'));
	if(match && match[1]){
		return decodeURIComponent(match[1].replace(/\+/g,' '));
	}else{
		return null;
	}
}
