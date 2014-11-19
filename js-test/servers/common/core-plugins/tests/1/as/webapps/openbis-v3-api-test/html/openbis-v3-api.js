var openbis = function() {

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

			settings.success = function(response) {
				if (response.error) {
					_private.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(response.error));
					originalError(response.error);
				} else {
					_private.log("Request succeeded - data: " + JSON.stringify(settings.data));
					originalSuccess(response.result);
				}
			}

			settings.error = function(xhr, status, error) {
				_private.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(error));
				originalError(error);
			}

			$.ajax(settings)
		},

		log : function(msg) {
			if (console) {
				console.log(msg);
			}
		}

	}

	return function(openbisUrl) {

		this.login = function(user, password, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "login",
					"params" : [ user, password ]
				},
				success : function(sessionToken) {
					_private.sessionToken = sessionToken;
					onSuccess(sessionToken);
				},
				error : onError
			});
		}

		this.logout = function(onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "logout",
					"params" : [ _private.sessionToken ]
				},
				success : function() {
					_private.sessionToken = null;
					onSuccess();
				},
				error : onError
			});
		}

		this.mapExperiments = function(experimentIds, experimentFetchOptions, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapExperiments",
					"params" : [ _private.sessionToken, experimentIds, experimentFetchOptions ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.mapSamples = function(sampleIds, sampleFetchOptions, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "mapSamples",
					"params" : [ _private.sessionToken, sampleIds, sampleFetchOptions ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.searchExperiments = function(experimentSearchCriterion, experimentFetchOptions, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchExperiments",
					"params" : [ _private.sessionToken, experimentSearchCriterion, experimentFetchOptions ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.searchSamples = function(sampleSearchCriterion, sampleFetchOptions, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "searchSamples",
					"params" : [ _private.sessionToken, sampleSearchCriterion, sampleFetchOptions ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.createExperiments = function(experimentCreations, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createExperiments",
					"params" : [ _private.sessionToken, experimentCreations ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.createSamples = function(sampleCreations, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "createSamples",
					"params" : [ _private.sessionToken, sampleCreations ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.updateExperiments = function(experimentUpdates, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateExperiments",
					"params" : [ _private.sessionToken, experimentUpdates ]
				},
				success : onSuccess,
				error : onError
			});
		}

		this.updateSamples = function(sampleUpdates, onSuccess, onError) {
			_private.ajaxRequest({
				url : openbisUrl,
				data : {
					"method" : "updateSamples",
					"params" : [ _private.sessionToken, sampleUpdates ]
				},
				success : onSuccess,
				error : onError
			});
		}

	}

}();