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

var createExperimentFetchOptions = function() {
	return {
		"@type" : "ExperimentFetchOptions",

		"type" : {
			"@type" : "ExperimentTypeFetchOptions"
		},

		"project" : {
			"@type" : "ProjectFetchOptions",
			"space" : {
				"@type" : "SpaceFetchOptions"
			}
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
	}
}

var createSampleFetchOptions = function() {
	return {
		"@type" : "SampleFetchOptions",

		"sampleType" : {
			"@type" : "SampleTypeFetchOptions"
		},

		"experiment" : {
			"@type" : "ExperimentFetchOptions",
			"project" : {
				"@type" : "ProjectFetchOptions",
				"space" : {
					"@type" : "SpaceFetchOptions"
				}
			}
		},

		"space" : {
			"@type" : "SpaceFetchOptions"
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
	}
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
				} ], createExperimentFetchOptions() ]
			},
			success : function(experiments) {
				assertObjectsCount(experiments, 1);

				var experiment = experiments[0];
				equal(experiment.code, "EXP-1", "Experiment code");
				equal(experiment.type.code, "HCS_PLATONIC", "Type code");
				equal(experiment.project.code, "SCREENING-EXAMPLES", "Project code");
				equal(experiment.project.space.code, "PLATONIC", "Space code");
				facade.close();
			}
		});
	});
});

test("listSamples()", function() {
	createFacadeAndLogin(function(facade) {
		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "listSamples",
				"params" : [ facade.sessionToken, [ {
					"@type" : "SamplePermId",
					"permId" : "20130412140147735-20"
				} ], createSampleFetchOptions() ]
			},
			success : function(samples) {
				assertObjectsCount(samples, 1);

				var sample = samples[0];
				equal(sample.code, "PLATE-1", "Sample code");
				equal(sample.sampleType.code, "PLATE", "Type code");
				equal(sample.experiment.code, "EXP-1", "Experiment code");
				equal(sample.experiment.project.code, "SCREENING-EXAMPLES", "Project code");
				equal(sample.space.code, "PLATONIC", "Space code");
				facade.close();
			}
		});
	});
});

test("searchExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "searchExperiments",
				"params" : [ facade.sessionToken, {
					"@type" : "ExperimentSearchCriterion",
					"criteria" : [ {
						"@type" : "CodeSearchCriterion",
						"fieldValue" : {
							"@type" : "StringEqualToValue",
							"value" : "TEST-EXPERIMENT-2"
						}
					} ]
				}, createExperimentFetchOptions() ]
			},
			success : function(experiments) {
				assertObjectsCount(experiments, 1);

				var experiment = experiments[0];
				equal(experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
				equal(experiment.type.code, "UNKNOWN", "Type code");
				equal(experiment.project.code, "TEST-PROJECT", "Project code");
				equal(experiment.project.space.code, "TEST", "Space code");
				facade.close();
			}
		});
	});
});

test("searchSamples()", function() {
	createFacadeAndLogin(function(facade) {
		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "searchSamples",
				"params" : [ facade.sessionToken, {
					"@type" : "SampleSearchCriterion",
					"criteria" : [ {
						"@type" : "CodeSearchCriterion",
						"fieldValue" : {
							"@type" : "StringEqualToValue",
							"value" : "PLATE-1"
						}
					} ]
				}, createSampleFetchOptions() ]
			},
			success : function(samples) {
				assertObjectsCount(samples, 1);

				var sample = samples[0];
				equal(sample.code, "PLATE-1", "Sample code");
				equal(sample.sampleType.code, "PLATE", "Type code");
				equal(sample.experiment.code, "EXP-1", "Experiment code");
				equal(sample.experiment.project.code, "SCREENING-EXAMPLES", "Project code");
				equal(sample.space.code, "PLATONIC", "Space code");
				facade.close();
			}
		});
	});
});

test("createExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());

		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "createExperiments",
				"params" : [ facade.sessionToken, [ {
					"@type" : "ExperimentCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"projectId" : {
						"@type" : "ProjectIdentifier",
						"identifier" : "/TEST/TEST-PROJECT"
					},

					"tagIds" : [ {
						"@type" : "TagNameId",
						"name" : "CREATE_JSON_TAG"
					} ]

				} ] ]
			},
			success : function(experimentPermIds) {
				facade.ajaxRequest({
					url : testApiUrl,
					data : {
						"method" : "listExperiments",
						"params" : [ facade.sessionToken, [ experimentPermIds[0] ], createExperimentFetchOptions() ]
					},
					success : function(experiments) {
						assertObjectsCount(experiments, 1);

						var experiment = experiments[0];
						equal(experiment.code, code, "Experiment code");
						equal(experiment.type.code, "UNKNOWN", "Type code");
						equal(experiment.project.code, "TEST-PROJECT", "Project code");
						equal(experiment.project.space.code, "TEST", "Space code");
						equal(experiment.tags[0].name, "CREATE_JSON_TAG", "Tag code");
						facade.close();
					}
				});
			}
		});
	});
});

test("createSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "CREATE_JSON_SAMPLE_" + (new Date().getTime());

		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "createSamples",
				"params" : [ facade.sessionToken, [ {
					"@type" : "SampleCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"spaceId" : {
						"@type" : "SpacePermId",
						"permId" : "TEST"
					},

					"tagIds" : [ {
						"@type" : "TagNameId",
						"name" : "CREATE_JSON_TAG"
					} ]

				} ] ]
			},
			success : function(samplePermIds) {
				facade.ajaxRequest({
					url : testApiUrl,
					data : {
						"method" : "listSamples",
						"params" : [ facade.sessionToken, [ samplePermIds[0] ], createSampleFetchOptions() ]
					},
					success : function(samples) {
						assertObjectsCount(samples, 1);

						var sample = samples[0];
						equal(sample.code, code, "Sample code");
						equal(sample.sampleType.code, "UNKNOWN", "Type code");
						equal(sample.space.code, "TEST", "Space code");
						facade.close();
					}
				});
			}
		});
	});
});

test("updateExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "UPDATE_JSON_EXPERIMENT_" + (new Date().getTime());

		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "createExperiments",
				"params" : [ facade.sessionToken, [ {
					"@type" : "ExperimentCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"projectId" : {
						"@type" : "ProjectIdentifier",
						"identifier" : "/TEST/TEST-PROJECT"
					}

				} ] ]
			},
			success : function(experimentPermIds) {
				facade.ajaxRequest({
					url : testApiUrl,
					data : {
						"method" : "updateExperiments",
						"params" : [ facade.sessionToken, [ {
							"@type" : "ExperimentUpdate",

							"experimentId" : experimentPermIds[0],

							"projectId" : {
								"@type" : "ProjectIdentifier",
								"identifier" : "/PLATONIC/SCREENING-EXAMPLES"
							}

						} ] ]
					},
					success : function() {

						facade.ajaxRequest({
							url : testApiUrl,
							data : {
								"method" : "listExperiments",
								"params" : [ facade.sessionToken, [ experimentPermIds[0] ], createExperimentFetchOptions() ]
							},
							success : function(experiments) {
								assertObjectsCount(experiments, 1);

								var experiment = experiments[0];
								equal(experiment.code, code, "Experiment code");
								equal(experiment.type.code, "UNKNOWN", "Type code");
								equal(experiment.project.code, "SCREENING-EXAMPLES", "Project code");
								equal(experiment.project.space.code, "PLATONIC", "Space code");
								facade.close();
							}
						});
					}
				});
			}
		});
	});
});

test("updateSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "UPDATE_JSON_SAMPLE_" + (new Date().getTime());

		facade.ajaxRequest({
			url : testApiUrl,
			data : {
				"method" : "createSamples",
				"params" : [ facade.sessionToken, [ {
					"@type" : "SampleCreation",

					"typeId" : {
						"@type" : "EntityTypePermId",
						"permId" : "UNKNOWN"
					},

					"code" : code,

					"spaceId" : {
						"@type" : "SpacePermId",
						"permId" : "PLATONIC"
					}

				} ] ]
			},
			success : function(samplePermIds) {
				facade.ajaxRequest({
					url : testApiUrl,
					data : {
						"method" : "updateSamples",
						"params" : [ facade.sessionToken, [ {
							"@type" : "SampleUpdate",

							"sampleId" : samplePermIds[0],

							"spaceId" : {
								"@type" : "SpacePermId",
								"permId" : "TEST"
							}

						} ] ]
					},
					success : function() {

						facade.ajaxRequest({
							url : testApiUrl,
							data : {
								"method" : "listSamples",
								"params" : [ facade.sessionToken, [ samplePermIds[0] ], createSampleFetchOptions() ]
							},
							success : function(samples) {
								assertObjectsCount(samples, 1);

								var sample = samples[0];
								equal(sample.code, code, "Sample code");
								equal(sample.sampleType.code, "UNKNOWN", "Type code");
								equal(sample.space.code, "TEST", "Space code");
								facade.close();
							}
						});
					}
				});
			}
		});
	});
});

