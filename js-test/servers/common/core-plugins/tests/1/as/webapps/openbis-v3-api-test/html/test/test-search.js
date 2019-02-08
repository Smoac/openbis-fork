define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common', 'test/naturalsort' ], function($, _, openbis, openbisExecuteOperations, common, naturalsort) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testSearch = function(c, fSearch, fCheck, fCheckError) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fSearch(facade).then(function(results) {
					c.ok("Got results");
					fCheck(facade, results.getObjects());
					c.finish();
				});
			}).fail(function(error) {
				if (fCheckError) {
					fCheckError(error);
				} else {
					c.fail(error.message);
				}
				c.finish();
			});
		}

		var testSearchWithPagingAndSortingByAll = function(c, fSearch, fetchOptions) {
			testSearchWithPagingAndSorting(c, fSearch, fetchOptions, "code").then(function() {
				testSearchWithPagingAndSorting(c, fSearch, fetchOptions, "registrationDate").then(function() {
					testSearchWithPagingAndSorting(c, fSearch, fetchOptions, "modificationDate");
				});
			});
		}

		var testSearchWithPagingAndSorting = function(c, fSearch, fetchOptions, fieldName, fieldParameters, disableSortCheck, codeOfFirstAsc) {
			c.start();

			fetchOptions.from(null).count(null);
			fetchOptions.sort = null;

			return c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");

				return fSearch(facade).then(function(results) {
					var objects = results.getObjects();

					c.assertTrue(objects.length > 1, "Got at least 2 objects");

					c.ok("Sorting by " + fieldName);

					var fieldGetterName = "get" + fieldName.substr(0, 1).toUpperCase() + fieldName.substr(1);

					if(disableSortCheck && codeOfFirstAsc) {
						fetchOptions.from(0).count(1);
					} else {
						var comparatorAsc = function(o1, o2) {
							var v1 = o1[fieldGetterName](fieldParameters);
							var v2 = o2[fieldGetterName](fieldParameters);
							return naturalsort(v1, v2);
						};
	
						var comparatorDesc = function(o1, o2) {
							return comparatorAsc(o2, o1);
						};
	
						objects.sort(comparatorAsc);
						var codesAsc = objects.map(function(object) {
							return object.code;
						});
	
						objects.sort(comparatorDesc);
						var codesDesc = objects.map(function(object) {
							return object.code;
						});
						
						fetchOptions.from(1).count(1);
					}
					
					fetchOptions.sortBy()[fieldName](fieldParameters);
					return fSearch(facade).then(function(results) {
						c.ok("Got results ASC");
						if(disableSortCheck && codeOfFirstAsc) {
							c.assertObjectsWithValues(results.getObjects(), "code", [ codeOfFirstAsc ]);
							fetchOptions.from(null).count(null);
						} else {
							c.assertObjectsWithValues(results.getObjects(), "code", [ codesAsc[1] ]);
						}
						
						
						fetchOptions.sortBy()[fieldName](fieldParameters).desc();
						return fSearch(facade).then(function(results) {
							c.ok("Got results DESC");
							if(disableSortCheck && codeOfFirstAsc) {
								var lastObject = results.getObjects()[results.getObjects().length - 1];
								c.assertObjectsWithValues([lastObject], "code", [ codeOfFirstAsc ]);
							} else {
								c.assertObjectsWithValues(results.getObjects(), "code", [ codesDesc[1] ]);
							}
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("searchSpaces()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SpaceSearchCriteria();
				criteria.withCode().thatEquals("TEST");
				return facade.searchSpaces(criteria, c.createSpaceFetchOptions());
			}

			var fCheck = function(facade, spaces) {
				c.assertEqual(spaces.length, 1);
				var space = spaces[0];
				c.assertEqual(space.getPermId(), "TEST", "PermId");
				c.assertEqual(space.getCode(), "TEST", "Code");
				c.assertEqual(space.getDescription(), null, "Description");
				c.assertDate(space.getRegistrationDate(), "Registration date", 2013, 04, 12, 12, 59);
				c.assertEqual(space.getRegistrator().getUserId(), "admin", "Registrator userId");
				c.assertObjectsWithCollections(space, function(object) {
					return object.getSamples()
				});
				c.assertObjectsWithCollections(space, function(object) {
					return object.getProjects()
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSpaces() with paging and sorting", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.SpaceSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST");
			criteria.withCode().thatEquals("PLATONIC");

			var fo = c.createSpaceFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchSpaces(criteria, fo);
			}, fo);
		});

		QUnit.test("searchProjects()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ProjectSearchCriteria();
				criteria.withSpace().withCode().thatEquals("PLATONIC");
				criteria.withCode().thatEquals("SCREENING-EXAMPLES");
				return facade.searchProjects(criteria, c.createProjectFetchOptions());
			}

			var fCheck = function(facade, projects) {
				c.assertEqual(projects.length, 1);
				var project = projects[0];
				c.assertEqual(project.getPermId().getPermId(), "20130412103942912-1", "PermId");
				c.assertEqual(project.getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES", "Identifier");
				c.assertEqual(project.getCode(), "SCREENING-EXAMPLES", "Code");
				c.assertEqual(project.getDescription(), null, "Description");
				c.assertDate(project.getRegistrationDate(), "Registration date", 2013, 4, 12, 8, 39);
				c.assertObjectsWithCollections(project, function(object) {
					return object.getExperiments()
				});
				c.assertEqual(project.getSpace().getCode(), "PLATONIC", "Space code");
				c.assertEqual(project.getRegistrator().getUserId(), "admin", "Registrator userId");
				c.assertEqual(project.getLeader(), null, "Leader");
				c.assertObjectsWithoutCollections(project, function(object) {
					return object.getAttachments()
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchProjects() with paging and sorting", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.ProjectSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST-PROJECT");
			criteria.withCode().thatEquals("SCREENING-EXAMPLES");

			var fo = c.createProjectFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchProjects(criteria, fo);
			}, fo);
		});

		QUnit.test("searchProjects() with sorting by identifier", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.ProjectSearchCriteria();
			criteria.withOrOperator();
			criteria.withId().thatEquals(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
			criteria.withId().thatEquals(new c.ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));

			var fo = c.createProjectFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchProjects(criteria, fo);
			}, fo, "identifier");
		});

		QUnit.test("searchExperiments()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentSearchCriteria();
				criteria.withPermId().thatEquals("20130412105232616-2");
				return facade.searchExperiments(criteria, c.createExperimentFetchOptions());
			}

			var fCheck = function(facade, experiments) {
				c.assertEqual(experiments.length, 1);
				var experiment = experiments[0];
				c.assertEqual(experiment.getCode(), "EXP-1", "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HCS_PLATONIC", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExperiments() with paging and sorting", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.ExperimentSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("EXP-1");
			criteria.withCode().thatEquals("EXP-2");

			var fo = c.createExperimentFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchExperiments(criteria, fo);
			}, fo);
		});
		
		QUnit.test("searchExperiments() with paging and sorting by score", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.ExperimentSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatContains("EXP");
			criteria.withCode().thatContains("-1");

			var fo = c.createExperimentFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchExperiments(criteria, fo);
			}, fo, "fetchedFieldsScore", null, true, "EXP-1");
		});

		QUnit.test("searchExperiments() with sorting by identifier", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.ExperimentSearchCriteria();
			criteria.withOrOperator();
			criteria.withId().thatEquals(new c.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
			criteria.withId().thatEquals(new c.ExperimentIdentifier("/PLATONIC/SCREENING-EXAMPLES/EXP-2"));

			var fo = c.createExperimentFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchExperiments(criteria, fo);
			}, fo, "identifier");
		});

		QUnit.test("searchExperiments() with sorting by type", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.ExperimentSearchCriteria();
			criteria.withOrOperator();
			criteria.withId().thatEquals(new c.ExperimentIdentifier("/TEST/TEST-PROJECT/TEST-EXPERIMENT"));
			criteria.withId().thatEquals(new c.ExperimentIdentifier("/PLATONIC/SCREENING-EXAMPLES/EXP-2"));

			var fo = c.createExperimentFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchExperiments(criteria, fo);
			}, fo, "type");
		});

		QUnit.test("searchExperiments() withRegistrator withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentSearchCriteria();
				criteria.withRegistrator().withUserId().thatEquals("etlserver");
				return facade.searchExperiments(criteria, c.createExperimentFetchOptions());
			}

			var fCheck = function(facade, experiments) {
				c.assertObjectsWithValues(experiments, "code", [ "TEST-EXPERIMENT" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExperiments() withModifier withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentSearchCriteria();
				criteria.withModifier().withUserId().thatEquals("etlserver");
				return facade.searchExperiments(criteria, c.createExperimentFetchOptions());
			}

			var fCheck = function(facade, experiments) {
				c.assertObjectsWithValues(experiments, "code", [ "EXP-2", "TEST-EXPERIMENT-3" ]);
			}

			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchExperiments() withIdentifier", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentSearchCriteria();
				criteria.withIdentifier().thatEquals("/TEST/TEST-PROJECT/TEST-EXPERIMENT");
				return facade.searchExperiments(criteria, c.createExperimentFetchOptions());
			}

			var fCheck = function(facade, experiments) {
				c.assertEqual(experiments.length, 1);
				c.assertEqual(experiments[0].getIdentifier(), "/TEST/TEST-PROJECT/TEST-EXPERIMENT");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExperimentTypes()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentTypeSearchCriteria();
				criteria.withCode().thatStartsWith("HT");
				var fetchOptions = new c.ExperimentTypeFetchOptions();
				fetchOptions.withPropertyAssignments().withPropertyType();
				return facade.searchExperimentTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, experimentTypes) {
				c.assertEqual(experimentTypes.length, 1, "Number of experiment types");
				var type = experimentTypes[0];
				c.assertEqual(type.getCode(), "HT_SEQUENCING", "Experiment type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				c.assertEqual(type.getFetchOptions().withPropertyAssignments().withPropertyType().hasVocabulary(), false);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 1, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), false, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "EXPERIMENT_DESIGN", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Experiment Design", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataType(), "CONTROLLEDVOCABULARY", "Property data type");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExperimentTypes() with vocabularies", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentTypeSearchCriteria();
				criteria.withCode().thatStartsWith("HT");
				var fetchOptions = new c.ExperimentTypeFetchOptions();
				fetchOptions.withPropertyAssignments().withPropertyType().withVocabulary();
				return facade.searchExperimentTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, experimentTypes) {
				c.assertEqual(experimentTypes.length, 1, "Number of experiment types");
				var type = experimentTypes[0];
				c.assertEqual(type.getCode(), "HT_SEQUENCING", "Experiment type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				c.assertEqual(type.getFetchOptions().withPropertyAssignments().withPropertyType().hasVocabulary(), true);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 1, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), false, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "EXPERIMENT_DESIGN", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Experiment Design", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataType(), "CONTROLLEDVOCABULARY", "Property data type");
				c.assertEqual(propertyType.getVocabulary().getCode(), "EXPERIMENT_DESIGN", "Vocabulary code");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withPermId().thatEquals("20130415095748527-404");
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertEqual(samples.length, 1);
				var sample = samples[0];
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
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() with paging and sorting", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.SampleSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST-SAMPLE-1");
			criteria.withCode().thatEquals("TEST-SAMPLE-2");

			var fo = c.createSampleFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchSamples(criteria, fo);
			}, fo);
		});
		
		QUnit.test("searchSamples() with paging and sorting by score", function(assert) {
			var c = new common(assert, openbis);
			
			var criteria = new c.SampleSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatContains("TEST-SAMPLE-1");
			
			var fo = c.createSampleFetchOptions();
			
			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchSamples(criteria, fo);
			}, fo, "fetchedFieldsScore", null, true,"TEST-SAMPLE-1");
		});
		
		QUnit.test("searchSamples() withoutSpace", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("SAMPLE");
			var waitUntilIndexed = function(facade, samplePermId, timeout, action) {
				if (timeout < 0) {
					c.fail("Sample " + samplePermId + " after " + timeout + " msec.");
				}
				setTimeout(function() {
					c.ok("Wait until " + samplePermId + " indexed. " + timeout);
					var criteria = new c.SampleSearchCriteria();
					criteria.withPermId().thatEquals(samplePermId);
					facade.searchSamples(criteria, c.createSampleFetchOptions()).then(function(result) {
						if (result.getTotalCount() == 0) {
							waitUntilIndexed(facade, samplePermId, timeout - 1000, action);
						} else {
							action();
						}
					});
				}, 1000)
			};

			c.start();
			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				var creation = new c.SampleCreation();
				creation.setTypeId(new c.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				facade.createSamples([ creation ]).then(function(permIds) {
					var permId = permIds[0];
					c.ok("Shared sample created: " + permId);
					waitUntilIndexed(facade, permId.getPermId(), 10000, function() {
						var criteria = new c.SampleSearchCriteria();
						criteria.withoutSpace();
						facade.searchSamples(criteria, c.createSampleFetchOptions()).then(function(results) {
							c.ok("Got results");
							var samples = results.getObjects();
							c.assertObjectsWithValues(samples, "identifier", [ "/" + code ]);
							c.deleteSample(facade, permId).then(function() {
								c.ok("Sample " + permId + " trashed");
								c.finish();
							});
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchSamples() withoutExperiment", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withoutExperiment();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-1-CONTAINED-1", "TEST-SAMPLE-1-CONTAINED-2", "TEST-SAMPLE-1" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withExperiment", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withExperiment();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-2-CHILD-2", "TEST-SAMPLE-2-CHILD-1", "TEST-SAMPLE-2-PARENT", "TEST-SAMPLE-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withoutContainer", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withoutContainer();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-2", "TEST-SAMPLE-1", "TEST-SAMPLE-2-PARENT", "TEST-SAMPLE-2-CHILD-1", "TEST-SAMPLE-2-CHILD-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withContainer", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withCode().thatStartsWith("TEST-SAMPLE");
				criteria.withContainer();
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-1-CONTAINED-1", "TEST-SAMPLE-1-CONTAINED-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() with sorting by identifier", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.SampleSearchCriteria();
			criteria.withOrOperator();
			criteria.withId().thatEquals(new c.SampleIdentifier("/PLATONIC/PLATE-1"));
			criteria.withId().thatEquals(new c.SampleIdentifier("/TEST/TEST-SAMPLE-1"));

			var fo = c.createSampleFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchSamples(criteria, fo);
			}, fo, "identifier");
		});

		QUnit.test("searchSamples() with sorting by type", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.SampleSearchCriteria();
			criteria.withOrOperator();
			criteria.withId().thatEquals(new c.SampleIdentifier("/PLATONIC/PLATE-1"));
			criteria.withId().thatEquals(new c.SampleIdentifier("/TEST/TEST-SAMPLE-1"));

			var fo = c.createSampleFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchSamples(criteria, fo);
			}, fo, "type");
		});

		QUnit.test("searchSamples() withRegistrator withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withRegistrator().withUserId().thatEquals("etlserver");
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "TEST-SAMPLE-1", "TEST-SAMPLE-2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples() withModifier withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withModifier().withUserId().thatEquals("etlserver");
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertObjectsWithValues(samples, "code", [ "PLATE-1A", "SERIES-1" ]);
			}

			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchSamples() withIdentifier", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleSearchCriteria();
				criteria.withIdentifier().thatEquals("/PLATONIC/PLATE-1");
				return facade.searchSamples(criteria, c.createSampleFetchOptions());
			}

			var fCheck = function(facade, samples) {
				c.assertEqual(samples.length, 1);
				c.assertEqual(samples[0].getIdentifier(), "/PLATONIC/PLATE-1");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSampleTypes()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleTypeSearchCriteria();
				criteria.withCode().thatStartsWith("MA");

				var fetchOptions = new c.SampleTypeFetchOptions();

				var assignmentFetchOptions = fetchOptions.withPropertyAssignments();
				assignmentFetchOptions.withRegistrator();
				assignmentFetchOptions.sortBy().label().desc();

				var propertyTypeFetchOptions = assignmentFetchOptions.withPropertyType();
				propertyTypeFetchOptions.withVocabulary();
				propertyTypeFetchOptions.withMaterialType();
				propertyTypeFetchOptions.withRegistrator();

				return facade.searchSampleTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, sampleTypes) {
				c.assertEqual(sampleTypes.length, 1, "Number of sample types");
				var type = sampleTypes[0];
				c.assertEqual(type.getCode(), "MASTER_SAMPLE", "Sample type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 8, "Number of property assignments");

				var assignment = assignments[0];
				c.assertEqual(assignment.isMandatory(), true, "Mandatory property assignment?");
				c.assertEqual(assignment.getOrdinal(), 22, "Ordinal");
				c.assertEqual(assignment.getSection(), null, "Section");
				c.assertEqual(assignment.isShowInEditView(), true, "Show in edit view");
				c.assertEqual(assignment.isShowRawValueInForms(), false, "Show raw value in forms");
				c.assertDate(assignment.getRegistrationDate(), "Registration date", 2013, 4, 12, 8, 4);
				c.assertEqual(assignment.getRegistrator().getUserId(), "system", "Registrator user id");

				var propertyType = assignment.getPropertyType();
				c.assertEqual(propertyType.getCode(), "SAMPLE_KIND", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Sample Kind", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataType(), "CONTROLLEDVOCABULARY", "Property data type");
				c.assertEqual(propertyType.isManagedInternally(), false, "Property type managed internally?");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
				c.assertEqual(propertyType.getVocabulary().getCode(), "SAMPLE_TYPE", "Property type vocabulary code");
				c.assertEqual(propertyType.getMaterialType(), null, "Property type vocabulary code");
				c.assertEqual(propertyType.getSchema(), null, "Property type schema");
				c.assertEqual(propertyType.getTransformation(), null, "Property type transformation");
				c.assertEqual(propertyType.getRegistrator().getUserId(), "system", "Registrator user id");
				c.assertDate(propertyType.getRegistrationDate(), "Registration date", 2013, 4, 12, 8, 4);

				c.assertEqual(assignments[1].getPropertyType().getCode(), "NCBI_ORGANISM_TAXONOMY", "Second property type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSampleTypes() withSemanticAnnotations", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleTypeSearchCriteria();
				criteria.withSemanticAnnotations().withPermId().thatEquals("ST_SIRNA_WELL");
				var fetchOptions = new c.SampleTypeFetchOptions();
				return facade.searchSampleTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, sampleTypes) {
				c.assertEqual(sampleTypes.length, 1, "Number of sample types");
				c.assertEqual(sampleTypes[0].getCode(), "SIRNA_WELL", "Sample type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSampleTypes() withPropertyAssignments withSemanticAnnotations", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleTypeSearchCriteria();
				criteria.withPropertyAssignments().withSemanticAnnotations().withPermId().thatEquals("ST_ILLUMINA_FLOW_CELL_PT_CREATED_ON_CS");
				var fetchOptions = new c.SampleTypeFetchOptions();
				return facade.searchSampleTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, sampleTypes) {
				c.assertEqual(sampleTypes.length, 1, "Number of sample types");
				c.assertEqual(sampleTypes[0].getCode(), "ILLUMINA_FLOW_CELL", "Sample type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSampleTypes() withPropertyAssignments withPropertyType withSemanticAnnotations", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SampleTypeSearchCriteria();
				criteria.withPropertyAssignments().withPropertyType().withSemanticAnnotations().withPermId().thatEquals("PT_AGILENT_KIT");
				var fetchOptions = new c.SampleTypeFetchOptions();
				return facade.searchSampleTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, sampleTypes) {
				c.assertEqual(sampleTypes.length, 2, "Number of sample types");
				c.assertObjectsWithValues(sampleTypes, "code", [ "LIBRARY", "LIBRARY_POOL" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withPermId().thatEquals("20130415093804724-403");
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertEqual(dataSets.length, 1);
				var dataSet = dataSets[0];
				c.assertEqual(dataSet.getCode(), "20130415093804724-403", "Code");
				c.assertEqual(dataSet.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(dataSet.getExperiment().getCode(), "TEST-EXPERIMENT-2", "Experiment code");
				c.assertEqual(dataSet.getSample().getCode(), "TEST-SAMPLE-2", "Sample code");
				c.assertEqual(dataSet.getProperties()["DESCRIPTION"], "403 description", "Property DESCRIPTION");
				c.assertEqual(dataSet.isPostRegistered(), true, "post registered");

				var physicalData = dataSet.getPhysicalData();
				c.assertEqual(physicalData.getShareId(), "1", "Share id");
				c.assertEqual(physicalData.getLocation(), "1FD3FF61-1576-4908-AE3D-296E60B4CE06/06/e5/ad/20130415093804724-403", "Location");
				c.assertEqual(physicalData.getStatus(), "AVAILABLE", "Status");
				c.assertEqual(physicalData.getFileFormatType().getCode(), "PROPRIETARY", "File format type");
				c.assertEqual(physicalData.getLocatorType().getCode(), "RELATIVE_LOCATION", "Locator type");

				c.assertObjectsWithValues(dataSet.getParents(), "code", [ "20130415100158230-407" ]);
				c.assertObjectsWithValues(dataSet.getChildren(), "code", [ "20130415100238098-408", "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() with paging and sorting", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("20130412142205843-196");
			criteria.withCode().thatEquals("20130412142543232-197");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo);
		});

		QUnit.test("searchDataSets() with paging and sorting by score", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatContains("20130412142");
			criteria.withCode().thatContains("942295-198");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo, "fetchedFieldsScore", null, true, "20130412142942295-198");
		});

		QUnit.test("searchDataSets() with sorting by property", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withPermId().thatEquals("20130412142543232-197");
			criteria.withPermId().thatEquals("20130412142205843-196");
			criteria.withPermId().thatEquals("20130412142942295-198");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo, "property", "$RESOLUTION");
		});

		QUnit.test("searchDataSets() with sorting by type", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withPermId().thatEquals("20130412142543232-197");
			criteria.withPermId().thatEquals("20130412143121081-200");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo, "type");
		});

		QUnit.test("searchDataSets() withoutSample", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withoutSample();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415100158230-407", "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withSample", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withSample();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415093804724-403", "20130415100238098-408" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withoutExperiment", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withoutExperiment();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415100238098-408" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withExperiment", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatContains("-40");
				criteria.withExperiment();
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415093804724-403", "20130415100158230-407", "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withPhysicalData", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				var pdCriteria = criteria.withPhysicalData();
				pdCriteria.withLocation().thatEquals('"1FD3FF61-1576-4908-AE3D-296E60B4CE06/2e/ac/5a/20130412153118625-384"');
				pdCriteria.withStorageFormat().withCode().thatContains("PROPRIETARY");
				pdCriteria.withFileFormatType().withCode().thatContains("UNKNOWN");
				pdCriteria.withLocatorType().withCode().thatContains("RELATIVE_LOCATION");
				pdCriteria.withComplete().thatEquals("YES");
				pdCriteria.withStatus().thatEquals("AVAILABLE");
				pdCriteria.withPresentInArchive().thatEquals(false);
				pdCriteria.withStorageConfirmation().thatEquals(true);
				pdCriteria.withSpeedHint().thatEquals(-50);

				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130412153118625-384" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withPhysicalData with archiving requested", function(assert) {
			var c = new common(assert, openbis);
			
			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withCode().thatStartsWith("2013");
				var pdCriteria = criteria.withPhysicalData();
				pdCriteria.withArchivingRequested().thatEquals(true);
				
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}
			
			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130412152036861-380" ]);
			}
			
			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchDataSets() withLinkedData", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				var ldCriteria = criteria.withLinkedData();
				ldCriteria.withExternalDms().withCode().thatEquals("DMS_1");

				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20160613195437233-437" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withRegistrator withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withRegistrator().withUserId().thatEquals("selenium");
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130417094936021-428", "20130417094934693-427" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets() withModifier withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				criteria.withModifier().withUserId().thatEquals("selenium");
				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130412143121081-200", "20130412153119864-385" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSetTypes()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetTypeSearchCriteria();
				criteria.withCode().thatStartsWith("MA");
				var fetchOptions = new c.DataSetTypeFetchOptions();
				fetchOptions.withPropertyAssignments().sortBy().code().asc();
				return facade.searchDataSetTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, dataSetTypes) {
				c.assertEqual(dataSetTypes.length, 1, "Number of data set types");
				var type = dataSetTypes[0];
				c.assertEqual(type.getCode(), "MACS_OUTPUT", "Data set type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 2, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), false, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "MACS_VERSION", "Property type code");
				c.assertEqual(propertyType.getLabel(), "MACS VERSION", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataType(), "CONTROLLEDVOCABULARY", "Property data type");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
				c.assertEqual(assignments[1].getPropertyType().getCode(), "NOTES", "Second property type code");
				c.assertEqual(assignments[1].getPropertyType().getDataType(), "MULTILINE_VARCHAR", "Second property data type");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterials()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.MaterialSearchCriteria();
				criteria.withCode().thatEquals("H2O");
				return facade.searchMaterials(criteria, c.createMaterialFetchOptions());
			}

			var fCheck = function(facade, materials) {
				c.assertEqual(materials.length, 1);
				var material = materials[0];
				c.assertEqual(material.getCode(), "H2O", "Code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(Object.keys(properties), "DESCRIPTION", "Water");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterials() with paging and sorting", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.MaterialSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("ABC");
			criteria.withCode().thatEquals("SIRNA-2");

			var fo = c.createMaterialFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchMaterials(criteria, fo);
			}, fo);
		});
		
		QUnit.test("searchMaterials() with paging and sorting by score", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.MaterialSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatContains("SIRNA");
			criteria.withCode().thatContains("A-2");

			var fo = c.createMaterialFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchMaterials(criteria, fo);
			}, fo, "fetchedFieldsScore", null, true, "SIRNA-2");
		});

		QUnit.test("searchMaterials() with sorting by type", function(assert) {
			var c = new common(assert, openbis);

			var criteria = new c.MaterialSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("ABC");
			criteria.withCode().thatEquals("SIRNA-2");

			var fo = c.createMaterialFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchMaterials(criteria, fo);
			}, fo, "type");
		});

		QUnit.test("searchMaterials() withRegistrator withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.MaterialSearchCriteria();
				criteria.withRegistrator().withUserId().thatEquals("etlserver");
				return facade.searchMaterials(criteria, c.createMaterialFetchOptions());
			}

			var fCheck = function(facade, materials) {
				c.assertObjectsWithValues(materials, "code", [ "SIRNA-3", "SIRNA-4" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterials() withModifier withUserId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.MaterialSearchCriteria();
				criteria.withModifier().withUserId().thatEquals("etlserver");
				return facade.searchMaterials(criteria, c.createMaterialFetchOptions());
			}

			var fCheck = function(facade, materials) {
				// search by a modifier not supported yet
				c.assertObjectsWithValues(materials, "code", []);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterialTypes()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.MaterialTypeSearchCriteria();
				criteria.withCode().thatStartsWith("G");
				var fetchOptions = new c.MaterialTypeFetchOptions();
				fetchOptions.withPropertyAssignments().sortBy().code().desc();
				return facade.searchMaterialTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, materialTypes) {
				c.assertEqual(materialTypes.length, 1, "Number of material types");
				var type = materialTypes[0];
				c.assertEqual(type.getCode(), "GENE", "Material type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 2, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), false, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "GENE_SYMBOLS", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Gene symbols", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataType(), "VARCHAR", "Property data type");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
				c.assertEqual(assignments[1].getPropertyType().getCode(), "DESCRIPTION", "Second property type code");
				c.assertEqual(assignments[1].getPropertyType().getDescription(), "A Description", "Second property type description");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withText thatContains", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContains("20130412150049446-204 20130412140147735-20 20130417094936021-428 H2O");
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 4);

				var objectDataSet = objects[0];
				c.assertEqual(objectDataSet.getObjectKind(), "DATA_SET", "ObjectKind");
				c.assertEqual(objectDataSet.getObjectPermId().getPermId(), "20130417094936021-428", "ObjectPermId");
				c.assertEqual(objectDataSet.getObjectIdentifier().getPermId(), "20130417094936021-428", "ObjectIdentifier");
				c.assertContains(objectDataSet.getMatch(), "Perm ID: 20130417094936021-428", "Match");
				c.assertContains(objectDataSet.getMatch(), "Location: 1FD3FF61-1576-4908-AE3D-296E60B4CE06/67/85/36/20130417094936021-428", "Match");
				c.assertNotNull(objectDataSet.getScore(), "Score");
				c.assertNull(objectDataSet.getExperiment(), "Experiment");
				c.assertNull(objectDataSet.getSample(), "Sample");
				c.assertEqual(objectDataSet.getDataSet().getCode(), "20130417094936021-428", "DataSet");
				c.assertNull(objectDataSet.getMaterial(), "Material");

				var objectExperiment = objects[1];
				c.assertEqual(objectExperiment.getObjectKind(), "EXPERIMENT", "ObjectKind");
				c.assertEqual(objectExperiment.getObjectPermId().getPermId(), "20130412150049446-204", "ObjectPermId");
				c.assertEqual(objectExperiment.getObjectIdentifier().getIdentifier(), "/TEST/TEST-PROJECT/TEST-EXPERIMENT", "ObjectIdentifier");
				c.assertEqual(objectExperiment.getMatch(), "Perm ID: 20130412150049446-204", "Match");
				c.assertNotNull(objectExperiment.getScore(), "Score");
				c.assertEqual(objectExperiment.getExperiment().getCode(), "TEST-EXPERIMENT", "Experiment");
				c.assertNull(objectExperiment.getSample(), "Sample");
				c.assertNull(objectExperiment.getDataSet(), "DataSet");
				c.assertNull(objectExperiment.getMaterial(), "Material");

				var objectSample = objects[2];
				c.assertEqual(objectSample.getObjectKind(), "SAMPLE", "ObjectKind");
				c.assertEqual(objectSample.getObjectPermId().getPermId(), "20130412140147735-20", "ObjectPermId");
				c.assertEqual(objectSample.getObjectIdentifier().getIdentifier(), "/PLATONIC/PLATE-1", "ObjectIdentifier");
				c.assertEqual(objectSample.getMatch(), "Perm ID: 20130412140147735-20", "Match");
				c.assertNotNull(objectSample.getScore(), "Score");
				c.assertNull(objectSample.getExperiment(), "Experiment");
				c.assertEqual(objectSample.getSample().getCode(), "PLATE-1", "Sample");
				c.assertNull(objectSample.getDataSet(), "DataSet");
				c.assertNull(objectSample.getMaterial(), "Material");

				var objectMaterial = objects[3];
				c.assertEqual(objectMaterial.getObjectKind(), "MATERIAL", "ObjectKind");
				c.assertEqual(objectMaterial.getObjectPermId().getCode(), "H2O", "ObjectPermId 1");
				c.assertEqual(objectMaterial.getObjectPermId().getTypeCode(), "COMPOUND", "ObjectPermId 2");
				c.assertEqual(objectMaterial.getObjectIdentifier().getCode(), "H2O", "ObjectIdentifier 1");
				c.assertEqual(objectMaterial.getObjectIdentifier().getTypeCode(), "COMPOUND", "ObjectIdentifier 2");
				c.assertEqual(objectMaterial.getMatch(), "Identifier: H2O (COMPOUND)", "Match");
				c.assertNotNull(objectMaterial.getScore(), "Score");
				c.assertNull(objectMaterial.getExperiment(), "Experiment");
				c.assertNull(objectMaterial.getSample(), "Sample");
				c.assertNull(objectMaterial.getDataSet(), "DataSet");
				c.assertEqual(objectMaterial.getMaterial().getCode(), "H2O", "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withText thatContainsExactly", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContainsExactly("407 description");
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 1);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "DATA_SET", "ObjectKind");
				c.assertEqual(object0.getObjectPermId().getPermId(), "20130415100158230-407", "ObjectPermId");
				c.assertEqual(object0.getObjectIdentifier().getPermId(), "20130415100158230-407", "ObjectIdentifier");
				c.assertEqual(object0.getMatch(), "Property 'Description': 407 description", "Match");
				c.assertNotNull(object0.getScore(), "Score");
				c.assertNull(object0.getExperiment(), "Experiment");
				c.assertNull(object0.getSample(), "Sample");
				c.assertEqual(object0.getDataSet().getCode(), "20130415100158230-407", "DataSet");
				c.assertNull(object0.getMaterial(), "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withObjectKind thatIn", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContains("20130412150049446-204 20130412140147735-20 20130417094936021-428 H2O");
				criteria.withObjectKind().thatIn([ "EXPERIMENT" ]);
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 1);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "EXPERIMENT", "ObjectKind");
				c.assertEqual(object0.getObjectPermId().getPermId(), "20130412150049446-204", "ObjectPermId");
				c.assertEqual(object0.getObjectIdentifier().getIdentifier(), "/TEST/TEST-PROJECT/TEST-EXPERIMENT", "ObjectIdentifier");
				c.assertEqual(object0.getMatch(), "Perm ID: 20130412150049446-204", "Match");
				c.assertNotNull(object0.getScore(), "Score");
				c.assertEqual(object0.getExperiment().getCode(), "TEST-EXPERIMENT", "Experiment");
				c.assertNull(object0.getSample(), "Sample");
				c.assertNull(object0.getDataSet(), "DataSet");
				c.assertNull(object0.getMaterial(), "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withWildCards", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.GlobalSearchCriteria();
				criteria.withText().thatContains("256x25*");
				criteria.withWildCards();
				return facade.searchGlobally(criteria, c.createGlobalSearchObjectFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 1);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "DATA_SET", "ObjectKind");
				c.assertEqual(object0.getObjectPermId().getPermId(), "20130412142942295-198", "ObjectPermId");
				c.assertEqual(object0.getObjectIdentifier().getPermId(), "20130412142942295-198", "ObjectIdentifier");
				c.assertEqual(object0.getMatch(), "Property 'Resolution': 256x256", "Match");
				c.assertNotNull(object0.getScore(), "Score");
				c.assertEqual(object0.getDataSet().getCode(), "20130412142942295-198", "DataSet");
				c.assertNull(object0.getExperiment(), "Experiment");
				c.assertNull(object0.getSample(), "Sample");
				c.assertNull(object0.getMaterial(), "Material");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchObjectKindModifications()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ObjectKindModificationSearchCriteria();
				criteria.withObjectKind().thatIn([ "SAMPLE", "EXPERIMENT" ]);
				criteria.withOperationKind().thatIn([ "CREATE_OR_DELETE" ]);
				return facade.searchObjectKindModifications(criteria, c.createObjectKindModificationFetchOptions());
			}

			var fCheck = function(facade, objects) {
				c.assertEqual(objects.length, 2);

				var object0 = objects[0];
				c.assertEqual(object0.getObjectKind(), "SAMPLE", "ObjectKind");
				c.assertNotNull(object0.getLastModificationTimeStamp(), "LastModificationTimeStamp");

				var object1 = objects[1];
				c.assertEqual(object1.getObjectKind(), "EXPERIMENT", "ObjectKind");
				c.assertNotNull(object1.getLastModificationTimeStamp(), "LastModificationTimeStamp");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchPlugins()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.PluginSearchCriteria();
				criteria.withName().thatContains("e");
				criteria.withPluginType().thatEquals(c.PluginType.ENTITY_VALIDATION);
				var fo = c.createPluginFetchOptions();
				fo.withScript();
				fo.sortBy().name().desc();
				return facade.searchPlugins(criteria, fo);
			}

			var fCheck = function(facade, plugins) {
				c.assertEqual(plugins.length, 1);
				var plugin = plugins[0];
				c.assertEqual(plugin.getName(), "Has_Parents", "Name");
				c.assertEqual(plugin.getDescription(), "Check if the Entity has a parent", "Description");
				c.assertEqual(plugin.getPluginKind(), c.PluginKind.JYTHON, "Plugin kind");
				c.assertEqual(plugin.getPluginType(), c.PluginType.ENTITY_VALIDATION, "Plugin type");
				c.assertEqual(plugin.getFetchOptions().hasScript(), true, "Has script");
				c.assertEqual(plugin.getScript(), 'def validate(entity, isNew):\n'
						+ '  parents = entity.entityPE().parents\n'
						+ '  if parents:\n'
						+ '    return None\n'
						+ '  else:\n'
						+ '    return "No Parents have been selected!"\n' , "Script");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchVocabularies()", function(assert) {
			var c = new common(assert, openbis);
			
			var fSearch = function(facade) {
				var criteria = new c.VocabularySearchCriteria();
				criteria.withCode().thatEquals("$STORAGE_FORMAT");
				return facade.searchVocabularies(criteria, c.createVocabularyFetchOptions());
			}
			
			var fCheck = function(facade, vocabularies) {
				c.assertEqual(vocabularies.length, 1);
				var vocabulary = vocabularies[0];
				c.assertEqual(vocabulary.getCode(), "$STORAGE_FORMAT", "Code");
				c.assertEqual(vocabulary.getDescription(), "The on-disk storage format of a data set", "Description");
				c.assertEqual(vocabulary.isManagedInternally(), true, "Managed internally");
				c.assertEqual(vocabulary.isInternalNameSpace(), true, "Internal namespace");
				c.assertEqual(vocabulary.getTerms().length, 2, "# of terms");
			}
			
			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.VocabularyTermSearchCriteria();
				criteria.withCode().thatEquals("BDS_DIRECTORY");
				return facade.searchVocabularyTerms(criteria, c.createVocabularyTermFetchOptions());
			}

			var fCheck = function(facade, terms) {
				c.assertEqual(terms.length, 1);
				var term = terms[0];
				c.assertEqual(term.getCode(), "BDS_DIRECTORY", "Code");
				c.assertEqual(term.getVocabulary().getCode(), "$STORAGE_FORMAT", "Vocabulary code");
				c.assertEqual(term.getOrdinal(), 2, "Ordinal");
				c.assertEqual(term.isOfficial(), true, "Official");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExternalDms()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.ExternalDmsSearchCriteria();
				criteria.withCode().thatEquals("DMS_2");
				return facade.searchExternalDataManagementSystems(criteria, c.createExternalDmsFetchOptions());
			}

			var fCheck = function(facade, entities) {
				c.assertEqual(entities.length, 1);
				var edms = entities[0];
				c.assertEqual(edms.getCode(), "DMS_2", "Code");
				c.assertEqual(edms.getLabel(), "Test External openBIS instance", "Label");
				c.assertEqual(edms.getAddress(), "http://www.openbis.ch/perm_id=${code}", "Address");
				c.assertEqual(edms.getUrlTemplate(), "http://www.openbis.ch/perm_id=${code}", "URL template");
				c.assertEqual(edms.getAddressType(), "OPENBIS", "Address type");
				c.assertEqual(edms.isOpenbis(), true, "is openBIS?");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchTags()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.TagSearchCriteria();
				criteria.withCode().thatEquals("JS_TEST_METAPROJECT");
				return facade.searchTags(criteria, c.createTagFetchOptions());
			}

			var fCheck = function(facade, tags) {
				c.assertEqual(tags.length, 1);
				var tag = tags[0];
				c.assertEqual(tag.getCode(), "JS_TEST_METAPROJECT", "Code");
				c.assertEqual(tag.getPermId().getPermId(), "/openbis_test_js/JS_TEST_METAPROJECT", "PermId");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchAuthorizationGroups()", function(assert) {
			var c = new common(assert, openbis);
			var code;

			var fSearch = function(facade) {
				return c.createAuthorizationGroup(facade).then(function(permId) {
					var criteria = new c.AuthorizationGroupSearchCriteria();
					code = permId.getPermId();
					criteria.withCode().thatEquals(code);
					return facade.searchAuthorizationGroups(criteria, c.createAuthorizationGroupFetchOptions());
				});
			}

			var fCheck = function(facade, groups) {
				c.assertEqual(groups.length, 1);
				var group = groups[0];
				c.assertEqual(group.getCode(), code, "Code");
				var users = group.getUsers();
				c.assertEqual(users[0].getUserId(), "power_user", "User");
				c.assertEqual(users.length, 1, "# Users");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchAuthorizationGroups() existing with role assigments", function(assert) {
			var c = new common(assert, openbis);
			var code;

			var fSearch = function(facade) {
				var criteria = new c.AuthorizationGroupSearchCriteria();
				criteria.withCode().thatEquals("TEST-GROUP");
				return facade.searchAuthorizationGroups(criteria, c.createAuthorizationGroupFetchOptions());
			}

			var fCheck = function(facade, groups) {
				c.assertEqual(groups.length, 1);
				var group = groups[0];
				c.assertEqual(group.getCode(), "TEST-GROUP", "Code");
				var users = group.getUsers();
				c.assertEqual(users.length, 0, "# Users");
				var roleAssignments = group.getRoleAssignments();
				var numberOfTestSpaceAssignments = 0;
				var numberOfProjectAssignments = 0;
				for (var i = 0; i < roleAssignments.length; i++) {
					var ra = roleAssignments[i];
					if (ra.getSpace() && ra.getSpace().getCode() === "TEST") {
						c.assertEqual(ra.getRole(), "OBSERVER", "Role of assignment for space TEST");
						numberOfTestSpaceAssignments++;
					}
					if (ra.getProject()) {
						c.assertEqual(ra.getRole(), "ADMIN", "Role of assignment for project");
						c.assertEqual(ra.getProject().getCode(), "TEST-PROJECT", "Project code of assignment for project");
						c.assertEqual(ra.getProject().getSpace().getCode(), "TEST", "Project space of assignment for project");
						numberOfProjectAssignments++;
					}
				}
				c.assertEqual(numberOfTestSpaceAssignments, 1, "Number of TEST space assignments");
				c.assertEqual(numberOfProjectAssignments, 0, "Number of project assignments");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchRoleAssignments()", function(assert) {
			var c = new common(assert, openbis);
			var code;

			var fSearch = function(facade) {
				var criteria = new c.RoleAssignmentSearchCriteria();
				criteria.withSpace().withCode().thatEquals("TEST");
				criteria.withUser().withUserId().thatEquals("observer");
				return facade.searchRoleAssignments(criteria, c.createRoleAssignmentFetchOptions());
			}

			var fCheck = function(facade, assignments) {
				c.assertEqual(assignments.length, 1, "# Role Assignments");
				var assignment = assignments[0];
				c.assertEqual(assignment.getRole(), "OBSERVER", "Role");
				c.assertEqual(assignment.getRoleLevel(), "SPACE", "Role level");
				c.assertEqual(assignment.getSpace().getCode(), "TEST", "Space");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchPersons()", function(assert) {
			var c = new common(assert, openbis);
			var code;

			var fSearch = function(facade) {
				var criteria = new c.PersonSearchCriteria();
				criteria.withUserId().thatContains("bser");
				return facade.searchPersons(criteria, c.createPersonFetchOptions());
			}

			var fCheck = function(facade, persons) {
				c.assertEqual(persons.length, 1, "# persons");
				var person = persons[0];
				c.assertEqual(person.getUserId(), "observer", "User id");
				c.assertEqual(person.getRegistrator().getUserId(), "system", "Registrator");
				var assignments = person.getRoleAssignments();
				c.assertEqual(assignments.length, 1, "# Role Assignments");
				var assignment = assignments[0];
				c.assertEqual(assignment.getRole(), "OBSERVER", "Role");
				c.assertEqual(assignment.getRoleLevel(), "SPACE", "Role level");
				c.assertEqual(assignment.getSpace().getCode(), "TEST", "Space");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchOperationExecutions()", function(assert) {
			var c = new common(assert, openbis);

			c.start();

			// We want to start only once. Because testSearch() also calls
			// start() let's make the start() do nothing.
			c.start = function() {
			}

			c.createFacadeAndLogin().then(function(facade) {
				$.when(c.createOperationExecution(facade)).then(function(permId) {
					var fSearch = function(facade) {
						var criteria = new c.OperationExecutionSearchCriteria();
						return facade.searchOperationExecutions(criteria, new c.OperationExecutionFetchOptions());
					}

					var fCheck = function(facade, executions) {
						c.assertTrue(executions.length >= 1);
						var found = false;

						executions.forEach(function(execution) {
							if (execution.getPermId().getPermId() == permId.getPermId()) {
								found = true;
							}
						});

						c.assertTrue(found);
					}

					return testSearch(c, fSearch, fCheck);
				});
			}).fail(function() {
				c.fail();
				c.finish();
			});
		});

		QUnit.test("searchDataStores() withEmptyCriteria", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataStoreSearchCriteria();
				return facade.searchDataStores(criteria, c.createDataStoreFetchOptions());

				facade.getDss().searchDataSetFiles();
			}

			var fCheck = function(facade, dataStores) {
				c.assertEqual(dataStores.length, 2);
				c.assertObjectsWithValues(dataStores, "code", [ "DSS1", "DSS2" ]);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataStores() withCodeThatEquals", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataStoreSearchCriteria();
				criteria.withCode().thatEquals("DSS1");
				return facade.searchDataStores(criteria, c.createDataStoreFetchOptions());
			}

			var fCheck = function(facade, dataStores) {
				c.assertEqual(dataStores.length, 1);
				var dataStore = dataStores[0];
				c.assertEqual(dataStore.getCode(), "DSS1", "Code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("dataStoreFacade.searchFiles() atNonexistentDataStore", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				return facade.getDataStoreFacade("I_DONT_EXIST", "ME_NEITHER").searchFiles(new c.DataSetFileSearchCriteria(), c.createDataSetFileFetchOptions());
			}

			var fCheckError = function(error) {
				c.assertEqual(error, "No data stores found for codes: I_DONT_EXIST,ME_NEITHER");
			}

			testSearch(c, fSearch, null, fCheckError);
		});

		QUnit.test("dataStoreFacade.searchFiles() atOneChosenDataStore", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetFileSearchCriteria();
				var dataSetCriteria = criteria.withDataSet();
				dataSetCriteria.withOrOperator();

				// at DSS1
				dataSetCriteria.withCode().thatEquals("20130424111751209-430");
				// at DSS2
				dataSetCriteria.withCode().thatEquals("20130415093804724-403");

				return facade.getDataStoreFacade("DSS1").searchFiles(criteria, c.createDataSetFileFetchOptions());
			}

			var fCheck = function(facade, files) {
				c.assertEqual(files.length, 3);
				c.assertObjectsWithValues(files, "path", [ "", "feature_lists", "feature_lists/NUMBER_FEATURE_LIST" ]);

				files.forEach(function(file) {
					c.assertEqual(file.getDataStore().getCode(), "DSS1");
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("dataStoreFacade.searchFiles() atMultipleChosenDataStores", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetFileSearchCriteria();
				var dataSetCriteria = criteria.withDataSet();
				dataSetCriteria.withOrOperator();

				// at DSS1
				dataSetCriteria.withCode().thatEquals("20130424111751209-430");
				// at DSS2
				dataSetCriteria.withCode().thatEquals("20130415093804724-403");

				return facade.getDataStoreFacade("DSS1", "DSS2").searchFiles(criteria, c.createDataSetFileFetchOptions());
			}

			var fCheck = function(facade, files) {
				c.assertEqual(files.length, 6);
				c.assertObjectsWithValues(files, "path", [ "", "feature_lists", "feature_lists/NUMBER_FEATURE_LIST", "original", "original/emptyFile" ]);

				files.forEach(function(file) {
					if (file.getDataSetPermId().getPermId() == "20130424111751209-430") {
						c.assertEqual(file.getDataStore().getCode(), "DSS1");
					} else if (file.getDataSetPermId().getPermId() == "20130415093804724-403") {
						c.assertEqual(file.getDataStore().getCode(), "DSS2");
					} else {
						c.fail();
					}
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("dataStoreFacade.searchFiles() atAllAvailableDataStores", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.DataSetFileSearchCriteria();
				var dataSetCriteria = criteria.withDataSet();
				dataSetCriteria.withOrOperator();

				// at DSS1
				dataSetCriteria.withCode().thatEquals("20130424111751209-430");
				// at DSS2
				dataSetCriteria.withCode().thatEquals("20130415093804724-403");

				return facade.getDataStoreFacade().searchFiles(criteria, c.createDataSetFileFetchOptions());
			}

			var fCheck = function(facade, files) {
				c.assertEqual(files.length, 6);
				c.assertObjectsWithValues(files, "path", [ "", "feature_lists", "feature_lists/NUMBER_FEATURE_LIST", "original", "original/emptyFile" ]);

				files.forEach(function(file) {
					if (file.getDataSetPermId().getPermId() == "20130424111751209-430") {
						c.assertEqual(file.getDataStore().getCode(), "DSS1");
					} else if (file.getDataSetPermId().getPermId() == "20130415093804724-403") {
						c.assertEqual(file.getDataStore().getCode(), "DSS2");
					} else {
						c.fail();
					}
				});
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSemanticAnnotations() withPermId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				return c.createSemanticAnnotation(facade).then(function(permId) {
					var criteria = new c.SemanticAnnotationSearchCriteria();
					criteria.withPermId().thatEquals(permId.getPermId());
					return facade.searchSemanticAnnotations(criteria, c.createSemanticAnnotationFetchOptions());
				});
			}

			var fCheck = function(facade, annotations) {
				c.assertEqual(annotations.length, 1);
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSemanticAnnotations() withPredicate and withDescriptor", function(assert) {
			var c = new common(assert, openbis);
			var expectedAnnotation = null;

			var fSearch = function(facade) {
				return c.createSemanticAnnotation(facade).then(function(permId) {
					return c.findSemanticAnnotation(facade, permId);
				}).then(function(annotation) {
					expectedAnnotation = annotation;
					var criteria = new c.SemanticAnnotationSearchCriteria();
					criteria.withPredicateOntologyId().thatEquals(annotation.getPredicateOntologyId());
					return facade.searchSemanticAnnotations(criteria, c.createSemanticAnnotationFetchOptions());
				});
			}

			var fCheck = function(facade, annotations) {
				c.assertEqual(annotations.length, 1);
				var annotation = annotations[0];
				c.assertEqual(annotation.getPredicateOntologyId(), expectedAnnotation.getPredicateOntologyId(), "predicateOntologyId");
				c.assertEqual(annotation.getPredicateOntologyVersion(), expectedAnnotation.getPredicateOntologyVersion(), "predicateOntologyVersion");
				c.assertEqual(annotation.getPredicateAccessionId(), expectedAnnotation.getPredicateAccessionId(), "predicateAccessionId");
				c.assertEqual(annotation.getDescriptorOntologyId(), expectedAnnotation.getDescriptorOntologyId(), "descriptorOntologyId");
				c.assertEqual(annotation.getDescriptorOntologyVersion(), expectedAnnotation.getDescriptorOntologyVersion(), "descriptorOntologyVersion");
				c.assertEqual(annotation.getDescriptorAccessionId(), expectedAnnotation.getDescriptorAccessionId(), "descriptorAccessionId");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSemanticAnnotations() withEntityTypeId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SemanticAnnotationSearchCriteria();
				criteria.withEntityType().withId().thatEquals(new c.EntityTypePermId("PLATE", "SAMPLE"));
				return facade.searchSemanticAnnotations(criteria, c.createSemanticAnnotationFetchOptions());
			}

			var fCheck = function(facade, annotations) {
				c.assertEqual(annotations.length, 1);
				c.assertEqual(annotations[0].getPermId().getPermId(), "ST_PLATE", "Annotation perm id");
				c.assertEqual(annotations[0].getEntityType().getCode(), "PLATE", "Entity type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSemanticAnnotations() withPropertyTypeId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SemanticAnnotationSearchCriteria();
				criteria.withPropertyType().withId().thatEquals(new c.PropertyTypePermId("DESCRIPTION"));
				return facade.searchSemanticAnnotations(criteria, c.createSemanticAnnotationFetchOptions());
			}

			var fCheck = function(facade, annotations) {
				c.assertEqual(annotations.length, 1);
				c.assertEqual(annotations[0].getPermId().getPermId(), "PT_DESCRIPTION", "Annotation perm id");
				c.assertEqual(annotations[0].getPropertyType().getCode(), "DESCRIPTION", "Property type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSemanticAnnotations() withPropertyAssignmentId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.SemanticAnnotationSearchCriteria();
				criteria.withPropertyAssignment().withId().thatEquals(new c.PropertyAssignmentPermId(new c.EntityTypePermId("LIBRARY", "SAMPLE"), new c.PropertyTypePermId("PREPARED_BY")));
				return facade.searchSemanticAnnotations(criteria, c.createSemanticAnnotationFetchOptions());
			}

			var fCheck = function(facade, annotations) {
				c.assertEqual(annotations.length, 1);
				c.assertEqual(annotations[0].getPermId().getPermId(), "ST_LIBRARY_PT_PREPARED_BY", "Annotation perm id");
				c.assertEqual(annotations[0].getPropertyAssignment().getEntityType().getCode(), "LIBRARY", "Entity type code");
				c.assertEqual(annotations[0].getPropertyAssignment().getEntityType().getPermId().getEntityKind(), "SAMPLE", "Entity type kind");
				c.assertEqual(annotations[0].getPropertyAssignment().getPropertyType().getCode(), "PREPARED_BY", "Property type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchPropertyTypes() withPermId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.PropertyTypeSearchCriteria();
				criteria.withId().thatEquals(new c.PropertyTypePermId("TOTAL_READS"));
				return facade.searchPropertyTypes(criteria, c.createPropertyTypeFetchOptions());
			}

			var fCheck = function(facade, types) {
				c.assertEqual(types.length, 1);
				c.assertEqual(types[0].label, "Total reads", "Label");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchPropertyAssignments() withPermId", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				var criteria = new c.PropertyAssignmentSearchCriteria();
				criteria.withId().thatEquals(new c.PropertyAssignmentPermId(new c.EntityTypePermId("LIBRARY", "SAMPLE"), new c.PropertyTypePermId("EXTERNAL_SAMPLE_NAME")));
				return facade.searchPropertyAssignments(criteria, c.createPropertyAssignmentFetchOptions());
			}

			var fCheck = function(facade, types) {
				c.assertEqual(types.length, 1);
				c.assertEqual(types[0].getEntityType().getCode(), "LIBRARY", "Entity type code");
				c.assertEqual(types[0].getPropertyType().getCode(), "EXTERNAL_SAMPLE_NAME", "Property type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDeletions()", function(assert) {
			var c = new common(assert, openbis);

			var fSearch = function(facade) {
				return c.createSample(facade).then(function(permId) {
					var options = new c.SampleDeletionOptions();
					options.setReason("test reason");
					return facade.deleteSamples([ permId ], options).then(function(deletionId) {
						var criteria = new c.DeletionSearchCriteria();
						criteria.withId().thatEquals(deletionId);
						var fetchOptions = new c.DeletionFetchOptions();
						return facade.searchDeletions(criteria, fetchOptions);
					});
				});
			}

			var fCheck = function(facade, deletions) {
				c.assertEqual(deletions.length, 1);
				var deletion = deletions[0];
				c.assertEqual(deletion.getReason(), "test reason", "reason");
				c.assertToday(deletion.getDeletionDate(), "deletion date");
			}

			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchQueries() withId", function(assert) {
			var c = new common(assert, openbis);

			var creation = new c.QueryCreation();
			creation.setName(c.generateId("query"));
			creation.setDatabaseId(new c.QueryDatabaseName("openbisDB"));
			creation.setQueryType(c.QueryType.GENERIC);
			creation.setSql("select * from spaces");

			var fSearch = function(facade) {
				return facade.createQueries([creation]).then(function(techIds) {
					var criteria = new c.QuerySearchCriteria();
					criteria.withId().thatEquals(techIds[0]);
					return facade.searchQueries(criteria, c.createQueryFetchOptions());
				});
			}

			var fCheck = function(facade, queries) {
				c.assertEqual(queries.length, 1);
				c.assertEqual(queries[0].getName(), creation.getName(), "Name");
			}

			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchQueries() withName", function(assert) {
			var c = new common(assert, openbis);

			var creation = new c.QueryCreation();
			creation.setName(c.generateId("query"));
			creation.setDatabaseId(new c.QueryDatabaseName("openbisDB"));
			creation.setQueryType(c.QueryType.GENERIC);
			creation.setSql("select * from spaces");

			var fSearch = function(facade) {
				return facade.createQueries([creation]).then(function(techIds) {
					var criteria = new c.QuerySearchCriteria();
					criteria.withName().thatEquals(creation.getName());
					return facade.searchQueries(criteria, c.createQueryFetchOptions());
				});
			}

			var fCheck = function(facade, queries) {
				c.assertEqual(queries.length, 1);
				c.assertEqual(queries[0].getName(), creation.getName(), "Name");
			}

			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchQueries() withEntityTypeCodePattern", function(assert) {
			var c = new common(assert, openbis);

			var creation = new c.QueryCreation();
			creation.setName(c.generateId("query"));
			creation.setDatabaseId(new c.QueryDatabaseName("openbisDB"));
			creation.setQueryType(c.QueryType.EXPERIMENT);
			creation.setEntityTypeCodePattern(c.generateId("pattern"))
			creation.setSql("select * from experiments where perm_id = ${key}");
			
			var fSearch = function(facade) {
				return facade.createQueries([creation]).then(function(techIds) {
					var criteria = new c.QuerySearchCriteria();
					criteria.withEntityTypeCodePattern().thatEquals(creation.getEntityTypeCodePattern());
					return facade.searchQueries(criteria, c.createQueryFetchOptions());
				});
			}

			var fCheck = function(facade, queries) {
				c.assertEqual(queries.length, 1);
				c.assertEqual(queries[0].getName(), creation.getName(), "Name");
			}

			testSearch(c, fSearch, fCheck);
		});

	}

	return function() {
		executeModule("Search tests", openbis);
		executeModule("Search tests (executeOperations)", openbisExecuteOperations);
	}
});
