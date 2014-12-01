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

var createFacade = function(action) {
	stop();

	var facade = new openbis(testApiUrl);

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
	var fo = new ExperimentFetchOptions();
	fo.withType();
	fo.withProject().withSpace();
	fo.withProperties();
	fo.withTags();
	fo.withRegistrator();
	fo.withModifier();
	fo.withAttachments();
	return fo;
}

var createSampleFetchOptions = function() {
	var fo = new SampleFetchOptions();
	fo.withType();
	fo.withExperiment().withProject().withSpace();
	fo.withSpace();
	fo.withProperties();
	fo.withTags();
	fo.withRegistrator();
	fo.withModifier();
	fo.withAttachments();
	return fo;
}

test("mapExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var experimentIds = [ {
			"@type" : "ExperimentPermId",
			"permId" : "20130412105232616-2"
		} ];
		var fetchOptions = createExperimentFetchOptions();

		facade.mapExperiments(experimentIds, fetchOptions, function(experiments) {
			assertObjectsCount(Object.keys(experiments), 1);

			var experiment = experiments["20130412105232616-2"];
			equal(experiment.code, "EXP-1", "Experiment code");
			equal(experiment.type.code, "HCS_PLATONIC", "Type code");
			equal(experiment.project.code, "SCREENING-EXAMPLES", "Project code");
			equal(experiment.project.space.code, "PLATONIC", "Space code");
			facade.close();
		}, function(error) {
			ok(false, error);
		});
	});
});

test("mapSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var sampleIds = [ {
			"@type" : "SamplePermId",
			"permId" : "20130412140147735-20"
		} ];
		var fetchOptions = createSampleFetchOptions();

		facade.mapSamples(sampleIds, fetchOptions, function(samples) {
			assertObjectsCount(Object.keys(samples), 1);

			var sample = samples["20130412140147735-20"];
			equal(sample.code, "PLATE-1", "Sample code");
			equal(sample.type.code, "PLATE", "Type code");
			equal(sample.experiment.code, "EXP-1", "Experiment code");
			equal(sample.experiment.project.code, "SCREENING-EXAMPLES", "Project code");
			equal(sample.space.code, "PLATONIC", "Space code");
			facade.close();
		}, function(error) {
			ok(false, error);
		});
	});
});

test("searchExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var criterion = {
			"@type" : "ExperimentSearchCriterion",
			"criteria" : [ {
				"@type" : "CodeSearchCriterion",
				"fieldValue" : {
					"@type" : "StringEqualToValue",
					"value" : "TEST-EXPERIMENT-2"
				}
			} ]
		};
		var fetchOptions = createExperimentFetchOptions();

		facade.searchExperiments(criterion, fetchOptions, function(experiments) {
			assertObjectsCount(experiments, 1);

			var experiment = experiments[0];
			equal(experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
			equal(experiment.type.code, "UNKNOWN", "Type code");
			equal(experiment.project.code, "TEST-PROJECT", "Project code");
			equal(experiment.project.space.code, "TEST", "Space code");
			facade.close();
		}, function(error) {
			ok(false, error);
		});
	});
});

test("searchSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var criterion = {
			"@type" : "SampleSearchCriterion",
			"criteria" : [ {
				"@type" : "CodeSearchCriterion",
				"fieldValue" : {
					"@type" : "StringEqualToValue",
					"value" : "PLATE-1"
				}
			} ]
		};
		var fetchOptions = createSampleFetchOptions()

		facade.searchSamples(criterion, fetchOptions, function(samples) {
			assertObjectsCount(samples, 1);

			var sample = samples[0];
			equal(sample.code, "PLATE-1", "Sample code");
			equal(sample.type.code, "PLATE", "Type code");
			equal(sample.experiment.code, "EXP-1", "Experiment code");
			equal(sample.experiment.project.code, "SCREENING-EXAMPLES", "Project  code");
			equal(sample.space.code, "PLATONIC", "Space code");
			facade.close();
		}, function(error) {
			ok(false, error);
		});
	});
});

test("createExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());
		var creations = [ {
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
				"@type" : "TagCode",
				"code" : "CREATE_JSON_TAG"
			} ]
		} ];
		
		var experimentCreation = new ExperimentCreation();
		experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
		experimentCreation.setCode(code);
		experimentCreation.setProjectId(new ProjectIdentifier("/TEST/TEST-PROJECT"));
		experimentCreation.setTagIds([new TagCode("CREATE_JSON_TAG")]);

		var fetchOptions = createExperimentFetchOptions();

		facade.createExperiments([experimentCreation], function(permIds) {
			facade.mapExperiments(permIds, fetchOptions, function(experiments) {
				assertObjectsCount(Object.keys(experiments), 1);

				var experiment = experiments[permIds[0].permId];
				equal(experiment.code, code, "Experiment code");
				equal(experiment.type.code, "UNKNOWN", "Type code");
				equal(experiment.project.code, "TEST-PROJECT", "Project code");
				equal(experiment.project.space.code, "TEST", "Space code");
				equal(experiment.tags[0].code, "CREATE_JSON_TAG", "Tag code");
				facade.close();
			}, function(error) {
				ok(false, error);
			});
		}, function(error) {
			ok(false, error);
		});
	});
});

test("createSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "CREATE_JSON_SAMPLE_" + (new Date().getTime());
		var creations = [ {
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
				"@type" : "TagCode",
				"code" : "CREATE_JSON_TAG"
			} ]
		} ];
		var fetchOptions = createSampleFetchOptions();

		facade.createSamples(creations, function(permIds) {
			facade.mapSamples(permIds, fetchOptions, function(samples) {
				assertObjectsCount(Object.keys(samples), 1);

				var sample = samples[permIds[0].permId];
				equal(sample.code, code, "Sample code");
				equal(sample.type.code, "UNKNOWN", "Type code");
				equal(sample.space.code, "TEST", "Space code");
				facade.close();
			}, function(error) {
				ok(false, error);
			});
		}, function(error) {
			ok(false, error);
		});
	});
});

test("updateExperiments()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "UPDATE_JSON_EXPERIMENT_" + (new Date().getTime());
		var creations = [ {
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
		} ];

		facade.createExperiments(creations, function(permIds) {
			var updates = [ {
				"@type" : "ExperimentUpdate",

				"experimentId" : permIds[0],

				"projectId" : {
					"@type" : "ProjectIdentifier",
					"identifier" : "/PLATONIC/SCREENING-EXAMPLES"
				}
			} ];

			facade.updateExperiments(updates, function() {
				var fetchOptions = createExperimentFetchOptions();

				facade.mapExperiments(permIds, fetchOptions, function(experiments) {
					assertObjectsCount(Object.keys(experiments), 1);

					var experiment = experiments[permIds[0].permId];
					equal(experiment.code, code, "Experiment code");
					equal(experiment.type.code, "UNKNOWN", "Type code");
					equal(experiment.project.code, "SCREENING-EXAMPLES", "Project code");
					equal(experiment.project.space.code, "PLATONIC", "Space code");
					facade.close();
				}, function(error) {
					ok(false, error);
				});
			}, function(error) {
				ok(false, error);
			});
		}, function(error) {
			ok(false, error);
		});
	});
});

test("updateSamples()", function() {
	createFacadeAndLogin(function(facade) {
		var code = "UPDATE_JSON_SAMPLE_" + (new Date().getTime());
		var creations = [ {
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
		} ];

		facade.createSamples(creations, function(permIds) {
			var updates = [ {
				"@type" : "SampleUpdate",

				"sampleId" : permIds[0],

				"spaceId" : {
					"@type" : "SpacePermId",
					"permId" : "TEST"
				}
			} ];

			facade.updateSamples(updates, function() {
				var fetchOptions = createSampleFetchOptions();

				facade.mapSamples(permIds, fetchOptions, function(samples) {
					assertObjectsCount(Object.keys(samples), 1);

					var sample = samples[permIds[0].permId];
					equal(sample.code, code, "Sample code");
					equal(sample.type.code, "UNKNOWN", "Type code");
					equal(sample.space.code, "TEST", "Space code");
					facade.close();
				}, function(error) {
					ok(false, error);
				});
			}, function(error) {
				ok(false, error);
			});
		}, function(error) {
			ok(false, error);
		});
	});
});
