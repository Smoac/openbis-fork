/*
 * These tests should be run against openBIS instance 
 * with screening sprint server database version
 */

var testProtocol = window.location.protocol;
var testHost = window.location.hostname;
var testPort = window.location.port;
var testUrl = testProtocol + "//" + testHost + ":" + testPort;
var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

var testUserId = "openbis_test_js";
var testUserPassword = "password";

var openbis = function() {

	this.ajaxRequest = function(settings) {
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
				console.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(response.error));
				originalError(response.error);
			} else {
				originalSuccess(response.result);
			}
		}

		settings.error = function(xhr, status, error) {
			console.log("Request failed - data: " + JSON.stringify(settings.data) + ", error: " + JSON.stringify(error));
			originalError(error);
		}

		$.ajax(settings)
	}

	this.login = function(user, password, onSuccess, onError) {
		var thisOpenbis = this;

		this.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "login",
				"params" : [ user, password ]
			},
			success : function(sessionToken) {
				thisOpenbis.sessionToken = sessionToken;
				onSuccess(sessionToken);
			},
			error : onError
		});
	}

	this.logout = function(onSuccess, onError) {
		var thisOpenbis = this;

		this.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "logout",
				"params" : [ thisOpenbis.sessionToken ]
			},
			success : function() {
				thisOpenbis.sessionToken = null;
				onSuccess();
			},
			error : onError
		});
	}

}

var createFacade = function(action) {
	stop();

	var facade = new openbis();

	facade.close = function() {
		facade.logout(function() {
			facade.closed = true;
		});
	};

	action(facade);

	var timeout = 30000;
	var checkInterval = 100;
	var intervalTotal = 0;

	var startWhenClosed = function() {
		if (facade.closed) {
			start();
		} else {
			intervalTotal += checkInterval;

			if (intervalTotal < timeout) {
				setTimeout(startWhenClosed, checkInterval);
			} else {
				start();
			}
		}
	};

	startWhenClosed();
}

var createFacadeAndLogin = function(action) {
	createFacade(function(facade) {
		facade.login(testUserId, testUserPassword, function() {
			action(facade);
		});
	});
}

test("listExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "listExperiments",
				"params" : [ facade.sessionToken, [ {
					"@type" : "ExperimentPermId",
					"permId" : "20130412105232616-2"
				} ], {
					"@type" : "ExperimentFetchOptions",

					"type" : {
						"@type" : "ExperimentTypeFetchOptions"
					},

					"project" : {
						"@type" : "ProjectFetchOptions"
					},

					"properties" : {
						"@type" : "PropertyFetchOptions"
					},

					"tags" : {
						"@type" : "TagFetchOptions"
					},

					"registrator" : {
						"@type" : "PersonFetchOptions"
					},

					"modifier" : {
						"@type" : "PersonFetchOptions"
					},

					"attachments" : {
						"@type" : "AttachmentFetchOptions"
					}
				} ]
			},
			success : function(experiments) {
				alert(experiments);
			}
		});
	});
});
