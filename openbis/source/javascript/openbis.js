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
 
var openbis = {}
 
openbis.login = function(username, password, action) {
	ajaxRequest({
		url: openbis.url,
		data: { "method" : "tryToAuthenticateForAllServices",
				"params" : [ username, password ] 
			  },
		success: 
			function(data) {
			   openbis.sessionToken = data.result;
			   action(data)
			},
		error: function() {
		  alert("Login failed")
		}
	 });
}

openbis.logout = function(action) {
	ajaxRequest({
		url: openbis.url,
		data: { "method" : "logout",
				"params" : [ openbis.sessionToken ] 
			  },
		success: action
	 });
}

openbis.listProjects = function(action) {
	 ajaxRequest({
		url: openbis.url,
		data: { "method" : "listProjects",
				"params" : [ openbis.sessionToken ] 
			  },
		success: action
	 });
}

openbis.listExperiments = function(projects, experimentType, action) {
	 ajaxRequest({
		url: openbis.url,
		data: { "method" : "listExperiments",
				"params" : [ openbis.sessionToken, projects, experimentType ] 
			  },
		success: action
	 });
}
 
openbis.searchForSamples = function(searchCriteria, action) {
	 ajaxRequest({
		url: openbis.url,
		data: { "method" : "searchForSamples",
				"params" : [ openbis.sessionToken,
							 searchCriteria ] },
		success: action
	 });
}
