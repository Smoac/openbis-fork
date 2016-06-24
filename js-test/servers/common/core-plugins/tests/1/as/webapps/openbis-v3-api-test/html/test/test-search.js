define([ 'jquery', 'underscore', 'openbis', 'test/common', 'test/naturalsort' ], function($, _, openbis, common, naturalsort) {
	return function() {
		QUnit.module("Search tests");

		var testSearch = function(c, fSearch, fCheck) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				c.ok("Login");
				return fSearch(facade).then(function(results) {
					c.ok("Got results");
					fCheck(facade, results.getObjects());
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
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

		var testSearchWithPagingAndSorting = function(c, fSearch, fetchOptions, fieldName, fieldParameters) {
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
					fetchOptions.sortBy()[fieldName](fieldParameters);

					return fSearch(facade).then(function(results) {
						c.ok("Got results ASC");
						c.assertObjectsWithValues(results.getObjects(), "code", [ codesAsc[1] ]);
						fetchOptions.sortBy()[fieldName](fieldParameters).desc();

						return fSearch(facade).then(function(results) {
							c.ok("Got results DESC");
							c.assertObjectsWithValues(results.getObjects(), "code", [ codesDesc[1] ]);
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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

			var criteria = new c.ProjectSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST-PROJECT");
			criteria.withCode().thatEquals("SCREENING-EXAMPLES");

			var fo = c.createProjectFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchProjects(criteria, fo);
			}, fo);
		});

		QUnit.test("searchExperiments()", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

			var criteria = new c.ExperimentSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("EXP-1");
			criteria.withCode().thatEquals("EXP-2");

			var fo = c.createExperimentFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchExperiments(criteria, fo);
			}, fo);
		});

		QUnit.test("searchExperimentTypes()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentTypeSearchCriteria();
				criteria.withCode().thatStartsWith("HT");
				var fetchOptions = new c.ExperimentTypeFetchOptions();
				fetchOptions.withPropertyAssignments();
				return facade.searchExperimentTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, experimentTypes) {
				c.assertEqual(experimentTypes.length, 1, "Number of experiment types");
				var type = experimentTypes[0];
				c.assertEqual(type.getCode(), "HT_SEQUENCING", "Experiment type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				c.assertEqual(type.getFetchOptions().withPropertyAssignments().hasVocabulary(), false);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 1, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), false, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "EXPERIMENT_DESIGN", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Experiment Design", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataTypeCode(), "CONTROLLEDVOCABULARY", "Property data type code");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchExperimentTypes() with vocabularies", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.ExperimentTypeSearchCriteria();
				criteria.withCode().thatStartsWith("HT");
				var fetchOptions = new c.ExperimentTypeFetchOptions();
				fetchOptions.withPropertyAssignments().withVocabulary();
				return facade.searchExperimentTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, experimentTypes) {
				c.assertEqual(experimentTypes.length, 1, "Number of experiment types");
				var type = experimentTypes[0];
				c.assertEqual(type.getCode(), "HT_SEQUENCING", "Experiment type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				c.assertEqual(type.getFetchOptions().withPropertyAssignments().hasVocabulary(), true);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 1, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), false, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "EXPERIMENT_DESIGN", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Experiment Design", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataTypeCode(), "CONTROLLEDVOCABULARY", "Property data type code");
				c.assertEqual(propertyType.getVocabulary().getCode(), "EXPERIMENT_DESIGN", "Vocabulary code");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchSamples()", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

			var criteria = new c.SampleSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("TEST-SAMPLE-1");
			criteria.withCode().thatEquals("TEST-SAMPLE-2");

			var fo = c.createSampleFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchSamples(criteria, fo);
			}, fo);
		});

		QUnit.test("searchSamples() withoutExperiment", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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

		QUnit.test("searchSampleTypes()", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.SampleTypeSearchCriteria();
				criteria.withCode().thatStartsWith("MA");
				var fetchOptions = new c.SampleTypeFetchOptions();
				fetchOptions.withPropertyAssignments().sortBy().label().desc();
				return facade.searchSampleTypes(criteria, fetchOptions);
			}

			var fCheck = function(facade, sampleTypes) {
				c.assertEqual(sampleTypes.length, 1, "Number of sample types");
				var type = sampleTypes[0];
				c.assertEqual(type.getCode(), "MASTER_SAMPLE", "Sample type code");
				c.assertEqual(type.getFetchOptions().hasPropertyAssignments(), true);
				var assignments = type.getPropertyAssignments();
				c.assertEqual(assignments.length, 8, "Number of property assignments");
				c.assertEqual(assignments[0].isMandatory(), true, "Mandatory property assignment?");
				var propertyType = assignments[0].getPropertyType();
				c.assertEqual(propertyType.getCode(), "SAMPLE_KIND", "Property type code");
				c.assertEqual(propertyType.getLabel(), "Sample Kind", "Property type label");
				c.assertEqual(propertyType.getDescription(), "", "Property type description");
				c.assertEqual(propertyType.getDataTypeCode(), "CONTROLLEDVOCABULARY", "Property data type code");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
				c.assertEqual(assignments[1].getPropertyType().getCode(), "NCBI_ORGANISM_TAXONOMY", "Second property type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchDataSets()", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("20130412142205843-196");
			criteria.withCode().thatEquals("20130412142543232-197");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo);
		});

		QUnit.test("searchDataSets() with sorting by property", function(assert) {
			var c = new common(assert);

			var criteria = new c.DataSetSearchCriteria();
			criteria.withOrOperator();
			criteria.withPermId().thatEquals("20130412142543232-197");
			criteria.withPermId().thatEquals("20130412142205843-196");
			criteria.withPermId().thatEquals("20130412142942295-198");

			var fo = c.createDataSetFetchOptions();

			testSearchWithPagingAndSorting(c, function(facade) {
				return facade.searchDataSets(criteria, fo);
			}, fo, "property", "RESOLUTION");
		});

		QUnit.test("searchDataSets() withoutSample", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				var pdCriteria = criteria.withPhysicalData();
				pdCriteria.withShareId().thatEquals("2");
				pdCriteria.withLocation().thatEquals("1FD3FF61-1576-4908-AE3D-296E60B4CE06/2f/7a/b9/20130415100308111-409");
				pdCriteria.withSize().thatEquals(1);
				pdCriteria.withStorageFormat().withCode().thatContains("");
				pdCriteria.withFileFormatType().withCode().thatContains("PROPRIETARY");
				pdCriteria.withLocatorType().withCode().thatContains("RELATIVE_LOCATION");
				pdCriteria.withComplete().thatEquals("UNKNOWN");
				pdCriteria.withStatus().thatEquals("AVAILABLE");
				pdCriteria.withPresentInArchive().thatEquals(false);
				pdCriteria.withStorageConfirmation().thatEquals(true);
				pdCriteria.withSpeedHint().thatEquals(-50);

				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20130415100308111-409" ]);
			}

			testSearch(c, fSearch, fCheck);
		});
		
		QUnit.test("searchDataSets() withLinkedData", function(assert) {
			var c = new common(assert);

			var fSearch = function(facade) {
				var criteria = new c.DataSetSearchCriteria();
				var ldCriteria = criteria.withLinkedData();
				ldCriteria.withExternalCode().thatEquals("EXTERNAL_CODE_1");
				ldCriteria.withExternalDms().withCode().thatEquals("DMS_1");

				return facade.searchDataSets(criteria, c.createDataSetFetchOptions());
			}

			var fCheck = function(facade, dataSets) {
				c.assertObjectsWithValues(dataSets, "code", [ "20160613195437233-437" ]);
			}

			testSearch(c, fSearch, fCheck);
		});


		QUnit.test("searchDataSetTypes()", function(assert) {
			var c = new common(assert);

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
				c.assertEqual(propertyType.getDataTypeCode(), "CONTROLLEDVOCABULARY", "Property data type code");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
				c.assertEqual(assignments[1].getPropertyType().getCode(), "NOTES", "Second property type code");
				c.assertEqual(assignments[1].getPropertyType().getDataTypeCode(), "MULTILINE_VARCHAR", "Second property data type code");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchMaterials()", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

			var criteria = new c.MaterialSearchCriteria();
			criteria.withOrOperator();
			criteria.withCode().thatEquals("ABC");
			criteria.withCode().thatEquals("SIRNA-2");

			var fo = c.createMaterialFetchOptions();

			testSearchWithPagingAndSortingByAll(c, function(facade) {
				return facade.searchMaterials(criteria, fo);
			}, fo);
		});

		QUnit.test("searchMaterialTypes()", function(assert) {
			var c = new common(assert);

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
				c.assertEqual(propertyType.getDataTypeCode(), "VARCHAR", "Property data type code");
				c.assertEqual(propertyType.isInternalNameSpace(), false, "Property type internal name space?");
				c.assertEqual(assignments[1].getPropertyType().getCode(), "DESCRIPTION", "Second property type code");
				c.assertEqual(assignments[1].getPropertyType().getDescription(), "A Description", "Second property type description");
			}

			testSearch(c, fSearch, fCheck);
		});

		QUnit.test("searchGlobally() withText thatContains", function(assert) {
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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
			var c = new common(assert);

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

		QUnit.test("searchVocabularyTerms()", function(assert) {
			var c = new common(assert);

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

		QUnit.test("searchTags()", function(assert) {
			var c = new common(assert);

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

	}
});
