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
	settings.type = "POST"
	settings.processData = false
	settings.dataType = "json"
	settings.data = jsonRequestData(settings.data)
	$.ajax(settings)
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
			   action(data)
			},
		error: function() {
		  alert("Login failed")
		}
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
