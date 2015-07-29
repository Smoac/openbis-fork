define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Experiment tests");

		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.mapExperiments([ new c.ExperimentPermId("20130412105232616-2") ], c.createExperimentFetchOptions()).done(function() {
					facade.logout()
				});
			}).done(function(experiments) {
				c.assertObjectsCount(Object.keys(experiments), 1);

				var experiment = experiments["20130412105232616-2"];
				c.assertEqual(experiment.getCode(), "EXP-1", "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchExperiments()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				var criterion = new c.ExperimentSearchCriterion();
				criterion.withCode().thatEquals("TEST-EXPERIMENT-2");

				return facade.searchExperiments(criterion, c.createExperimentFetchOptions()).done(function() {
					facade.logout();
				})
			}).done(function(experiments) {
				c.assertObjectsCount(experiments, 1);

				var experiment = experiments[0];
				c.assertEqual(experiment.getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("createAndDeleteAnExperiment()", function(assert) {
			var c = new common(assert);
			c.start();

			var code = "CREATE_JSON_EXPERIMENT_" + (new Date().getTime());
			var experimentCreation = new c.ExperimentCreation();
			experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
			experimentCreation.setCode(code);
			experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
			experimentCreation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
			attachmentCreation = new c.AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world!"));
			experimentCreation.setAttachments([ attachmentCreation ]);
			experimentCreation.setProperty("EXPERIMENT_DESIGN", "SEQUENCE_ENRICHMENT");

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createExperiments([ experimentCreation ]).then(function(permIds) {
					return facade.mapExperiments(permIds, c.createExperimentFetchOptions()).done(function() {
						var options = new c.ExperimentDeletionOptions();
						options.setReason("test");
						facade.deleteExperiments([ new c.ExperimentIdentifier("/TEST/TEST-PROJECT/" + code) ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							c.fail(error.message);
							c.finish();
						});
					})
				})
			}).done(function(experiments) {
				var keys = Object.keys(experiments);
				c.assertObjectsCount(keys, 1);
				var experiment = experiments[keys[0]];
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(experiment.getTags()[0].code, "CREATE_JSON_TAG", "Tag code");
				var tags = experiment.getTags();
				c.assertEqual(tags[0].code, 'CREATE_JSON_TAG', "tags");
				c.assertEqual(tags.length, 1, "Number of tags");
				var attachments = experiment.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
				var properties = experiment.getProperties();
				c.assertEqual(properties["EXPERIMENT_DESIGN"], "SEQUENCE_ENRICHMENT", "Property EXPERIMENT_DESIGN");
				c.assertEqual(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		var asyncUpdateExperimentsTest = function(testNamePostfix, experimentUpdateModifier, experimentCheckerOrExpectedErrorMessage) {
			QUnit.test("updateExperiments" + testNamePostfix + "()", function(assert) {
				var c = new common(assert);
				c.start();

				var expectingFailure = _.isFunction(experimentCheckerOrExpectedErrorMessage) === false;
				var code = "UPDATE_JSON_EXPERIMENT_" + (new Date().getTime());
				var experimentCreation = new c.ExperimentCreation();
				experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setCode(code);
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "EXPRESSION");
				experimentCreation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
				c.createFacadeAndLogin().then(function(facade) {
					var ids = facade.createExperiments([ experimentCreation ]).then(function(permIds) {
						var experimentUpdate = new c.ExperimentUpdate();
						experimentUpdate.setExperimentId(permIds[0]);
						experimentUpdateModifier(c, experimentUpdate);
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
							c.fail("Experiment update didn't failed as expected.");
						} else {
							var keys = Object.keys(experiments);
							c.assertObjectsCount(keys, 1);
							var experiment = experiments[keys[0]];
							experimentCheckerOrExpectedErrorMessage(c, code, experiment);
						}
						c.finish();
					}).fail(function(error) {
						if (expectingFailure) {
							c.assertEqual(error.message, experimentCheckerOrExpectedErrorMessage, "Error message");
						} else {
							c.fail(error.message);
						}
						c.finish();
					});
				});
			});
		}

		asyncUpdateExperimentsTest("WithChangedProjectAndAddedTagAndAttachment", function(c, experimentUpdate) {
			experimentUpdate.setProjectId(new c.ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
			experimentUpdate.getTagIds().add(new c.TagCode("CREATE_ANOTHER_JSON_TAG"));
			attachmentCreation = new c.AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world"));
			experimentUpdate.getAttachments().add([ attachmentCreation ]);
		}, function(c, code, experiment) {
			c.assertEqual(experiment.getCode(), code, "Experiment code");
			c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
			c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
			c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
			var tags = _.sortBy(experiment.getTags(), "code");
			c.assertEqual(tags[0].code, 'CREATE_ANOTHER_JSON_TAG', "tags");
			c.assertEqual(tags[1].code, 'CREATE_JSON_TAG', "tags");
			c.assertEqual(tags.length, 2, "Number of tags");
			var attachments = experiment.getAttachments();
			c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
			c.assertEqual(attachments[0].title, "test_title", "Attachment title");
			c.assertEqual(attachments[0].description, "test_description", "Attachment description");
			c.assertEqual(atob(attachments[0].content), "hello world", "Attachment content");
			c.assertEqual(attachments.length, 1, "Number of attachments");
		});

		asyncUpdateExperimentsTest("WithUnChangedProjectButChangedPropertiesAndRemovedTag", function(c, experimentUpdate) {
			experimentUpdate.setProperty("EXPERIMENT_DESIGN", "OTHER");
			experimentUpdate.getTagIds().remove([ new c.TagCode("UNKNOWN_TAG"), new c.TagCode("CREATE_JSON_TAG") ]);
		}, function(c, code, experiment) {
			c.assertEqual(experiment.getCode(), code, "Experiment code");
			c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
			c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
			c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
			var properties = experiment.getProperties();
			c.assertEqual(properties["EXPERIMENT_DESIGN"], "OTHER", "Property EXPERIMENT_DESIGN");
			c.assertEqual(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
			c.assertEqual(experiment.getTags().length, 0, "Number of tags");
		});

		asyncUpdateExperimentsTest("WithRemovedProject", function(c, experimentUpdate) {
			experimentUpdate.setProjectId(null);
		}, "Project id cannot be null (Context: [])");

	}
});
