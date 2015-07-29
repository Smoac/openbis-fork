define([ 'jquery', 'openbis', 'test/common' ], function($, openbis, common) {
	return function() {
		QUnit.module("Sample tests");

		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.mapSamples([ new c.SamplePermId("20130415095748527-404") ], c.createSampleFetchOptions()).done(function() {
					facade.logout()
				})
			}).done(function(samples) {
				c.assertObjectsCount(Object.keys(samples), 1);
				var sample = samples["20130415095748527-404"];
				c.assertEqual(sample.code, "TEST-SAMPLE-2-PARENT", "Sample code");
				c.assertEqual(sample.type.code, "UNKNOWN", "Type code");
				c.assertEqual(sample.experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(sample.experiment.project.code, "TEST-PROJECT", "Project code");
				c.assertEqual(sample.space.code, "TEST", "Space code");
				c.assertNotEqual(sample.children, null, "Children expected");
				if (sample.children !== null) {
					console.log("Children %s", sample.children);
					var child = sample.children[0];
					c.assertEqual(sample.children.length, 1, "Number of children");
					c.assertEqual(child.code, "TEST-SAMPLE-2", "Child sample code");
					c.assertEqual(child.type.code, "UNKNOWN", "Child type code");
					c.assertEqual(child.experiment.code, "TEST-EXPERIMENT-2", "Child experiment code");
					c.assertNotEqual(child.children, null, "Grand children expected");
					if (child.children !== null) {
						c.assertEqual(child.children.length, 2, "Number of grand children");
					}
				}
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchSamples()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				var criterion = new c.SampleSearchCriterion();
				criterion.withCode().thatEquals("PLATE-1");

				return facade.searchSamples(criterion, c.createSampleFetchOptions()).done(function() {
					facade.logout();
				})
			}).done(function(samples) {
				c.assertObjectsCount(samples, 1);

				var sample = samples[0];
				c.assertEqual(sample.getCode(), "PLATE-1", "Sample code");
				c.assertEqual(sample.getType().getCode(), "PLATE", "Type code");
				c.assertEqual(sample.getExperiment().getCode(), "EXP-1", "Experiment code");
				c.assertEqual(sample.getExperiment().getProject().getCode(), "SCREENING-EXAMPLES", "Project  code");
				c.assertEqual(sample.getSpace().getCode(), "PLATONIC", "Space code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("createSamples()", function(assert) {
			var c = new common(assert);
			c.start();

			var creation = new c.SampleCreation();
			creation.setTypeId(new c.EntityTypePermId("UNKNOWN"));
			creation.setCode("CREATE_JSON_SAMPLE_" + (new Date().getTime()));
			creation.setSpaceId(new c.SpacePermId("TEST"));
			creation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createSamples([ creation ]).then(function(permIds) {
					return facade.mapSamples(permIds, c.createSampleFetchOptions()).done(function() {
						facade.logout();
					})
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				c.assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				c.assertEqual(sample.getCode(), creation.getCode(), "Sample code");
				c.assertEqual(sample.getType().getCode(), creation.getTypeId().getPermId(), "Type code");
				c.assertEqual(sample.getSpace().getCode(), creation.getSpaceId().getPermId(), "Space code");
				c.assertEqual(sample.getTags()[0].getCode(), creation.getTagIds()[0].getCode(), "Tag code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("updateSamples()", function(assert) {
			var c = new common(assert);
			c.start();

			var creation = new c.SampleCreation();
			creation.setTypeId(new c.EntityTypePermId("UNKNOWN"));
			creation.setCode("CREATE_JSON_SAMPLE_" + (new Date().getTime()));
			creation.setSpaceId(new c.SpacePermId("TEST"));
			creation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createSamples([ creation ]).then(function(permIds) {

					var update = new c.SampleUpdate();
					update.setSampleId(permIds[0]);
					update.getTagIds().remove(new c.TagCode("CREATE_JSON_TAG"));
					update.getTagIds().add(new c.TagCode("CREATE_JSON_TAG_2"));
					update.getTagIds().add(new c.TagCode("CREATE_JSON_TAG_3"));

					return facade.updateSamples([ update ]).then(function() {
						return facade.mapSamples(permIds, c.createSampleFetchOptions()).done(function() {
							facade.logout();
						})
					});
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				c.assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				c.assertEqual(sample.getCode(), creation.getCode(), "Sample code");
				c.assertEqual(sample.getType().getCode(), creation.getTypeId().getPermId(), "Type code");
				c.assertEqual(sample.getSpace().getCode(), creation.getSpaceId().getPermId(), "Space code");
				c.assertObjectsCount(sample.getTags(), 2);
				c.assertObjectsWithValues(sample.getTags(), "code", [ "CREATE_JSON_TAG_2", "CREATE_JSON_TAG_3" ]);
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("deleteSamples()", function(assert) {
			var c = new common(assert);
			c.start();

			var creation = new c.SampleCreation();
			creation.setTypeId(new c.EntityTypePermId("UNKNOWN"));
			creation.setCode("CREATE_JSON_SAMPLE_" + (new Date().getTime()));
			creation.setSpaceId(new c.SpacePermId("TEST"));

			var deletion = new c.SampleDeletionOptions();
			deletion.setReason("test reason");

			$.when(c.createFacadeAndLogin(), c.createSampleFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createSamples([ creation ]).then(function(permIds) {
					return facade.deleteSamples(permIds, deletion).then(function() {
						return facade.mapSamples(permIds, fetchOptions).done(function() {
							facade.logout();
						})
					});
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				c.assertObjectsCount(keys, 0);
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

	}
});
