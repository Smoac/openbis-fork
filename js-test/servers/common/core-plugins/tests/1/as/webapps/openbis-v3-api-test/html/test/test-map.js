define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Map tests");
		
		var testMap = function(c, fCreate, fMap, fechOptionsTestConfig) {
			c.start();
			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length > 0, "Entities were created");
					return fMap(facade, permIds).then(function(map) {
						c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
						permIds.forEach(function(permId) {
							var entity = map[permId];
							testFetchOptionsResults(c, fechOptionsTestConfig, true, entity);
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
		
		var testFetchOptionsAssignation = function(c, fo, toTest) {
			for(property in toTest) {
				if(property === "SortBy") {
					fo.sortBy().code();
					c.assertEqual(true, ((fo.getSortBy())?true:false));
				} else {
					var methodNameWithUsing = "with" + property + "Using";
					if(typeof fo[methodNameWithUsing] === "function") {
						fo[methodNameWithUsing](null);
					} else {
						throw methodNameWithUsing + " should be a method.";
					}
					
					var methodNameWith = "with" + property;
					if(typeof fo[methodNameWith] === "function") {
						fo[methodNameWith]();
					} else {
						throw methodNameWith + " should be a method.";
					}
					
					var methodNameHas = "has" + property;
					if(typeof fo[methodNameHas] === "function") {
						c.assertEqual(true, fo[methodNameHas]());
					} else {
						throw methodNameHas + " should be a method.";
					}
				}
			}
		}
		
		var testFetchOptionsResults = function(c, toTest, expectedShouldSucceed, entity) {
			for(property in toTest) {
				if(property !== "SortBy") {
					var methodName = "get" + property;
					if(typeof entity[methodName] === "function") {
						try {
							var result = entity[methodName](); //Should not thrown an exception, what it means is right!
							if(!expectedShouldSucceed) {
								throw "Calling method " + methodName + " succeed when it should thrown an exception for entity type " + entity.toString() + ".";
							}
						} catch(error) {
							if(expectedShouldSucceed) {
								throw "Calling method " + methodName + " thrown an exception when it should succeed for entity type " + entity.toString() + ".";
							}
						}
					} else {
						throw methodName + " should be a method.";
					}
				}
			}
		}
		
		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);
			var fechOptionsTestConfig = {
					Registrator : null,
					Projects : null,
					Samples : null,
					SortBy : null
			}
			
			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.SpaceFetchOptions();
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapSpaces(permIds, fo);
			}
			
			testMap(c, fCreate, fMap, fechOptionsTestConfig);
		});
		
		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);
			var fechOptionsTestConfig = {
					Experiments : null,
					Space : null,
					Leader : null,
					Modifier : null,
					Attachments : null,
					Registrator : null,
					Samples : null,
					SortBy : null,
					History : null
			};
			
			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.ProjectFetchOptions();
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapProjects(permIds, fo);
			}
			
			testMap(c, fCreate, fMap, fechOptionsTestConfig);
		});
		
		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);
			var fechOptionsTestConfig = {
					Type : null,
					Project : null,
					DataSets : null,
					Properties : null,
					MaterialProperties : null,
					Tags : null,
					Samples : null,
					History : null,
					Registrator : null,
					Modifier : null,
					Attachments : null,
					SortBy : null
			};
			
			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.ExperimentFetchOptions();
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapExperiments(permIds, fo);
			}
			
			testMap(c, fCreate, fMap, fechOptionsTestConfig);
		});
		
		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);
			var fechOptionsTestConfig = {
					Parents : null,
					Children : null,
					Container : null,
					Components : null,
					Type : null,
					Project : null,
					Space : null,
					Experiment : null,
					Properties : null,
					MaterialProperties : null,
					DataSets : null,
					History : null,
					SortBy : null
			};
			
			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.SampleFetchOptions();
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapSamples(permIds, fo);
			}
			
			testMap(c, fCreate, fMap, fechOptionsTestConfig);
		});
		
		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);
			var fechOptionsTestConfig = {
					Parents : null,
					Children : null,
					Components : null,
					Type : null,
					History : null,
					Experiment : null,
					Properties : null,
					MaterialProperties : null,
					Modifier : null,
					Registrator : null,
					Containers : null,
					PhysicalData : null,
					LinkedData : null,
					Tags : null,
					DataStore : null,
					SortBy : null
			};
			
			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.DataSetFetchOptions();
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.mapDataSets(permIds, fo);
				
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}
			
			testMap(c, fCreate, fMap, fechOptionsTestConfig);
		});
		
		QUnit.test("mapMaterials()", function(assert) {
			var c = new common(assert);
			var fechOptionsTestConfig = {
					Type : null,
					History : null,
					Registrator : null,
					Properties : null,
					MaterialProperties : null,
					Tags : null,
					SortBy : null
			};
			
			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.MaterialFetchOptions();
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapMaterials(permIds, fo);
			}
			
			testMap(c, fCreate, fMap, fechOptionsTestConfig);
		});
		
	}
});
