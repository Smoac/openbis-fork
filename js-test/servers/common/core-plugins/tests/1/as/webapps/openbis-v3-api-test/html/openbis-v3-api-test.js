define(['jquery', 'openbis-v3-api'], function($, openbis){
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
	
		action(facade);
	}
	
	var createFacadeAndLogin = function() {
		var dfd = $.Deferred();

		createFacade(function(facade) {
			facade
				.login(testUserId, testUserPassword)
				.done(function() {
					dfd.resolve(facade);
					start();
				}).
				fail(function() {
					dfd.reject(arguments);
					start();
				});
		});
		
		
		return dfd.promise();
	}
	
	var createExperimentFetchOptions = function() {
		var dfd = $.Deferred();
		
		require(['dto/fetchoptions/experiment/ExperimentFetchOptions'], function(efo) {
			var fo = new efo;
			fo.withType();
			fo.withProject().withSpace();
			fo.withProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments();
			
			dfd.resolve(fo);
		}); 
		
		return dfd.promise();
	}
	
	var createSampleFetchOptions = function() {
		var dfd = $.Deferred();
		
		require(['dto/fetchoptions/sample/SampleFetchOptions'], function(sfo) {
			var fo = new sfo;
			fo.withType();
			fo.withExperiment().withProject().withSpace();
			fo.withSpace();
			fo.withProperties();
			fo.withTags();
			fo.withRegistrator();
			fo.withModifier();
			fo.withAttachments();
			dfd.resolve(fo);
		});
		return dfd.promise();
	}
	
	return function() {
		asyncTest("mapExperiments()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
				var experimentIds = [ {
					"@type" : "ExperimentPermId",
					"permId" : "20130412105232616-2"
				} ];
				
				createExperimentFetchOptions()
				.done(function(fetchOptions) {
					facade
					.mapExperiments(experimentIds, fetchOptions)
					.done(function(experiments) {
						assertObjectsCount(Object.keys(experiments), 1);
			
						var experiment = experiments["20130412105232616-2"];
						equal(experiment.getCode(), "EXP-1", "Experiment code");
						equal(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
						equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
						equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
						facade.logout();
						start();
					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
				});
			});
		});
		
		asyncTest("mapSamples()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
				var sampleIds = [ {
					"@type" : "SamplePermId",
					"permId" : "20130412140147735-20"
				} ];
				createSampleFetchOptions()
				.done(function(fetchOptions) {
					facade
					.mapSamples(sampleIds, fetchOptions)
					.done(function(samples) {
						assertObjectsCount(Object.keys(samples), 1);
			
						var sample = samples["20130412140147735-20"];
						equal(sample.code, "PLATE-1", "Sample code");
						equal(sample.type.code, "PLATE", "Type code");
						equal(sample.experiment.code, "EXP-1", "Experiment code");
						equal(sample.experiment.project.code, "SCREENING-EXAMPLES", "Project code");
						equal(sample.space.code, "PLATONIC", "Space code");
						facade.logout();
						start();
					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
					
				});
			});
		});
		
		asyncTest("searchExperiments()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
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
				createExperimentFetchOptions()
				.done(function(fetchOptions) {
					facade
					.searchExperiments(criterion, fetchOptions)
					.done(function(experiments) {
						assertObjectsCount(experiments, 1);
			
						var experiment = experiments[0];
						equal(experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
						equal(experiment.type.code, "UNKNOWN", "Type code");
						equal(experiment.project.code, "TEST-PROJECT", "Project code");
						equal(experiment.project.space.code, "TEST", "Space code");
						facade.logout();
						start();
					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
				});
			});
		});
		
		asyncTest("searchSamples()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
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
				createSampleFetchOptions()
				.done(function(fetchOptions) {
		
					facade
					.searchSamples(criterion, fetchOptions)
					.done(function(samples) {
						assertObjectsCount(samples, 1);
			
						var sample = samples[0];
						equal(sample.code, "PLATE-1", "Sample code");
						equal(sample.type.code, "PLATE", "Type code");
						equal(sample.experiment.code, "EXP-1", "Experiment code");
						equal(sample.experiment.project.code, "SCREENING-EXAMPLES", "Project  code");
						equal(sample.space.code, "PLATONIC", "Space code");
						facade.logout();
						start();
					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
				});
			});
		});
		
		asyncTest("createExperiments()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
				var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());
		//		var creations = [ {
		//			"@type" : "ExperimentCreation",
		//
		//			"typeId" : {
		//				"@type" : "EntityTypePermId",
		//				"permId" : "UNKNOWN"
		//			},
		//
		//			"code" : code,
		//
		//			"projectId" : {
		//				"@type" : "ProjectIdentifier",
		//				"identifier" : "/TEST/TEST-PROJECT"
		//			},
		//
		//			"tagIds" : [ {
		//				"@type" : "TagCode",
		//				"code" : "CREATE_JSON_TAG"
		//			} ]
		//		} ];
				require(['dto/entity/experiment/ExperimentCreation', 
				         'dto/id/entitytype/EntityTypePermId', 
				         'dto/id/project/ProjectIdentifier',
				         'dto/id/tag/TagCode'], function(ExperimentCreation, EntityTypePermId, ProjectIdentifier, TagCode) {
					var experimentCreation = new ExperimentCreation();
					experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
					experimentCreation.setCode(code);
					experimentCreation.setProjectId(new ProjectIdentifier("/TEST/TEST-PROJECT"));
					experimentCreation.setTagIds([new TagCode("CREATE_JSON_TAG")]);
			
					createExperimentFetchOptions()
					.done(function(fetchOptions) {
				
						facade.createExperiments([experimentCreation])
						.done(function(permIds) {
							facade
							.mapExperiments(permIds, fetchOptions)
							.done(function(experiments) {
								assertObjectsCount(Object.keys(experiments), 1);
				
								var experiment = experiments[permIds[0].permId];
								equal(experiment.getCode(), code, "Experiment code");
								equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
								equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
								equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
								equal(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
								facade.logout();
								start();
							})
							.fail(function(error) {
								ok(false, error);
								start();
							});
						})
						.fail(function(error) {
							ok(false, error);
							start();
						});
					});
				});
			});
		});
		
		asyncTest("createSamples()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
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
				createSampleFetchOptions()
				.done(function(fetchOptions) {
		
					facade
					.createSamples(creations)
					.done(function(permIds) {
						facade
						.mapSamples(permIds, fetchOptions)
						.done(function(samples) {
							assertObjectsCount(Object.keys(samples), 1);
			
							var sample = samples[permIds[0].permId];
							equal(sample.code, code, "Sample code");
							equal(sample.type.code, "UNKNOWN", "Type code");
							equal(sample.space.code, "TEST", "Space code");
							facade.logout();
							start();
						})
						.fail(function(error) {
							ok(false, error);
							start();
						});
					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
				});
			});
		});
		
		asyncTest("updateExperiments()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
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
		
				facade
				.createExperiments(creations)
				.done(function(permIds) {
					var updates = [ {
						"@type" : "ExperimentUpdate",
		
						"experimentId" : permIds[0],
		
						"projectId" : {
							"@type" : "ProjectIdentifier",
							"identifier" : "/PLATONIC/SCREENING-EXAMPLES"
						}
					} ];
		
					facade
					.updateExperiments(updates)
					.done(function() {
						createExperimentFetchOptions()
						.done(function(fetchOptions) {
		
							facade
							.mapExperiments(permIds, fetchOptions)
							.done(function(experiments) {
								assertObjectsCount(Object.keys(experiments), 1);
			
								var experiment = experiments[permIds[0].permId];
								equal(experiment.getCode(), code, "Experiment code");
								equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
								equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
								equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
								facade.logout();
								start();
							})
							.fail(function(error) {
								ok(false, error);
								start();
							});
						});

					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
				}).
				fail(function(error) {
					ok(false, error);
					start();
				});
			});
		});
		
		asyncTest("updateSamples()", function() {
			createFacadeAndLogin()
			.done(function(facade) {
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
		
				facade
				.createSamples(creations)
				.done(function(permIds) {
					var updates = [ {
						"@type" : "SampleUpdate",
		
						"sampleId" : permIds[0],
		
						"spaceId" : {
							"@type" : "SpacePermId",
							"permId" : "TEST"
						}
					} ];
		
					facade
					.updateSamples(updates)
					.done(function() {
						createSampleFetchOptions()
						.done(function(fetchOptions) {
		
							facade.mapSamples(permIds, fetchOptions)
							.done(function(samples) {
								assertObjectsCount(Object.keys(samples), 1);
			
								var sample = samples[permIds[0].permId];
								equal(sample.code, code, "Sample code");
								equal(sample.type.code, "UNKNOWN", "Type code");
								equal(sample.space.code, "TEST", "Space code");
								facade.logout();
								start();
							})
							.fail(function(error) {
								ok(false, error);
								start();
							});
						});
					})
					.fail(function(error) {
						ok(false, error);
						start();
					});
				})
				.fail(function(error) {
					ok(false, error);
					start();
				});
			});
		});
	}
});