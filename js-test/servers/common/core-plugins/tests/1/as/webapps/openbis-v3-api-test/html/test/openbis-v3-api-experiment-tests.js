define([ 'jquery', 'underscore', 'openbis', 'test/openbis-v3-api-test-common', 'dto/entity/experiment/ExperimentCreation', 'dto/id/entitytype/EntityTypePermId', 'dto/id/project/ProjectIdentifier',
		'dto/id/tag/TagCode', 'dto/entity/experiment/ExperimentUpdate', 'dto/entity/attachment/AttachmentCreation', 'dto/deletion/experiment/ExperimentDeletionOptions' ], function($, _, openbis, c,
		ExperimentCreation, EntityTypePermId, ProjectIdentifier, TagCode, ExperimentUpdate, AttachmentCreation, ExperimentDeletionOptions) {
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
				ok(false, error.message);
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
				ok(false, error.message);
				start();
			});
		});

		asyncTest("createAndDeleteAnExperiment()", function() {
			var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());
			var experimentCreation = new ExperimentCreation();
			experimentCreation.setTypeId(new EntityTypePermId("HT_SEQUENCING"));
			experimentCreation.setCode(code);
			experimentCreation.setProjectId(new ProjectIdentifier("/TEST/TEST-PROJECT"));
			experimentCreation.setTagIds([ new TagCode("CREATE_JSON_TAG") ]);
			attachmentCreation = new AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world!"));
			experimentCreation.setAttachments([ attachmentCreation ]);
			experimentCreation.setProperty("EXPERIMENT_DESIGN", "SEQUENCE_ENRICHMENT");

			$.when(c.createFacadeAndLogin(), c.createExperimentFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createExperiments([ experimentCreation ]).then(function(permIds) {
					return facade.mapExperiments(permIds, fetchOptions).done(function() {
						var identifier = c.createExperimentIdentifier("/TEST/TEST-PROJECT/" + code);
						var options = new ExperimentDeletionOptions();
						options.setReason("test");
						facade.deleteExperiments([ identifier ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							ok(false, error.message);
						});
					})
				})
			}).done(function(experiments) {
				var keys = Object.keys(experiments);
				assertObjectsCount(keys, 1);
				var experiment = experiments[keys[0]];
				equal(experiment.getCode(), code, "Experiment code");
				equal(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				equal(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				equal(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				equal(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
				var tags = experiment.getTags();
				equal(tags[0].code, 'CREATE_JSON_TAG', "tags");
				equal(tags.length, 1, "Number of tags");
				var attachments = experiment.getAttachments();
				equal(attachments[0].fileName, "test_file", "Attachment file name");
				equal(attachments[0].title, "test_title", "Attachment title");
				equal(attachments[0].description, "test_description", "Attachment description");
				equal(atob(attachments[0].content), "hello world!", "Attachment content");
				equal(attachments.length, 1, "Number of attachments");
				var properties = experiment.getProperties();
				equal(properties["EXPERIMENT_DESIGN"], "SEQUENCE_ENRICHMENT", "Property EXPERIMENT_DESIGN");
				equal(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});

		var asyncUpdateExperimentsTest = function(testNamePostfix, experimentUpdateModifier, experimentCheckerOrExpectedErrorMessage) {
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
					var ids = facade.createExperiments([ experimentCreation ]).then(function(permIds) {
						var experimentUpdate = new ExperimentUpdate();
						experimentUpdate.setExperimentId(permIds[0]);
						experimentUpdateModifier(experimentUpdate);
						return facade.updateExperiments([ experimentUpdate ]).then(function() {
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
							ok(false, error.message);
						}
						start();
					});
				});
			});
		}

		asyncUpdateExperimentsTest("WithChangedProjectAndAddedTagAndAttachment", function(experimentUpdate) {
			experimentUpdate.setProjectId(new ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
			experimentUpdate.getTagIds().add(new TagCode("CREATE_ANOTHER_JSON_TAG"));
			attachmentCreation = new AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world"));
			experimentUpdate.getAttachments().add([ attachmentCreation ]);
		}, function(code, experiment) {
			equal(experiment.getCode(), code, "Experiment code");
			equal(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
			equal(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
			equal(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
			var tags = _.sortBy(experiment.getTags(), "code");
			equal(tags[0].code, 'CREATE_ANOTHER_JSON_TAG', "tags");
			equal(tags[1].code, 'CREATE_JSON_TAG', "tags");
			equal(tags.length, 2, "Number of tags");
			var attachments = experiment.getAttachments();
			equal(attachments[0].fileName, "test_file", "Attachment file name");
			equal(attachments[0].title, "test_title", "Attachment title");
			equal(attachments[0].description, "test_description", "Attachment description");
			equal(atob(attachments[0].content), "hello world", "Attachment content");
			equal(attachments.length, 1, "Number of attachments");
		});

		asyncUpdateExperimentsTest("WithUnChangedProjectButChangedPropertiesAndRemovedTag", function(experimentUpdate) {
			experimentUpdate.setProperty("EXPERIMENT_DESIGN", "OTHER");
			experimentUpdate.getTagIds().remove([ new TagCode("UNKNOWN_TAG"), new TagCode("CREATE_JSON_TAG") ]);
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
