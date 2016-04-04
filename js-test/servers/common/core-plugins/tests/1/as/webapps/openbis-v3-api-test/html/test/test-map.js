define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Map tests");
		
		var testMap = function(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig) {
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
						return fMapEmptyFetchOptions(facade, permIds).then(function(map) {
							c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
							permIds.forEach(function(permId) {
								var entity = map[permId];
								testFetchOptionsResults(c, fechOptionsTestConfig, false, entity);
								c.assertEqual(entity.getPermId().toString(), permId.toString(), "Entity perm id matches");
							});
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}
		
		var testFetchOptionsAssignation = function(c, fo, toTest) {
			for(component in toTest) {
				if(component === "SortBy") {
					fo.sortBy().code();
					c.assertEqual(true, ((fo.getSortBy())?true:false), "Component " + component + " set on Fetch Options.");
				} else {
					var methodNameWithUsing = "with" + component + "Using";
					if(typeof fo[methodNameWithUsing] === "function") {
						fo[methodNameWithUsing](null);
					} else {
						throw methodNameWithUsing + " should be a method.";
					}
					
					var methodNameWith = "with" + component;
					if(typeof fo[methodNameWith] === "function") {
						fo[methodNameWith]();
					} else {
						throw methodNameWith + " should be a method.";
					}
					
					var methodNameHas = "has" + component;
					if(typeof fo[methodNameHas] === "function") {
						c.assertEqual(true, fo[methodNameHas](), "Component " + component + " set on Fetch Options.");
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
					var errorFound = null;
					if(typeof entity[methodName] === "function") {
						try {
							var result = entity[methodName](); //Should not thrown an exception, what it means is right!
						} catch(error) {
							errorFound = error;
						}
						var msg = (expectedShouldSucceed)?"Succeed":"Fail";
						c.assertEqual(expectedShouldSucceed, !errorFound, "Calling method " + methodName + " expected to " + msg);
					} else {
						throw methodName + " should be a method.";
					}
				}
			}
		}
		
		var getMethods = function(obj) {
			  var result = [];
			  for (var id in obj) {
			    try {
			      if (typeof(obj[id]) == "function") {
			        result.push(id + ": " + obj[id].toString());
			      }
			    } catch (err) {
			      result.push(id + ": inaccessible");
			    }
			  }
			  return result;
		}
		
		var getConfigForFetchOptions = function(fo) {
			var components = {};
			var methods = getMethods(fo);
			for(var mIdx = 0; mIdx < methods.length; mIdx++) {
				var method = methods[mIdx];
				if(method.startsWith("has")) {
					var component = method.substring(3, method.indexOf(':'));
					components[component] = null;
				}
			}
			return components;
		}
		
		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);
			var fo = new c.SpaceFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapSpaces(permIds, fo);
			}
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				return facade.mapSpaces(permIds, new c.SpaceFetchOptions());
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);
			var fo = new c.ProjectFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapProjects(permIds, fo);
			}
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				return facade.mapProjects(permIds, new c.ProjectFetchOptions());
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("mapExperiments()", function(assert) {
			var c = new common(assert);
			var fo = new c.ExperimentFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapExperiments(permIds, fo);
			}
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				return facade.mapExperiments(permIds, new c.ExperimentFetchOptions());
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("mapSamples()", function(assert) {
			var c = new common(assert);
			var fo = new c.SampleFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapSamples(permIds, fo);
			}
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				return facade.mapSamples(permIds, new c.SampleFetchOptions());
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("mapDataSets()", function(assert) {
			var c = new common(assert);
			var fo = new c.DataSetFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
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
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				var result = facade.mapDataSets(permIds, new c.DataSetFetchOptions());
				
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("mapMaterials()", function(assert) {
			var c = new common(assert);
			var fo = new c.MaterialFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapMaterials(permIds, fo);
			}
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				return facade.mapMaterials(permIds, new c.MaterialFetchOptions());
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("mapVocabularyTerms()", function(assert) {
			var c = new common(assert);
			var fo = new c.VocabularyTermFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createVocabularyTerm(facade), c.createVocabularyTerm(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fMap = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.mapVocabularyTerms(permIds, fo);
			}
			
			var fMapEmptyFetchOptions = function(facade, permIds) {
				return facade.mapVocabularyTerms(permIds, new c.VocabularyTermFetchOptions());
			}
			
			testMap(c, fCreate, fMap, fMapEmptyFetchOptions, fechOptionsTestConfig);
		});
		
	}
});
