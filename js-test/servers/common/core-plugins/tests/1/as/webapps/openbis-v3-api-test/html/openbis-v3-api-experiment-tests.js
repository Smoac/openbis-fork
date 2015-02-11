define([ 'jquery', 'support/underscore-min', 'openbis-v3-api', 'openbis-v3-api-test-common', 
         'dto/entity/experiment/ExperimentCreation', 'dto/id/entitytype/EntityTypePermId', 
         'dto/id/project/ProjectIdentifier', 'dto/id/tag/TagCode', 'dto/entity/experiment/ExperimentUpdate'
         ], 
function($, _, openbis, c, 
		ExperimentCreation, EntityTypePermId, 
		ProjectIdentifier, TagCode, ExperimentUpdate) {
	return function() {
		QUnit.module("Experiment tests");
		
		asyncTest("mapExperiments()", function() {
			$.when(c.createFacadeAndLogin(), c.createExperimentPermId("20130412105232616-2"), c.createExperimentFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapExperiments([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(experiments) {
				assertObjectsCount(Object.keys(experiments), 1);

				var experiment = experiments["20130412105232616-2"];
				equal(experiment.getCode(), "EXP-1", "Experiment code");
				equal(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
				equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("searchExperiments()", function() {
			$.when(c.createFacadeAndLogin(), c.createExperimentSearchCriterion(), c.createExperimentFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST-EXPERIMENT-2");

				return facade.searchExperiments(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(experiments) {
				assertObjectsCount(experiments, 1);

				var experiment = experiments[0];
				equal(experiment.getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
				equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});
		
		asyncTest("createExperiments()", function() {
			var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());
			var experimentCreation = new ExperimentCreation();
			experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
			experimentCreation.setCode(code);
			experimentCreation.setProjectId(new ProjectIdentifier("/TEST/TEST-PROJECT"));
			experimentCreation.setTagIds([ new TagCode("CREATE_JSON_TAG") ]);

			$.when(c.createFacadeAndLogin(), c.createExperimentFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createExperiments([ experimentCreation ]).then(function(permIds) {
					return facade.mapExperiments(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(experiments) {
				var keys = Object.keys(experiments);
				assertObjectsCount(keys, 1);
				var experiment = experiments[keys[0]];
				equal(experiment.getCode(), code, "Experiment code");
				equal(experiment.getType().getCode(), "UNKNOWN", "Type code");
				equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				equal(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		var asyncUpdateExperimentsTest = function(testNamePostfix, experimentUpdateModifier, 
				experimentCheckerOrExpectedErrorMessage) {
			asyncTest("updateExperiments" + testNamePostfix + "()", function() {
				var expectingFailure = _.isFunction(experimentCheckerOrExpectedErrorMessage) === false;
				var code = "UPDATE_JSON_EXPERIMENT_" + (new Date().getTime());
				var experimentCreation = new ExperimentCreation();
				experimentCreation.setTypeId(new EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setCode(code);
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "EXPRESSION");
				experimentCreation.setTagIds([ new TagCode("CREATE_JSON_TAG") ]);
				experimentCreation.setProjectId(new ProjectIdentifier("/TEST/TEST-PROJECT"));
				c.createFacadeAndLogin().then(function(facade) {
					var ids = facade.createExperiments([experimentCreation]).then(function(permIds) {
						var experimentUpdate = new ExperimentUpdate();
						experimentUpdate.setExperimentId(permIds[0]);
						experimentUpdateModifier(experimentUpdate);
						return facade.updateExperiments([experimentUpdate]).then(function() {
							return permIds;
						});
					});
					$.when(ids, c.createExperimentFetchOptions()).then(function(permIds, fetchOptions) {
						return facade.mapExperiments(permIds, fetchOptions).done(function() {
							facade.logout();
						});
					}).done(function(experiments) {
						if (expectingFailure) {
							ok(false, "Experiment update didn't failed as expected.");
						} else {
							var keys = Object.keys(experiments);
							assertObjectsCount(keys, 1);
							var experiment = experiments[keys[0]];
							experimentCheckerOrExpectedErrorMessage(code, experiment);
						}
						start();
					}).fail(function(error) {
						if (expectingFailure) {
							equal(error.message, experimentCheckerOrExpectedErrorMessage, "Error message");
						} else {
							ok(false, error);
						}
						start();
					});
				});
			});
		}
			
		asyncUpdateExperimentsTest("WithChangedProjectAndAddedTag", function(experimentUpdate) {
			experimentUpdate.setProjectId(new ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
			experimentUpdate.getTagIds().add(new TagCode("CREATE_ANOTHER_JSON_TAG"));
		}, function(code, experiment) {
			equal(experiment.getCode(), code, "Experiment code");
			equal(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
			equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
			equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
			var tags = _.sortBy(experiment.getTags(), "code");
			equal(tags[0].code, 'CREATE_ANOTHER_JSON_TAG', "tags");
			equal(tags[1].code, 'CREATE_JSON_TAG', "tags");
			equal(tags.length, 2, "Number of tags");
		});
		
		asyncUpdateExperimentsTest("WithUnChangedProjectButChangedPropertiesAndRemovedTag", function(experimentUpdate) {
			experimentUpdate.setProperty("EXPERIMENT_DESIGN", "OTHER");
			experimentUpdate.getTagIds().remove([new TagCode("UNKNOWN_TAG"), new TagCode("CREATE_JSON_TAG")]);
		}, function(code, experiment) {
			equal(experiment.getCode(), code, "Experiment code");
			equal(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
			equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
			equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
			var properties = experiment.getProperties();
			equal(properties["EXPERIMENT_DESIGN"], "OTHER", "Property EXPERIMENT_DESIGN");
			equal(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
			equal(experiment.getTags().length, 0, "Number of tags");
		});
		
		asyncUpdateExperimentsTest("WithRemovedProject", function(experimentUpdate) {
			experimentUpdate.setProjectId(null);
		}, "Project id cannot be null (Context: [])");
		
	}
});
