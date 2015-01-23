define(['jquery', 'support/Utils'], function($, stjsUtil) {

	var _private = {

		ajaxRequest : function(settings) {
			settings.type = "POST";
			settings.processData = false;
			settings.dataType = "json";

			var data = settings.data;
			data["id"] = "1";
			data["jsonrpc"] = "2.0";
			settings.data = JSON.stringify(data);

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
					originalSuccess(response.result);
					dfd.resolve(response.result);
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

		log : function(msg) {
			if (console) {
				console.log(msg);
			}
		}
	}

	return function(openbisUrl) {

		this.login = function(user, password) {
			var dfd = $.Deferred();
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "login",
					"params" : [ user, password ]
				}
			}).done(function(sessionToken) {
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
		}

		this.logout = function() {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "logout",
					"params" : [ _private.sessionToken ]
				}
			})
			.done(function() {
				_private.sessionToken = null;				
			});
		}

		this.mapExperiments = function(experimentIds, experimentFetchOptions) {
			var dfd = $.Deferred();

			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapExperiments",
					"params" : [ _private.sessionToken, experimentIds, experimentFetchOptions ]
				}
			})
			.done(function(experiments) {
				require(['dto/entity/experiment/Experiment'], function() {
					var experimentDTOs = {};
					for(var experimentPermId  in experiments) {
						var experimentJson = experiments[experimentPermId];
						var newExperiment = stjsUtil.fromJson(experimentJson);
						experimentDTOs[newExperiment.getPermId().getPermId()] = newExperiment;
					}
					dfd.resolve(experimentDTOs);
				});
			}).fail(function() {
				dfd.reject(arguments);
			});
			
			return dfd.promise();
		}

		this.performOperations = function(operations) {
			return _private.ajaxRequest({
				"method" : "performOperations",
				"params" : [_private.sessionToken, operations]
			});
		}

		this.mapSamples = function(sampleIds, sampleFetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSamples",
					"params" : [ _private.sessionToken, sampleIds, sampleFetchOptions ]
				}
			});
		}

		this.searchExperiments = function(experimentSearchCriterion, experimentFetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExperiments",
					"params" : [ _private.sessionToken, experimentSearchCriterion, experimentFetchOptions ]
				}
			});
		}

		this.searchSamples = function(sampleSearchCriterion, sampleFetchOptions) {
			var dfd = $.Deferred();

			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSamples",
					"params" : [ _private.sessionToken, sampleSearchCriterion, sampleFetchOptions ]
				}
			}).done(function(samples) {
				require(['dto/entity/sample/Sample'], function() {
					var sampleDTOs = {};
					for(var sampleId in samples) {
						var sampleJson = samples[sampleId];
						var newSample = stjsUtil.fromJson(sampleJson);
						sampleDTOs[newSample.getPermId().getPermId()] = newSample;
					}
					dfd.resolve(sampleDTOs);
				});
			}).fail(function() {
				dfd.reject(arguments);
			});
			return dfd.promise();
		}

		this.searchDataSets = function(dataSetSearchCriterion, dataSetFetchOptions) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchDataSets",
					"params" : [_private.sessionToken, dataSetSearchCriterion, dataSetFetchOptions]
				}
			});
		}

		this.createExperiments = function(experimentCreations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExperiments",
					"params" : [ _private.sessionToken, experimentCreations ]
				}
			});
		}

		this.createSamples = function(sampleCreations) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSamples",
					"params" : [ _private.sessionToken, sampleCreations ]
				}
			});
		}

		this.updateExperiments = function(experimentUpdates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateExperiments",
					"params" : [ _private.sessionToken, experimentUpdates ]
				}
			});
		}

		this.updateSamples = function(sampleUpdates) {
			return _private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSamples",
					"params" : [ _private.sessionToken, sampleUpdates ]
				}
			});
		}

		this.deleteExperiments = function(experimentIds, deletionOptions) {
			return _private.ajaxRequest({
				"method" : "deleteExperiments",
				"params" : [_private.sessionToken, experimentIds, deletionOptions]
			});
		}

		this.deleteSamples = function(sampleIds, deletionOptions) {
			return _private.ajaxRequest({
				"method" : "deleteSamples",
				"params" : [_private.sessionToken, sampleIds, deletionOptions]
			});
		}

		this.listDeletions = function(fetchOptions) {
			return _private.ajaxRequest({
				"method" : "listDeletions",
				"params" : [_private.sessionToken, fetchOptions]
			});
		}

		this.revertDeletions = function(deletionIds) {
			return _private.ajaxRequest({
				"method" : "revertDeletions",
				"params" : [_private.sessionToken, deletionIds]
			});
		}

		this.confirmDeletions = function(deletionIds) {
			return _private.ajaxRequest({
				"method" : "confirmDeletions",
				"params" : [_private.sessionToken, deletionIds]
			});
		}
	}

});