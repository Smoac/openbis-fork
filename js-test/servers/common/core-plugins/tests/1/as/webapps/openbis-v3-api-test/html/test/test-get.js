define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testGet = function(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig) {
			c.start();
			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length > 0, "Entities were created");
					return fGet(facade, permIds).then(function(map) {
						c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
						permIds.forEach(function(permId) {
							var entity = map[permId];
							testFetchOptionsResults(c, fechOptionsTestConfig, true, entity);
							c.assertEqual(c.getId(entity).toString(), permId.toString(), "Entity perm id matches");
						});
						return fGetEmptyFetchOptions(facade, permIds).then(function(map) {
							c.assertEqual(Object.keys(map).length, permIds.length, "Entity map size is correct");
							permIds.forEach(function(permId) {
								var entity = map[permId];
								testFetchOptionsResults(c, fechOptionsTestConfig, false, entity);
								c.assertEqual(c.getId(entity).toString(), permId.toString(), "Entity perm id matches");
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
			for (component in toTest) {
				if (component === "SortBy") {
					fo.sortBy().code();
					c.assertEqual(true, ((fo.getSortBy()) ? true : false), "Component " + component + " set on Fetch Options.");
				} else {
					var methodNameWithUsing = "with" + component + "Using";
					if (typeof fo[methodNameWithUsing] === "function") {
						fo[methodNameWithUsing](null);
					} else {
						throw methodNameWithUsing + " should be a method.";
					}

					var methodNameWith = "with" + component;
					if (typeof fo[methodNameWith] === "function") {
						fo[methodNameWith]();
					} else {
						throw methodNameWith + " should be a method.";
					}

					var methodNameHas = "has" + component;
					if (typeof fo[methodNameHas] === "function") {
						c.assertEqual(true, fo[methodNameHas](), "Component " + component + " set on Fetch Options.");
					} else {
						throw methodNameHas + " should be a method.";
					}
				}
			}
		}

		var testFetchOptionsResults = function(c, toTest, expectedShouldSucceed, entity) {
			for (property in toTest) {
				if (property !== "SortBy") {
					var methodName = "get" + property;
					var errorFound = null;
					if (typeof entity[methodName] === "function") {
						try {
							var result = entity[methodName](); // Should not
							// thrown an
							// exception,
							// what it means
							// is right!
						} catch (error) {
							errorFound = error;
						}
						var msg = (expectedShouldSucceed) ? "Succeed" : "Fail";
						c.assertEqual(expectedShouldSucceed, !errorFound, "Calling method " + methodName + " expected to " + msg);
					} else {
						throw methodName + " should be a method.";
					}
				}
			}
		}

		var getMethods = function(obj) {
			var result = [];
			for ( var id in obj) {
				try {
					if (typeof (obj[id]) == "function") {
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
			for (var mIdx = 0; mIdx < methods.length; mIdx++) {
				var method = methods[mIdx];
				if (method.startsWith("has")) {
					var component = method.substring(3, method.indexOf(':'));
					components[component] = null;
				}
			}
			return components;
		}

		QUnit.test("getSpaces()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.SpaceFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createSpace(facade), c.createSpace(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSpaces(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSpaces(permIds, new c.SpaceFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getProjects()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.ProjectFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createProject(facade), c.createProject(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {

				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getProjects(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getProjects(permIds, new c.ProjectFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getExperiments()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.ExperimentFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createExperiment(facade), c.createExperiment(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getExperiments(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getExperiments(permIds, new c.ExperimentFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getSamples()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.SampleFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createSample(facade), c.createSample(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSamples(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSamples(permIds, new c.SampleFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getDataSets()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.DataSetFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createDataSet(facade), c.createDataSet(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.getDataSets(permIds, fo);

				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				var result = facade.getDataSets(permIds, new c.DataSetFetchOptions());

				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.isPostRegistered(), false, "post registered for " + permId);
					});
				});
				return result;
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getMaterials()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.MaterialFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createMaterial(facade), c.createMaterial(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getMaterials(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getMaterials(permIds, new c.MaterialFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getPropertyTypes()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.PropertyTypeFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createPropertyType(facade), c.createPropertyType(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getPropertyTypes(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getPropertyTypes(permIds, new c.PropertyTypeFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getPlugins()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.PluginFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			
			var fCreate = function(facade) {
				return $.when(c.createPlugin(facade), c.createPlugin(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getPlugins(permIds, fo);
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getPlugins(permIds, new c.PluginFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getVocabularies()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.VocabularyFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createVocabulary(facade), c.createVocabulary(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getVocabularies(permIds, fo);
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getVocabularies(permIds, new c.VocabularyFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.VocabularyTermFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createVocabularyTerm(facade), c.createVocabularyTerm(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getVocabularyTerms(permIds, fo);
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getVocabularyTerms(permIds, new c.VocabularyTermFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getExternalDms()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.ExternalDmsFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			
			var fCreate = function(facade) {
				return $.when(c.createExternalDms(facade), c.createExternalDms(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getExternalDataManagementSystems(permIds, fo);
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getExternalDataManagementSystems(permIds, new c.ExternalDmsFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getTags()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.TagFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;

			var fCreate = function(facade) {
				return $.when(c.createTag(facade), c.createTag(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getTags(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getTags(permIds, new c.TagFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

		QUnit.test("getAuthorizationGroups()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.AuthorizationGroupFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			fechOptionsTestConfig.SortBy = null;
			
			var fCreate = function(facade) {
				return $.when(c.createAuthorizationGroup(facade), c.createAuthorizationGroup(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getAuthorizationGroups(permIds, fo);
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getAuthorizationGroups(permIds, new c.AuthorizationGroupFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getRoleAssignments() with user", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.RoleAssignmentFetchOptions();
			fo.withUser();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			
			var fCreate = function(facade) {
				return $.when(c.createRoleAssignment(facade, true)).then(function(id) {
					return [ id ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.getRoleAssignments(permIds, fo);
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.getUser().getUserId(), "power_user", "User");
					});
				});
				return result;
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getRoleAssignments(permIds, new c.RoleAssignmentFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getRoleAssignments() with authorization group", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.RoleAssignmentFetchOptions();
			fo.withAuthorizationGroup();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			
			var fCreate = function(facade) {
				return $.when(c.createRoleAssignment(facade, false)).then(function(id) {
					return [ id ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				var result = facade.getRoleAssignments(permIds, fo);
				result.then(function(map) {
					permIds.forEach(function(permId) {
						var entity = map[permId];
						c.assertEqual(entity.getAuthorizationGroup().getCode(), "TEST-GROUP", "Authorization group");
					});
				});
				return result;
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getRoleAssignments(permIds, new c.RoleAssignmentFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getPersons()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.PersonFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);
			
			var fCreate = function(facade) {
				return $.when(c.createPerson(facade), c.createPerson(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}
			
			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getPersons(permIds, fo);
			}
			
			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getPersons(permIds, new c.PersonFetchOptions());
			}
			
			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getOperationExecutions()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.OperationExecutionFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createOperationExecution(facade), c.createOperationExecution(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getOperationExecutions(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getOperationExecutions(permIds, new c.OperationExecutionFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});
		
		QUnit.test("getSemanticAnnotations()", function(assert) {
			var c = new common(assert, openbis);
			var fo = new c.SemanticAnnotationFetchOptions();
			var fechOptionsTestConfig = getConfigForFetchOptions(fo);

			var fCreate = function(facade) {
				return $.when(c.createSemanticAnnotation(facade), c.createSemanticAnnotation(facade)).then(function(permId1, permId2) {
					return [ permId1, permId2 ];
				});
			}

			var fGet = function(facade, permIds) {
				testFetchOptionsAssignation(c, fo, fechOptionsTestConfig);
				return facade.getSemanticAnnotations(permIds, fo);
			}

			var fGetEmptyFetchOptions = function(facade, permIds) {
				return facade.getSemanticAnnotations(permIds, new c.SemanticAnnotationFetchOptions());
			}

			testGet(c, fCreate, fGet, fGetEmptyFetchOptions, fechOptionsTestConfig);
		});

	}

	return function() {
		executeModule("Get tests", openbis);
		executeModule("Get tests (executeOperations)", openbisExecuteOperations);
	}
});
