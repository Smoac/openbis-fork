define([ 'jquery', 'util/Json' ], function($, stjsUtil) {

	var __private = function() {

		this.ajaxRequest = function(settings) {
			var thisPrivate = this;

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
					thisPrivate.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(response.error));
					originalError(response.error);
					dfd.reject(response.error);
				} else {
					thisPrivate.log("Request succeeded - data: " + JSON.stringify(settings.data));
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
				thisPrivate.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(error));
				originalError(error);
				dfd.reject(error);
			}

			$.ajax(settings).done(success).fail(error);

			return dfd.promise();
		};

		this.loginCommon = function(user, response) {
			var thisPrivate = this;
			var dfd = $.Deferred();

			response.done(function(sessionToken) {
				if (sessionToken && sessionToken.indexOf(user) > -1) {
					thisPrivate.sessionToken = sessionToken;
					dfd.resolve(sessionToken);
				} else {
					dfd.reject();
				}
			}).fail(function() {
				dfd.reject();
			});
			return dfd.promise();
		};

		this.log = function(msg) {
			if (console) {
				console.log(msg);
			}
		}
	}

	var facade = function(openbisUrl) {

		if (!openbisUrl) {
			openbisUrl = "/openbis/openbis/rmi-application-server-v3.json";
		}

		this._private = new __private();

		this.login = function(user, password) {
			var thisFacade = this;
			return thisFacade._private.loginCommon(user, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "login",
					"params" : [ user, password ]
				}
			}));
		}

		this.loginAs = function(user, password, asUserId) {
			var thisFacade = this;
			return thisFacade._private.loginCommon(user, thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "loginAs",
					"params" : [ user, password, asUserId ]
				}
			}));
		}

		this.logout = function() {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "logout",
					"params" : [ thisFacade._private.sessionToken ]
				}
			}).done(function() {
				thisFacade._private.sessionToken = null;
			});
		}

		this.createSpaces = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSpaces",
					"params" : [ thisFacade._private.sessionToken, creations ]
				}
			});
		}

		this.createProjects = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createProjects",
					"params" : [ thisFacade._private.sessionToken, creations ]
				}
			});
		}

		this.createExperiments = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExperiments",
					"params" : [ thisFacade._private.sessionToken, creations ]
				}
			});
		}

		this.createSamples = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSamples",
					"params" : [ thisFacade._private.sessionToken, creations ]
				}
			});
		}

		this.createMaterials = function(creations) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createMaterials",
					"params" : [ thisFacade._private.sessionToken, creations ]
				}
			});
		}

		this.updateSpaces = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSpaces",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateProjects = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateProjects",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateExperiments = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateExperiments",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateSamples = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSamples",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateDataSets = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateDataSets",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.updateMaterials = function(updates) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateMaterials",
					"params" : [ thisFacade._private.sessionToken, updates ]
				}
			});
		}

		this.mapSpaces = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapProjects = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapProjects",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapExperiments = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapSamples = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSamples",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapDataSets = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.mapMaterials = function(ids, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, fetchOptions ]
				}
			});
		}

		this.searchSpaces = function(criterion, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSpaces",
					"params" : [ thisFacade._private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchProjects = function(criterion, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchProjects",
					"params" : [ thisFacade._private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchExperiments = function(criterion, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExperiments",
					"params" : [ thisFacade._private.sessionToken, criterion, fetchOptions ]
				}
			})
		}

		this.searchSamples = function(criterion, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSamples",
					"params" : [ thisFacade._private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchDataSets = function(criterion, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDataSets",
					"params" : [ thisFacade._private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.searchMaterials = function(criterion, fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchMaterials",
					"params" : [ thisFacade._private.sessionToken, criterion, fetchOptions ]
				}
			});
		}

		this.deleteSpaces = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteSpaces",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteProjects = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteProjects",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteExperiments = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteExperiments",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteSamples = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteSamples",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteDataSets = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteDataSets",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.deleteMaterials = function(ids, deletionOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "deleteMaterials",
					"params" : [ thisFacade._private.sessionToken, ids, deletionOptions ]
				}
			});
		}

		this.listDeletions = function(fetchOptions) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "listDeletions",
					"params" : [ thisFacade._private.sessionToken, fetchOptions ]
				}
			});
		}

		this.revertDeletions = function(ids) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "revertDeletions",
					"params" : [ thisFacade._private.sessionToken, ids ]
				}
			});
		}

		this.confirmDeletions = function(ids) {
			var thisFacade = this;
			return thisFacade._private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "confirmDeletions",
					"params" : [ thisFacade._private.sessionToken, ids ]
				}
			});
		}
	}

	return facade;

});