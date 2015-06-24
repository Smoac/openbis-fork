define([ 'jquery', 'underscore', 'openbis', 'test/common', 'dto/entity/experiment/ExperimentCreation', 'dto/id/entitytype/EntityTypePermId', 'dto/id/project/ProjectIdentifier', 'dto/id/tag/TagCode',
		'dto/entity/experiment/ExperimentUpdate', 'dto/entity/attachment/AttachmentCreation', 'dto/deletion/experiment/ExperimentDeletionOptions' ], function($, _, openbis, common,
		ExperimentCreation, EntityTypePermId, ProjectIdentifier, TagCode, ExperimentUpdate, AttachmentCreation, ExperimentDeletionOptions) {
	return function() {
		QUnit.module("Experiment tests");

		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createExperimentPermId("20130412105232616-2"), c.createExperimentFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapExperiments([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(experiments) {
				c.assertObjectsCount(Object.keys(experiments), 1);

				var experiment = experiments["20130412105232616-2"];
				c.assertEqual(experiment.getCode(), "EXP-1", "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("searchExperiments()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createExperimentSearchCriterion(), c.createExperimentFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST-EXPERIMENT-2");

				return facade.searchExperiments(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(experiments) {
				c.assertObjectsCount(experiments, 1);

				var experiment = experiments[0];
				c.assertEqual(experiment.getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("createAndDeleteAnExperiment()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

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

			$.when(c.createFacadeAndLogin(), c.createExperimentIdentifier("/TEST/TEST-PROJECT/" + code), c.createExperimentFetchOptions()).then(function(facade, identifier, fetchOptions) {
				return facade.createExperiments([ experimentCreation ]).then(function(permIds) {
					return facade.mapExperiments(permIds, fetchOptions).done(function() {
						var options = new ExperimentDeletionOptions();
						options.setReason("test");
						facade.deleteExperiments([ identifier ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							c.fail(error.message);
							done();
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
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		var asyncUpdateExperimentsTest = function(testNamePostfix, experimentUpdateModifier, experimentCheckerOrExpectedErrorMessage) {
			QUnit.test("updateExperiments" + testNamePostfix + "()", function(assert) {
				var c = new common(assert);
				var done = assert.async();

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
						done();
					}).fail(function(error) {
						if (expectingFailure) {
							c.assertEqual(error.message, experimentCheckerOrExpectedErrorMessage, "Error message");
						} else {
							c.fail(error.message);
						}
						done();
					});
				});
			});
		}

		asyncUpdateExperimentsTest("WithChangedProjectAndAddedTagAndAttachment", function(c, experimentUpdate) {
			experimentUpdate.setProjectId(new ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
			experimentUpdate.getTagIds().add(new TagCode("CREATE_ANOTHER_JSON_TAG"));
			attachmentCreation = new AttachmentCreation();
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
			experimentUpdate.getTagIds().remove([ new TagCode("UNKNOWN_TAG"), new TagCode("CREATE_JSON_TAG") ]);
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
