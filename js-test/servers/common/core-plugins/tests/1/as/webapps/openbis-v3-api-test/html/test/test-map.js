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
		
		var testFetchOptionsResults = function(c, fo, toTest, entity, entityType) {
			for(property in toTest) {
				var expectedShouldSucceed = toTest[property];
				var methodName = "get" + property;
				if(typeof entity[methodName] === "function") {
					try {
						var result = entity[methodName](); //Should not thrown an exception, what it means is right!
						if(!expectedShouldSucceed) {
							throw "Calling method " + methodName + " succeed when it should thrown an exception for entity type " + entityType + ".";
						}
					} catch(error) {
						if(expectedShouldSucceed) {
							throw "Calling method " + methodName + " thrown an exception when it should succeed for entity type " + entityType + ".";
						}
					}
				} else {
					throw methodName + " should be a method.";
				}
			}
		}
		
		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);
			var config = {
					Registrator : true,
					Projects : true,
					Samples : true,
					SortBy : true
			}
			
			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.SpaceFetchOptions();
				testFetchOptionsAssignation(c, fo, config);
				return facade.mapSpaces(permIds, fo);
			}
			
			testMap(c, fCreate, fMap);
		});
		
		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);
			var config = {
					Experiments : true,
					Space : true,
					Leader : true,
					Modifier : true,
					Attachments : true,
					Registrator : true,
					Samples : true,
					SortBy : true,
					History : true
			};
			
			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.ProjectFetchOptions();
				testFetchOptionsAssignation(c, fo, config);
				return facade.mapProjects(permIds, fo);
			}
			
			testMap(c, fCreate, fMap);
		});
		
		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);
			var config = {
					Type : true,
					Project : true,
					DataSets : true,
					Properties : true,
					MaterialProperties : true,
					Tags : true,
					Samples : true,
					History : true,
					Registrator : true,
					Modifier : true,
					Attachments : true,
					SortBy : true
			};
			
			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.ExperimentFetchOptions();
				testFetchOptionsAssignation(c, fo, config);
				return facade.mapExperiments(permIds, fo);
			}
			
			testMap(c, fCreate, fMap);
		});
		
		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);
			var config = {
					Parents : true,
					Children : true,
					Container : true,
					Components : true,
					Type : true,
					Project : true,
					Space : true,
					Experiment : true,
					Properties : true,
					MaterialProperties : true,
					DataSets : true,
					History : true,
					SortBy : true
			};
			
			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.SampleFetchOptions();
				testFetchOptionsAssignation(c, fo, config);
				return facade.mapSamples(permIds, fo);
			}
			
			testMap(c, fCreate, fMap);
		});
		
		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);
			var config = {
					Parents : true,
					Children : true,
					Components : true,
					Type : true,
					History : true,
					Experiment : true,
					Properties : true,
					MaterialProperties : true,
					Modifier : true,
					Registrator : true,
					Containers : true,
					PhysicalData : true,
					LinkedData : true,
					Tags : true,
					DataStore : true,
					SortBy : true
			};
			
			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.DataSetFetchOptions();
				testFetchOptionsAssignation(c, fo, config);
				var result = facade.mapDataSets(permIds, fo);
				
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
			var config = {
					Type : true,
					History : true,
					Registrator : true,
					Properties : true,
					MaterialProperties : true,
					Tags : true,
					SortBy : true
			};
			
			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				var fo = new c.MaterialFetchOptions();
				testFetchOptionsAssignation(c, fo, config);
				return facade.mapMaterials(permIds, fo);
			}
			
			testMap(c, fCreate, fMap);
		});
		
	}
});
