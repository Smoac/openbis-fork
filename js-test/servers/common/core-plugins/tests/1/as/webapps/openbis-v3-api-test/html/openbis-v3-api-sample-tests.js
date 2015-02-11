define([ 'jquery', 'openbis-v3-api', 'openbis-v3-api-test-common', 'dto/entity/sample/SampleCreation', 
         'dto/id/entitytype/EntityTypePermId', 'dto/id/space/SpacePermId', 'dto/id/tag/TagCode'
         ], 
function($, openbis, c, SampleCreation,
		EntityTypePermId, SpacePermId, TagCode) {
	return function() {
		QUnit.module("Sample tests");
		
		asyncTest("mapSamples()", function() {
			$.when(c.createFacadeAndLogin(), c.createSamplePermId("20130415095748527-404"), c.createSampleFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapSamples([ permId ], fetchOptions).done(function() {
					facade.logout()
				})
			}).done(function(samples) {
				assertObjectsCount(Object.keys(samples), 1);
				var sample = samples["20130415095748527-404"];
				equal(sample.code, "TEST-SAMPLE-2-PARENT", "Sample code");
				equal(sample.type.code, "UNKNOWN", "Type code");
				equal(sample.experiment.code, "TEST-EXPERIMENT-2", "Experiment code");
				equal(sample.experiment.project.code, "TEST-PROJECT", "Project code");
				equal(sample.space.code, "TEST", "Space code");
				notEqual(sample.children, null, "Children expected");
				if (sample.children !== null) {
					console.log("Children %s", sample.children);
					var child = sample.children[0];
					equal(sample.children.length, 1, "Number of children");
					equal(child.code, "TEST-SAMPLE-2", "Child sample code");
					equal(child.type.code, "UNKNOWN", "Child type code");
					equal(child.experiment.code, "TEST-EXPERIMENT-2", "Child experiment code");
					notEqual(child.children, null, "Grand children expected");
					if (child.children !== null) {
						equal(child.children.length, 2, "Number of grand children");
					}
				}
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});

		asyncTest("searchSamples()", function() {
			$.when(c.createFacadeAndLogin(), c.createSampleSearchCriterion(), c.createSampleFetchOptions()).then(function(facade, criterion, fetchOptions) {
				
				criterion.withCode().thatEquals("PLATE-1");

				return facade.searchSamples(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(samples) {
				assertObjectsCount(samples, 1);

				var sample = samples[0];
				equal(sample.getCode(), "PLATE-1", "Sample code");
				equal(sample.getType().getCode(), "PLATE", "Type code");
				equal(sample.getExperiment().getCode(), "EXP-1", "Experiment code");
				equal(sample.getExperiment().getProject().getCode(), "SCREENING-EXAMPLES", "Project  code");
				equal(sample.getSpace().getCode(), "PLATONIC", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});
		
		asyncTest("createSamples()", function() {
			var creation = new SampleCreation();
			creation.setTypeId(new EntityTypePermId("UNKNOWN"));
			creation.setCode("CREATE_JSON_SAMPLE_" + (new Date().getTime()));
			creation.setSpaceId(new SpacePermId("TEST"));
			creation.setTagIds([new TagCode("CREATE_JSON_TAG")]);
			
			$.when(c.createFacadeAndLogin(), c.createSampleFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createSamples([creation]).then(function(permIds) {
					return facade.mapSamples(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				equal(sample.getCode(), creation.getCode(), "Sample code");
				equal(sample.getType().getCode(), creation.getTypeId().getPermId(), "Type code");
				equal(sample.getSpace().getCode(), creation.getSpaceId().getPermId(), "Space code");
				equal(sample.getTags()[0].getCode(), creation.getTagIds()[0].getCode(), "Tag code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});
		
/*
		asyncTest("updateSamples()", function() {
			var code = "UPDATE_JSON_SAMPLE_" + (new Date().getTime());

			createFacadeAndLogin().then(function(facade) {
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

				var ids = facade.createSamples(creations).then(function(permIds) {
					var updates = [ {
						"@type" : "SampleUpdate",

						"sampleId" : permIds[0],

						"spaceId" : {
							"@type" : "SpacePermId",
							"permId" : "TEST"
						}
					} ];

					return facade.updateSamples(updates).then(function() {
						return permIds;
					});
				});

				return $.when(ids, createSampleFetchOptions()).then(function(permIds, fetchOptions) {
					return facade.mapSamples(permIds, fetchOptions).done(function() {
						facade.logout();
					})
				})
			}).done(function(samples) {
				var keys = Object.keys(samples);
				assertObjectsCount(keys, 1);

				var sample = samples[keys[0]];
				equal(sample.code, code, "Sample code");
				equal(sample.type.code, "UNKNOWN", "Type code");
				equal(sample.space.code, "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error);
				start();
			});
		});
 */
		
	}
});
