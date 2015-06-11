define([ 'jquery', 'util/Json' ], function($, stjsUtil) {

	var _private = {

		ajaxRequest : function(settings) {
			settings.type = "POST";
			settings.processData = false;
			settings.dataType = "json";

			var data = settings.data;
			data["id"] = "1";
			data["jsonrpc"] = "2.0";
			settings.data = JSON.stringify(stjsUtil.decycle(data));

			var originalSuccess = settings.success || function() {
			};
			var originalError = settings.error || function() {
			};

			var dfd = $.Deferred();
			function success(response) {
				if (response.error) {
					_private.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(response.error));
					originalError(response.error);
					dfd.reject(response.error);
				} else {
					_private.log("Request succeeded - data: " + JSON.stringify(settings.data));
					stjsUtil.fromJson(response.result).done(function(dtos) {
						originalSuccess(dtos);
						dfd.resolve(dtos);
					}).fail(function() {
						originalError(arguments);
						dfd.reject(arguments);
					});
				}
			}

			function error(xhr, status, error) {
				_private.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(error));
				originalError(error);
				dfd.reject(error);
			}

			$.ajax(settings).done(success).fail(error);

			return dfd.promise();
		},

		loginCommon : function(user, response) {
			var dfd = $.Deferred();
			response.done(function(sessionToken) {
				if (sessionToken && sessionToken.indexOf(user) > -1) {
					_private.sessionToken = sessionToken;
					dfd.resolve(sessionToken);
				} else {
					dfd.reject();
				}
			}).fail(function() {
				dfd.reject();
			});
			return dfd.promise();
		},

		log : function(msg) {
			if (console) {
				console.log(msg);
			}
		}
	}

	return function(openbisUrl) {

		if (!openbisUrl) {
			openbisUrl = "/openbis/openbis/rmi-application-server-v3.json";
		}

		this.login = function(user, password) {
			return _private.loginCommon(user, _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "login",
					"params" : [ user, password ]
				}
			}));
		}

		this.loginAs = function(user, password, asUserId) {
			return _private.loginCommon(user, _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "loginAs",
					"params" : [ user, password, asUserId ]
				}
			}));
		}

		this.logout = function() {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "logout",
					"params" : [ _private.sessionToken ]
				}
			}).done(function() {
				_private.sessionToken = null;
			});
		}

		this.createSpaces = function(creations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSpaces",
					"params" : [ _private.sessionToken, creations ]
				}
			});
		}

		this.createProjects = function(creations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createProjects",
					"params" : [ _private.sessionToken, creations ]
				}
			});
		}

		this.createExperiments = function(creations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExperiments",
					"params" : [ _private.sessionToken, creations ]
				}
			});
		}

		this.createSamples = function(creations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSamples",
					"params" : [ _private.sessionToken, creations ]
				}
			});
		}

		this.createMaterials = function(creations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createMaterials",
					"params" : [ _private.sessionToken, creations ]
				}
			});
		}

		this.updateSpaces = function(updates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSpaces",
					"params" : [ _private.sessionToken, updates ]
				}
			});
		}

		this.updateProjects = function(updates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateProjects",
					"params" : [ _private.sessionToken, updates ]
				}
			});
		}

		this.updateExperiments = function(updates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateExperiments",
					"params" : [ _private.sessionToken, updates ]
				}
			});
		}

		this.updateSamples = function(updates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSamples",
					"params" : [ _private.sessionToken, updates ]
				}
			});
		}

		this.updateDataSets = function(updates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateDataSets",
					"params" : [ _private.sessionToken, updates ]
				}
			});
		}

		this.updateMaterials = function(updates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateMaterials",
					"params" : [ _private.sessionToken, updates ]
				}
			});
		}

		this.mapSpaces = function(ids, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSpaces",
					"params" : [ _private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapProjects = function(ids, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapProjects",
					"params" : [ _private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapExperiments = function(ids, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapExperiments",
					"params" : [ _private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapSamples = function(ids, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSamples",
					"params" : [ _private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapDataSets = function(ids, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapDataSets",
					"params" : [ _private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapMaterials = function(ids, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapMaterials",
					"params" : [ _private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.searchSpaces = function(criterion, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSpaces",
					"params" : [ _private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchProjects = function(criterion, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchProjects",
					"params" : [ _private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchExperiments = function(criterion, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExperiments",
					"params" : [ _private.sessionToken, criterion, fetchOptions ]
				}
			})
		}

		this.searchSamples = function(criterion, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSamples",
					"params" : [ _private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchDataSets = function(criterion, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDataSets",
					"params" : [ _private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchMaterials = function(criterion, fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchMaterials",
					"params" : [ _private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteSpaces",
					"params" : [ _private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteProjects = function(ids, deletionOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteProjects",
					"params" : [ _private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteExperiments",
					"params" : [ _private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteSamples = function(ids, deletionOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteSamples",
					"params" : [ _private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteDataSets",
					"params" : [ _private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteMaterials",
					"params" : [ _private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.listDeletions = function(fetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "listDeletions",
					"params" : [ _private.sessionToken, fetchOptions ]
				}
			});
		}

		this.revertDeletions = function(ids) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "revertDeletions",
					"params" : [ _private.sessionToken, ids ]
				}
			});
		}

		this.confirmDeletions = function(ids) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "confirmDeletions",
					"params" : [ _private.sessionToken, ids ]
				}
			});
		}
	}

});