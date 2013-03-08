/**
 * =============================================
 * OpenBIS facade internal code (DO NOT USE!!!)
 * =============================================
 */

if(typeof $ == 'undefined'){
	alert('Loading of openbis.js failed - jquery.js is missing');
}

function _openbisInternal(openbisUrl){
	this.init(openbisUrl);
}

_openbisInternal.prototype.init = function(openbisUrl){
	this.openbisUrl = openbisUrl;
	this.generalInfoServiceUrl = openbisUrl + "/rmi-general-information-v1.json";
	this.generalInfoChangingServiceUrl = openbisUrl + "/rmi-general-information-changing-v1.json";
	this.queryServiceUrl = openbisUrl + "/rmi-query-v1.json";
	this.webInfoServiceUrl = openbisUrl + "/rmi-web-information-v1.json"
}

_openbisInternal.prototype.log = function(msg){
	if(console){
		console.log(msg);
	}
}

_openbisInternal.prototype.jsonRequestData = function(params) {
	params["id"] = "1";
	params["jsonrpc"] = "2.0";
	return JSON.stringify(params)
}
 
_openbisInternal.prototype.ajaxRequest = function(settings) {
	settings.type = "POST";
	settings.processData = false;
	settings.dataType = "json";
	settings.data = this.jsonRequestData(settings.data);
	
	var openbisObj = this;
	var originalOnSuccess = settings.success;
	
	settings.success = function(response){
		if(response.error){
			openbisObj.log("Request failed: " + JSON.stringify(response.error));
		}else{
			originalOnSuccess(response);
		}
	};
	
	settings.error = function(xhr, status, error){
		openbisObj.log("Request failed: " + error);
	};
	
	$.ajax(settings)
}

// Functions for working with cookies (see http://www.quirksmode.org/js/cookies.html)

_openbisInternal.prototype.createCookie = function(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

_openbisInternal.prototype.readCookie = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

_openbisInternal.prototype.eraseCookie = function(name) {
	this.createCookie(name,"",-1);
}

_openbisInternal.prototype.getDataStoreUrlForDataStoreCode = function(dataStoreCodeOrNull) {
	if(this.dataStores.length == 0){
		throw "Couldn't get a data store url as there are no data stores configured."
	}else{
		if(dataStoreCodeOrNull){
			$.each(this.dataStores, function(index, dataStore){
				if(dataStore.code == dataStoreCodeOrNull){
					return dataStore.downloadUrl;
				}
			});
			return null;
		}else{
			if(this.dataStores.length == 1){
				return this.dataStores[0].downloadUrl;
			}else{
				throw "There is more than one data store configured. Please specify a data store code to get a data store url.";
			}
		}
	}
}

_openbisInternal.prototype.getDataStoreUrlForDataSetCode = function(dataSetCode, action) {
	if(this.dataStores.length == 0){
		throw "Couldn't get a data store url as there are no data stores configured."
	}else if(this.dataStores.length == 1){
		action(this.dataStores[0].downloadUrl);
	}else{
		this.ajaxRequest({
			url: this.generalInfoServiceUrl,
			data: { "method" : "tryGetDataStoreBaseURL",
					"params" : [ this.sessionToken, dataSetCode ] 
					},
			success: function(response){
				var hostUrl = response.result;
				
				if(hostUrl){
					action(hostUrl + "/datastore_server");
				}else{
					action(null);
				}
			}
		 });
	}
}

_openbisInternal.prototype.getDataStoreApiUrlForDataStoreCode = function(dataStoreCodeOrNull, action) {
	var dataStoreUrl = this.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull);
	if(dataStoreUrl){
		return dataStoreUrl + "/rmi-dss-api-v1.json";
	}else{
		return null;
	}
}

_openbisInternal.prototype.getDataStoreApiUrlForDataSetCode = function(dataSetCode, action) {
	this.getDataStoreUrlForDataSetCode(dataSetCode, function(dataStoreUrl){
		if(dataStoreUrl){
			action(dataStoreUrl + "/rmi-dss-api-v1.json");
		}else{
			action(null);
		}
	});
}

/**
 * ===============
 * OpenBIS facade
 * ===============
 * 
 * The facade provides access to the following services:
 * 
 * - ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService
 * - ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService
 * - ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer
 * - ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService
 * - ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric
 * 
 * @class
 * 
 */

function openbis(openbisUrl) {
	this._internal = new _openbisInternal(openbisUrl);
}

/**
 * ==================================================================================
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService methods
 * ==================================================================================
 */

/**
 * Log into openBIS.
 * 
 * @see IGeneralInformationService.tryToAuthenticateForAllServices(String, String)
 * @method
 */
openbis.prototype.login = function(userId, userPassword, action) {
	var openbisObj = this
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "tryToAuthenticateForAllServices",
				"params" : [ userId, userPassword ] 
				},
		success: 
			function(loginResponse) {
				openbisObj._internal.sessionToken = loginResponse.result;
				openbisObj.rememberSession();
				openbisObj.listDataStores(function(storesResponse){
					openbisObj._internal.dataStores = storesResponse.result;
					action(loginResponse)
				});
			},
		error: function() {
			alert("Login failed")
		}
	 });
}

/**
 * Stores the current session in a cookie. 
 *
 * @method
 */
openbis.prototype.rememberSession = function() {
	this._internal.createCookie('openbis', this.getSession(), 1);
}

/**
 * Removes the current session from a cookie. 
 *
 * @method
 */
openbis.prototype.forgetSession = function() {
	this._internal.eraseCookie('openbis');
}

/**
 * Restores the current session from a cookie.
 *
 * @method
 */
openbis.prototype.restoreSession = function() {
	this._internal.sessionToken = this._internal.readCookie('openbis');
}

/**
 * Sets the current session.
 *
 * @method
 */
openbis.prototype.useSession = function(sessionToken){
	this._internal.sessionToken = sessionToken;
}

/**
 * Returns the current session.
 * 
 * @method
 */
openbis.prototype.getSession = function(){
	return this._internal.sessionToken;
}

/**
 * Checks whether the current session is still active.
 *
 * @see IGeneralInformationService.isSessionActive(String)
 * @method
 */
openbis.prototype.isSessionActive = function(action) {
	if(this.getSession()){
		this._internal.ajaxRequest({
			url: this._internal.generalInfoServiceUrl,
			data: { "method" : "isSessionActive",
					"params" : [ this.getSession() ] 
					},
			success: action
		});
	}else{
		action({ result : false })
	}
}

/**
 * Restores the current session from a cookie and executes 
 * the specified action if the session is still active.
 * 
 * @see restoreSession()
 * @see isSessionActive()
 * @method
 */
openbis.prototype.ifRestoredSessionActive = function(action) {
	this.restoreSession();
	this.isSessionActive(function(data) { if (data.result) action(data) });
}

/**
 * Log out of openBIS.
 * 
 * @see IGeneralInformationService.logout(String)
 * @method
 */
openbis.prototype.logout = function(action) {
	this.forgetSession();
	
	if(this.getSession()){
		this._internal.ajaxRequest({
			url: this._internal.generalInfoServiceUrl,
			data: { "method" : "logout",
					"params" : [ this.getSession() ] 
				  },
			success: action
		});
	}else{
		action({ result : null });
	}
}

/**
 * @see IGeneralInformationService.listNamedRoleSets(String)
 * @method
 */
openbis.prototype.listNamedRoleSets = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listNamedRoleSets",
				"params" : [ this.getSession() ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listSpacesWithProjectsAndRoleAssignments(String, String)
 * @method
 */
openbis.prototype.listSpacesWithProjectsAndRoleAssignments = function(databaseInstanceCodeOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSpacesWithProjectsAndRoleAssignments",
				"params" : [ this.getSession(),  databaseInstanceCodeOrNull ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForSamples(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForSamples = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForSamples",
				"params" : [ this.getSession(),
							 searchCriteria ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForSamples(String, SearchCriteria, EnumSet<SampleFetchOption>)
 * @method
 */
openbis.prototype.searchForSamplesWithFetchOptions = function(searchCriteria, fetchOptions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { 
				"method" : "searchForSamples",
				"params" : [ 
					this.getSession(),
					searchCriteria,
					fetchOptions ] 
		},
		success: action
	 });
}

/**
 * @see IGeneralInformationService.searchForSamplesOnBehalfOfUser(String, SearchCriteria, EnumSet<SampleFetchOption>, String)
 * @method
 */
openbis.prototype.searchForSamplesOnBehalfOfUser = function(searchCriteria, fetchOptions, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { 
				"method" : "searchForSamplesOnBehalfOfUser",
				"params" : [ 
					this.getSession(),
					searchCriteria,
					fetchOptions,
					userId ] 
		},
		success: action
	 });
}

/**
 * @see IGeneralInformationService.filterSamplesVisibleToUser(String, List<Sample>, String)
 * @method
 */
openbis.prototype.filterSamplesVisibleToUser = function(allSamples, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { 
				"method" : "filterSamplesVisibleToUser",
				"params" : [ 
					this.getSession(),
					allSamples,
					userId ] 
		},
		success: action
	 });
}

/**
 * @see IGeneralInformationService.listSamplesForExperiment(String, String)
 * @method
 */
openbis.prototype.listSamplesForExperiment = function(experimentIdentifier, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSamplesForExperiment",
				"params" : [ this.getSession(), experimentIdentifier ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listSamplesForExperimentOnBehalfOfUser(String, String, String)
 * @method
 */
openbis.prototype.listSamplesForExperimentOnBehalfOfUser = function(experimentIdentifier, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listSamplesForExperimentOnBehalfOfUser",
				"params" : [ this.getSession(), experimentIdentifier, userId ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSets(String, List<Sample>)
 * @method
 */
openbis.prototype.listDataSetsForSamples = function(samples, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSets",
				"params" : [ this.getSession(), samples ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperiments(String, List<Project>, String)
 * @method
 */
openbis.prototype.listExperiments = function(projects, experimentType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.getSession(), projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperimentsHavingSamples(String, List<Project>, String)
 * @method
 */
openbis.prototype.listExperimentsHavingSamples = function(projects, experimentType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperimentsHavingSamples",
				"params" : [ this.getSession(), projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperimentsHavingDataSets(String, List<Project>, String)
 * @method
 */
openbis.prototype.listExperimentsHavingDataSets = function(projects, experimentType, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperimentsHavingDataSets",
				"params" : [ this.getSession(), projects, experimentType ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.filterExperimentsVisibleToUser(String, List<Experiment>, String)
 * @method
 */
openbis.prototype.filterExperimentsVisibleToUser = function(allExperiments, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "filterExperimentsVisibleToUser",
				"params" : [ this.getSession(), allExperiments, userId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsForSample(String, Sample, boolean)
 * @method
 */
openbis.prototype.listDataSetsForSample = function(sample, restrictToDirectlyConnected, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForSample",
				"params" : [ this.getSession(), sample, restrictToDirectlyConnected ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataStores(String)
 * @method
 */
openbis.prototype.listDataStores = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataStores",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDefaultPutDataStoreBaseURL(String)
 * @method
 */
openbis.prototype.getDefaultPutDataStoreBaseURL = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDefaultPutDataStoreBaseURL",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.tryGetDataStoreBaseURL(String)
 * @method
 */
openbis.prototype.tryGetDataStoreBaseURL = function(dataSetCode, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "tryGetDataStoreBaseURL",
				"params" : [ this.getSession(), dataSetCode ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDataStoreBaseURLs(String, List<String>)
 * @method
 */
openbis.prototype.getDataStoreBaseURLs = function(dataSetCodes, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDataStoreBaseURLs",
				"params" : [ this.getSession(), dataSetCodes ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetTypes(String)
 * @method
 */
openbis.prototype.listDataSetTypes = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetTypes",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listVocabularies(String)
 * @method
 */
openbis.prototype.listVocabularies = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listVocabularies",
				"params" : [ this.getSession() ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSets(String, List<Sample>, EnumSet<Connections>)
 * @method
 */
openbis.prototype.listDataSetsForSamplesWithConnections = function(samples, connectionsToGet, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSets",
				"params" : [ this.getSession(), samples, connectionsToGet ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsOnBehalfOfUser(String, List<Sample>, EnumSet<Connections>, String)
 * @method
 */
openbis.prototype.listDataSetsForSamplesOnBehalfOfUser = function(samples, connectionsToGet, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsOnBehalfOfUser",
				"params" : [ this.getSession(), samples, connectionsToGet, userId ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsForExperiments(String, List<Experiment>, EnumSet<Connections>)
 * @method
 */
openbis.prototype.listDataSetsForExperiments = function(experiments, connectionsToGet, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForExperiments",
				"params" : [ this.getSession(), experiments, connectionsToGet ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.listDataSetsForExperimentsOnBehalfOfUser(String, List<Experiment>, EnumSet<Connections>, String)
 * @method
 */
openbis.prototype.listDataSetsForExperimentsOnBehalfOfUser = function(experiments, connectionsToGet, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listDataSetsForExperimentsOnBehalfOfUser",
				"params" : [ this.getSession(), experiments, connectionsToGet, userId ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDataSetMetaData(String, List<String>)
 * @method
 */
openbis.prototype.getDataSetMetaData = function(dataSetCodes, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDataSetMetaData",
				"params" : [ this.getSession(), dataSetCodes ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.getDataSetMetaData(String, List<String>, EnumSet<DataSetFetchOption>)
 * @method
 */
openbis.prototype.getDataSetMetaDataWithFetchOptions = function(dataSetCodes, fetchOptions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getDataSetMetaData",
				"params" : [ this.getSession(), dataSetCodes, fetchOptions ] 
		},
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForDataSets(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForDataSets = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForDataSets",
				"params" : [ this.getSession(),
							 searchCriteria ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForDataSetsOnBehalfOfUser(String, SearchCriteria, String)
 * @method
 */
openbis.prototype.searchForDataSetsOnBehalfOfUser = function(searchCriteria, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForDataSetsOnBehalfOfUser",
				"params" : [ this.getSession(),
							 searchCriteria,
							 userId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.filterDataSetsVisibleToUser(String, List<DataSet>, String)
 * @method
 */
openbis.prototype.filterDataSetsVisibleToUser = function(allDataSets, userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "filterDataSetsVisibleToUser",
				"params" : [ this.getSession(),
				             allDataSets,
							 userId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listExperiments(String, List<String>)
 * @method
 */
openbis.prototype.listExperimentsForIdentifiers = function(experimentIdentifiers, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listExperiments",
				"params" : [ this.getSession(),
				             experimentIdentifiers ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForExperiments(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForExperiments = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForExperiments",
				"params" : [ this.getSession(),
				             searchCriteria ] },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listProjects(String)
 * @method
 */
openbis.prototype.listProjects = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listProjects",
				"params" : [ this.getSession() ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listProjectsOnBehalfOfUser(String, String)
 * @method
 */
openbis.prototype.listProjectsOnBehalfOfUser = function(userId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listProjectsOnBehalfOfUser",
				"params" : [ this.getSession(), userId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.getMaterialByCodes(String, List<MaterialIdentifier>)
 * @method
 */
openbis.prototype.getMaterialByCodes = function(materialIdentifiers, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getMaterialByCodes",
				"params" : [ this.getSession(), materialIdentifiers ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.searchForMaterials(String, SearchCriteria)
 * @method
 */
openbis.prototype.searchForMaterials = function(searchCriteria, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "searchForMaterials",
				"params" : [ this.getSession(), searchCriteria ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listMetaprojects(String)
 * @method
 */
openbis.prototype.listMetaprojects = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listMetaprojects",
				"params" : [ this.getSession() ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.getMetaproject(String, IMetaprojectId)
 * @method
 */
openbis.prototype.getMetaproject = function(metaprojectId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "getMetaproject",
				"params" : [ this.getSession(), metaprojectId ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAttachmentsForProject(String, IProjectId, boolean)
 * @method
 */
openbis.prototype.listAttachmentsForProject = function(projectId, allVersions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAttachmentsForProject",
				"params" : [ this.getSession(), projectId, allVersions ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAttachmentsForExperiment(String, IExperimentId, boolean)
 * @method
 */
openbis.prototype.listAttachmentsForExperiment = function(experimentId, allVersions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAttachmentsForExperiment",
				"params" : [ this.getSession(), experimentId, allVersions ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationService.listAttachmentsForSample(String, ISampleId, boolean)
 * @method
 */
openbis.prototype.listAttachmentsForSample = function(sampleId, allVersions, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoServiceUrl,
		data: { "method" : "listAttachmentsForSample",
				"params" : [ this.getSession(), sampleId, allVersions ] 
			  },
		success: action
	});
}

/**
 * ==========================================================================================
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService methods
 * ==========================================================================================
 */

/**
 * @see IGeneralInformationChangingService.updateSampleProperties(String, long, Map<String,String>)
 * @method
 */
openbis.prototype.updateSampleProperties = function(sampleId, properties, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "updateSampleProperties",
				"params" : [ this.getSession(), sampleId, properties ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.addUnofficialVocabularyTerm(String, Long, NewVocabularyTerm)
 * @method
 */
openbis.prototype.addUnofficialVocabularyTerm = function(vocabularyId, term, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "addUnofficialVocabularyTerm",
				"params" : [ this.getSession(), vocabularyId, term ] 
			  },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.getWebAppSettings(String, String)
 * @method
 */
openbis.prototype.getWebAppSettings = function(webappId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "getWebAppSettings",
		params : [ this.getSession(), webappId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.setWebAppSettings(String, WebAppSettings)
 * @method
 */
openbis.prototype.setWebAppSettings = function(webappSettings, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "setWebAppSettings",
		params : [ this.getSession(), webappSettings ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.createMetaproject(String, String, String)
 * @method
 */
openbis.prototype.createMetaproject = function(name, descriptionOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "createMetaproject",
		params : [ this.getSession(), name, descriptionOrNull ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.updateMetaproject(String, IMetaprojectId, String, String)
 * @method
 */
openbis.prototype.updateMetaproject = function(metaprojectId, name, descriptionOrNull, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "updateMetaproject",
		params : [ this.getSession(), metaprojectId, name, descriptionOrNull ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.deleteMetaproject(String, IMetaprojectId)
 * @method
 */
openbis.prototype.deleteMetaproject = function(metaprojectId, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "deleteMetaproject",
		params : [ this.getSession(), metaprojectId ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.addToMetaproject(String, IMetaprojectId, MetaprojectAssignmentsIds)
 * @method
 */
openbis.prototype.addToMetaproject = function(metaprojectId, assignmentsToAdd, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "addToMetaproject",
		params : [ this.getSession(), metaprojectId, assignmentsToAdd ] },
		success: action
	});
}

/**
 * @see IGeneralInformationChangingService.removeFromMetaproject(String, IMetaprojectId, MetaprojectAssignmentsIds)
 * @method
 */
openbis.prototype.removeFromMetaproject = function(metaprojectId, assignmentsToRemove, action) {
	this._internal.ajaxRequest({
		url: this._internal.generalInfoChangingServiceUrl,
		data: { "method" : "removeFromMetaproject",
		params : [ this.getSession(), metaprojectId, assignmentsToRemove ] },
		success: action
	});
}

/**
 * ============================================================================
 * ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer methods
 * ============================================================================
 */

/**
 * @see IQueryApiServer.listQueries(String)
 * @method
 */
openbis.prototype.listQueries = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "listQueries",
				"params" : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.executeQuery(String, long, Map<String, String>)
 * @method
 */
openbis.prototype.executeQuery = function(queryId, parameterBindings, action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "executeQuery",
				"params" : [ this.getSession(), queryId, parameterBindings ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.listTableReportDescriptions(String)
 * @method
 */
openbis.prototype.listTableReportDescriptions = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "listTableReportDescriptions",
				"params" : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.createReportFromDataSets(String, String, String, List<String>)
 * @method
 */
openbis.prototype.createReportFromDataSets = function(dataStoreCode, serviceKey, dataSetCodes, action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "createReportFromDataSets",
		params : [ this.getSession(), dataStoreCode, serviceKey, dataSetCodes ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.listAggregationServices(String)
 * @method
 */
openbis.prototype.listAggregationServices = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "listAggregationServices",
		params : [ this.getSession() ] },
		success: action
	});
}

/**
 * @see IQueryApiServer.createReportFromAggregationService(String, String, String, Map<String, Object>)
 * @method
 */
openbis.prototype.createReportFromAggregationService = function(dataStoreCode, serviceKey, parameters, action) {
	this._internal.ajaxRequest({
		url: this._internal.queryServiceUrl,
		data: { "method" : "createReportFromAggregationService",
		params : [ this.getSession(), dataStoreCode, serviceKey, parameters ] },
		success: action
	});
}


/**
 * ==============================================================================
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.IWebInformationService methods
 * ==============================================================================
 */

/**
 * Returns the current server side session.
 * 
 * @see IWebInformationService.getSessionToken()
 * @method
 */
openbis.prototype.getSessionTokenFromServer = function(action) {
	this._internal.ajaxRequest({
		url: this._internal.webInfoServiceUrl,
		data: { "method" : "getSessionToken" },
		success: action
	 });
}

/**
 * =================================================================================
 * ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric methods
 * =================================================================================
 */

/**
 * @see IDssServiceRpcGeneric.listFilesForDataSet(String, DataSetFileDTO)
 * @method
 */
openbis.prototype.listFilesForDataSetFile = function(fileOrFolder, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(fileOrFolder.dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "listFilesForDataSet",
					"params" : [ openbisObj.getSession(), fileOrFolder ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSet(String, DataSetFileDTO)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetFile = function(fileOrFolder, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(fileOrFolder.dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSet",
					"params" : [ openbisObj.getSession(), fileOrFolder ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSetWithTimeout(String, DataSetFileDTO, long)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetFileWithTimeout = function(fileOrFolder, validityDurationInSeconds, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(fileOrFolder.dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSetWithTimeout",
					"params" : [ openbisObj.getSession(), fileOrFolder, validityDurationInSeconds ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.listFilesForDataSet(String, String, String, boolean)
 * @method
 */
openbis.prototype.listFilesForDataSet = function(dataSetCode, path, recursive, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "listFilesForDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, path, recursive ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSet(String, String, String)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSet = function(dataSetCode, path, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, path ] },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getDownloadUrlForFileForDataSetWithTimeout(String, String, String, long)
 * @method
 */
openbis.prototype.getDownloadUrlForFileForDataSetWithTimeout = function(dataSetCode, path, validityDurationInSeconds, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getDownloadUrlForFileForDataSetWithTimeout",
					"params" : [ openbisObj.getSession(), dataSetCode, path, validityDurationInSeconds ] },
			success: action
		});
	});
}

/**
 * Creates a session workspace file uploader inside the specified uploaderContainer element and for the default data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceUploader = function(uploaderContainer){
	this.createSessionWorkspaceUploaderForDataStore(uploaderContainer, null);
}

/**
 * Creates a session workspace file uploader inside the specified uploaderContainer element and for the specified data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceUploaderForDataStore = function(uploaderContainer, dataStoreCodeOrNull){
	var uploaderSupported = window.File && window.FileReader && window.XMLHttpRequest;

	if(!uploaderSupported){
		alert("Uploader is not supported by your browser.");
		return;
	}
	
	var $this = this;
	var dataStoreUrl = this._internal.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull);
		
	// figure out what is the location of the openbis.js script and assume that uploader resources are served by the same server
	var openbisScriptLocation = $('script[src*=openbis\\.js]').attr('src');
	var uploaderDirectoryLocation = jsFileLocation = openbisScriptLocation.replace(/js\/openbis\.js/g, 'uploader');
	
	$('head').append('<link rel="stylesheet" media="screen" type="text/css" href="' + uploaderDirectoryLocation + '/css/src/upload.css" />');
	$('head').append('<script charset="utf-8" type="text/javascript" src="' + uploaderDirectoryLocation + '/js/src/upload.js" />');
	
	$(uploaderContainer).load(uploaderDirectoryLocation + "/index.html", function(){
		Uploader.init({
		       smart_mode: true,
		       chunk_size: 1000*1024,
		       file_upload_url: dataStoreUrl + "/session_workspace_file_upload",
		       form_upload_url: dataStoreUrl + "/session_workspace_form_upload",
		       file_download_url: dataStoreUrl + "/session_workspace_file_download",
		       sessionID: $this.getSession()
		});
	});
}

/**
 * Creates a session workspace download url for a file with the specified filePath and for the default data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadUrl = function(filePath){
	return this.createSessionWorkspaceDownloadUrlForDataStore(filePath, null);
}

/**
 * Creates a session workspace download url for a file with the specified filePath and for the specified data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadUrlForDataStore = function(filePath, dataStoreCodeOrNull){
	var dataStoreUrl = this._internal.getDataStoreUrlForDataStoreCode(dataStoreCodeOrNull);
	return dataStoreUrl + "/session_workspace_file_download?sessionID=" + this.getSession() + "&filePath=" + filePath; 
}

/**
 * Create a session workspace download link for a file with the specified filePath at the default data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadLink = function(filePath, linkText){
	return this.createSessionWorkspaceDownloadLinkForDataStore(filePath, linkText, null);
}

/**
 * Create a session workspace download link for a file with the specified filePath at the specified data store.
 * @method
 */
openbis.prototype.createSessionWorkspaceDownloadLinkForDataStore = function(filePath, linkText, dataStoreCodeOrNull){
	return $("<a href='" + this.createSessionWorkspaceDownloadUrlForDataStore(filePath, dataStoreCodeOrNull) + "'>" + (linkText ? linkText : filePath) + "</a>"); 
}

/**
 * Downloads a session workspace file with the specified filePath from the default data store.
 * @method
 */
openbis.prototype.downloadSessionWorkspaceFile = function(filePath, action) {
	this.downloadSessionWorkspaceFileForDataStore(filePath, null, action);
}

/**
 * Downloads a session workspace file with the specified filePath from the specified data store.
 * @method
 */
openbis.prototype.downloadSessionWorkspaceFileForDataStore = function(filePath, dataStoreCodeOrNull, action) {
	$.ajax({
		type: "GET",
		url: this.createSessionWorkspaceDownloadUrlForDataStore(filePath, dataStoreCodeOrNull),
		success: action
	});
}

/**
 * Deletes a session workspace file with the specified filePath from the default data store.
 * @method
 */
openbis.prototype.deleteSessionWorkspaceFile = function(filePath, action) {
	this.deleteSessionWorkspaceFileForDataStore(filePath, null, action)
}

/**
 * Deletes a session workspace file with the specified filePath from the specified data store.
 * @method
 */
openbis.prototype.deleteSessionWorkspaceFileForDataStore = function(filePath, dataStoreCodeOrNull, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "deleteSessionWorkspaceFile",
							"params" : [ openbisObj.getSession(), filePath ]
						 },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.getPathToDataSet(String, String, String)
 * @method
 */
openbis.prototype.getPathToDataSet = function(dataSetCode, overrideStoreRootPathOrNull, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getPathToDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, overrideStoreRootPathOrNull ] },
			success: action
		});
	});
}

/**
 * List shares from the default data store.
 * 
 * @see IDssServiceRpcGeneric.listAllShares(String)
 * @method
 */
openbis.prototype.listAllShares = function(action) {
	this.listAllShares(null, action);
}

/**
 * List shares from the specified data store.
 * 
 * @see IDssServiceRpcGeneric.listAllShares(String)
 * @method
 */
openbis.prototype.listAllSharesForDataStore = function(dataStoreCodeOrNull, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "listAllShares",
							"params" : [ openbisObj.getSession() ]
						 },
			success: action
		});
	});
}

/**
 * @see IDssServiceRpcGeneric.shuffleDataSet(String, String, String)
 * @method
 */
openbis.prototype.shuffleDataSet = function(dataSetCode, shareId, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataSetCode(dataSetCode, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "shuffleDataSet",
					"params" : [ openbisObj.getSession(), dataSetCode, shareId ] },
			success: action
		});
	});
}

/**
 * Get a validation script from the default data store.
 * 
 * @see IDssServiceRpcGeneric.getValidationScript(String, String)
 * @method
 */
openbis.prototype.getValidationScript = function(dataSetTypeOrNull, action) {
	this.getValidationScriptForDataStore(dataSetTypeOrNull, null, action);
}

/**
 * Get a validation script from the specified data store.
 * 
 * @see IDssServiceRpcGeneric.getValidationScript(String, String)
 * @method
 */
openbis.prototype.getValidationScriptForDataStore = function(dataSetTypeOrNull, dataStoreCodeOrNull, action) {
	var openbisObj = this;
	this._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCodeOrNull, function(dataStoreApiUrl){
		openbisObj._internal.ajaxRequest({
			url: dataStoreApiUrl,
			data: { "method" : "getValidationScript",
							"params" : [ openbisObj.getSession(), dataSetTypeOrNull ]
						 },
			success: action
		});
	});
}


/**
 * =====================================================
 * OpenBIS webapp context internal code (DO NOT USE!!!)
 * =====================================================
 */

function _openbisWebAppContextInternal(){
	this.webappCode = this.getParameter("webapp-code");
	this.sessionId = this.getParameter("session-id");
	this.entityKind = this.getParameter("entity-kind");
	this.entityType = this.getParameter("entity-type");
	this.entityIdentifier = this.getParameter("entity-identifier");
	this.entityPermId = this.getParameter("entity-perm-id");
}

_openbisWebAppContextInternal.prototype.getParameter = function(parameterName){
	var match = location.search.match(RegExp("[?|&]"+parameterName+'=(.+?)(&|$)'));
	if(match && match[1]){
		return decodeURIComponent(match[1].replace(/\+/g,' '));
	}else{
		return null;
	}
}

/**
 * =======================
 * OpenBIS webapp context 
 * =======================
 * 
 * Provides a context information for webapps that are embedded inside the OpenBIS UI.
 * 
 * @class
 * 
 */
function openbisWebAppContext(){
	this._internal = new _openbisWebAppContextInternal();
}

openbisWebAppContext.prototype.getWebappCode = function(){
	return this._internal.webappCode;
}

openbisWebAppContext.prototype.getSessionId = function(){
	return this._internal.sessionId;
}

openbisWebAppContext.prototype.getEntityKind = function(){
	return this._internal.entityKind;
}

openbisWebAppContext.prototype.getEntityType = function(){
	return this._internal.entityType;
}

openbisWebAppContext.prototype.getEntityIdentifier = function(){
	return this._internal.entityIdentifier;
}

openbisWebAppContext.prototype.getEntityPermId = function(){
	return this._internal.entityPermId;
}

