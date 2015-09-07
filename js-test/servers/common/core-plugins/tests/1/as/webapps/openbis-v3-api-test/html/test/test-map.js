define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Map tests");

		var testMap = function(c, fCreate, fMap) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length > 0, "Entities were created");
					return fMap(facade, permIds).then(function(map) {
						c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
						permIds.forEach(function(permId) {
							var entity = map[permId];
							c.assertEqual(entity.getPermId().toString(), permId.toString(), "Entity perm id matches");
						});
						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				return facade.mapSpaces(permIds, new c.SpaceFetchOptions());
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				return facade.mapProjects(permIds, new c.ProjectFetchOptions());
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				return facade.mapExperiments(permIds, new c.ExperimentFetchOptions());
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				return facade.mapSamples(permIds, new c.SampleFetchOptions());
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				var result = facade.mapDataSets(permIds, new c.DataSetFetchOptions());
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}

			testMap(c, fCreate, fMap);
		});

		QUnit.test("mapMaterials()", function(assert) {
			var c = new common(assert);

			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fMap = function(facade, permIds) {
				return facade.mapMaterials(permIds, new c.MaterialFetchOptions());
			}

			testMap(c, fCreate, fMap);
		});

	}
});
