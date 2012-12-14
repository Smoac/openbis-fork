/*!
 * OpenBIS API
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
	settings.type = "POST";
	settings.processData = false;
	settings.dataType = "json";
	settings.crossDomain = true;
	settings.data = jsonRequestData(settings.data);
	$.ajax(settings);
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

function openbis(url, dssUrl) {
	this.generalInfoServiceUrl = url + "/rmi-general-information-v1.json"
	this.queryServiceUrl = url + "/rmi-query-v1.json"
	this.dssUrl = dssUrl + "/rmi-dss-api-v1.json"
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
				openbisObj.rememberSession();
				action(data)
			},
		error: function(request, status, thrown) {
		  alert("Login failed : " + status)
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

openbis.prototype.isSessionActive = function(action) {	
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "isSessionActive",
				"params" : [ this.sessionToken ] 
			  },
		success: action
	 });
}

openbis.prototype.ifRestoredSessionActive = function(action) {	
	this.restoreSession();
	this.isSessionActive(function(data) { if (data.result) action(data) });
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
 
openbis.prototype.searchForSamples = function(searchCriteria, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForSamples",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	 });
}

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

openbis.prototype.searchForDataSets = function(searchCriteria, action) {
	 ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "searchForDataSets",
				"params" : [ this.sessionToken,
							 searchCriteria ] },
		success: action
	 });
}

 openbis.prototype.listFilesForDataSet = function(dataSetCode, path, recursive, action) {
     ajaxRequest({
        url: this.dssUrl,
        data: { "method" : "listFilesForDataSet",
                "params" : [ this.sessionToken, dataSetCode, path, recursive ]
               },
        success: action
     });
}

openbis.prototype.getDownloadUrlForFileForDataSet = function(dataSetCode, filePath, action) {
    ajaxRequest({
        url: this.dssUrl,
        data: { "method" : "getDownloadUrlForFileForDataSet",
                "params" : [ this.sessionToken, dataSetCode, filePath ]
               },
        success: action
    });
}

openbis.prototype.listDataSetsForSample = function(sample, restrictToDirectlyConnected, action) {
	ajaxRequest({
		url: this.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForSample",
				"params" : [ this.sessionToken, sample, restrictToDirectlyConnected ] 
		},
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

getQueryParameter = function(parameterName){
	var match = location.search.match(RegExp("[?|&]"+parameterName+'=(.+?)(&|$)'));
	if(match && match[1]){
		return decodeURIComponent(match[1].replace(/\+/g,' '));
	}else{
		return null;
	}
}
