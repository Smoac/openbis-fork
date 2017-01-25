define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testCreate = function(c, fCreate, fFind, fCheck) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permIds) {
					c.assertTrue(permIds != null && permIds.length == 1, "Entity was created");
					return fFind(facade, permIds[0]).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						fCheck(entity);
						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("createSpaces()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("SPACE");

			var fCreate = function(facade) {
				var creation = new c.SpaceCreation();
				creation.setCode(code);
				creation.setDescription("test description");
				return facade.createSpaces([ creation ]);
			}

			var fCheck = function(space) {
				c.assertEqual(space.getCode(), code, "Code");
				c.assertEqual(space.getDescription(), "test description", "Description");
			}

			testCreate(c, fCreate, c.findSpace, fCheck);
		});

		QUnit.test("createProjects()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new c.ProjectCreation();
				projectCreation.setSpaceId(new c.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				attachmentCreation = new c.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world!"));
				projectCreation.setAttachments([ attachmentCreation ]);
				return facade.createProjects([ projectCreation ]);
			}

			var fCheck = function(project) {
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(project.getDescription(), "JS test project", "Description");
				var attachments = project.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
			}

			testCreate(c, fCreate, c.findProject, fCheck);
		});

		QUnit.test("createExperiments()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new c.ExperimentCreation();
				experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setCode(code);
				experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
				experimentCreation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				attachmentCreation = new c.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world!"));
				experimentCreation.setAttachments([ attachmentCreation ]);
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "SEQUENCE_ENRICHMENT");
				return facade.createExperiments([ experimentCreation ]);
			}

			var fCheck = function(experiment) {
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
			}

			testCreate(c, fCreate, c.findExperiment, fCheck);
		});
		
		QUnit.test("createExperimentTypes()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EXPERIMENT_TYPE");

			var fCreate = function(facade) {
				var assignmentCreation = new c.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new c.PropertyTypePermId("DESCRIPTION"));
				assignmentCreation.setPluginId(new c.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("initial value");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);

				var creation = new c.ExperimentTypeCreation();
				creation.setCode(code);
				creation.setDescription("a new description");
				creation.setValidationPluginId(new c.PluginPermId("Has_Parents"));
				creation.setPropertyAssignments([ assignmentCreation ]);

				return facade.createExperimentTypes([ creation ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "a new description", "Description");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
			}

			testCreate(c, fCreate, c.findExperimentType, fCheck);
		});
		
		QUnit.test("createExternalDms()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("DMS1");

			var fCreate = function(facade) {
				var externalDmsCreation = new c.ExternalDmsCreation();
				externalDmsCreation.setCode(code);
				externalDmsCreation.setLabel("LABELI");
				externalDmsCreation.setUrlTemplate("HTTP://XXX");
				externalDmsCreation.setType("GIT");
				return facade.createExternalDms([ externalDmsCreation ]);
			}

			var fCheck = function(externalDms) {
				c.assertEqual(externalDms.getCode(), code, "SOEMTHING?");
			}

			testCreate(c, fCreate, c.findExternalDms, fCheck);
		});
		
		

		QUnit.test("createSamples()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("SAMPLE");

			var fCreate = function(facade) {
				var creation = new c.SampleCreation();
				creation.setTypeId(new c.EntityTypePermId("UNKNOWN"));
				creation.setCode(code);
				creation.setSpaceId(new c.SpacePermId("TEST"));
				creation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				return facade.createSamples([ creation ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(sample.getTags()[0].getCode(), "CREATE_JSON_TAG", "Tag code");
			}

			testCreate(c, fCreate, c.findSample, fCheck);
		});

		QUnit.test("createSampleTypes()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("SAMPLE_TYPE");

			var fCreate = function(facade) {
				var assignmentCreation = new c.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new c.PropertyTypePermId("DESCRIPTION"));
				assignmentCreation.setPluginId(new c.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("initial value");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);

				var creation = new c.SampleTypeCreation();
				creation.setCode(code);
				creation.setDescription("a new description");
				creation.setAutoGeneratedCode(true);
				creation.setSubcodeUnique(true);
				creation.setGeneratedCodePrefix("TEST_PREFIX");
				creation.setListable(true);
				creation.setShowContainer(true);
				creation.setShowParents(true);
				creation.setShowParentMetadata(true);
				creation.setValidationPluginId(new c.PluginPermId("Has_Parents"));
				creation.setPropertyAssignments([ assignmentCreation ]);

				return facade.createSampleTypes([ creation ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "a new description", "Description");
				c.assertEqual(type.isAutoGeneratedCode(), true, "AutoGeneratedCode");
				c.assertEqual(type.isSubcodeUnique(), true, "SubcodeUnique");
				c.assertEqual(type.getGeneratedCodePrefix(), "TEST_PREFIX", "GeneratedCodePrefix");
				c.assertEqual(type.isListable(), true, "Listable");
				c.assertEqual(type.isShowContainer(), true, "ShowContainer");
				c.assertEqual(type.isShowParents(), true, "ShowParents");
				c.assertEqual(type.isShowParentMetadata(), true, "ShowParentMetadata");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
			}

			testCreate(c, fCreate, c.findSampleType, fCheck);
		});

		QUnit.test("createDataSetTypes()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("DATA_SET_TYPE");

			var fCreate = function(facade) {
				var assignmentCreation = new c.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new c.PropertyTypePermId("DESCRIPTION"));
				assignmentCreation.setPluginId(new c.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("initial value");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);

				var creation = new c.DataSetTypeCreation();
				creation.setCode(code);
				creation.setDescription("a new description");
				creation.setMainDataSetPattern(".*\\.jpg");
				creation.setMainDataSetPath("original/images/");
				creation.setDisallowDeletion(true);
				creation.setValidationPluginId(new c.PluginPermId("Has_Parents"));
				creation.setPropertyAssignments([ assignmentCreation ]);

				return facade.createDataSetTypes([ creation ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "a new description", "Description");
				c.assertEqual(type.getMainDataSetPattern(), ".*\\.jpg", "Main data set pattern");
				c.assertEqual(type.getMainDataSetPath(), "original/images/", "Main data set path");
				c.assertEqual(type.isDisallowDeletion(), true, "Disallow deletion");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
			}

			testCreate(c, fCreate, c.findDataSetType, fCheck);
		});

		QUnit.test("createMaterials()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("MATERIAL");

			var fCreate = function(facade) {
				var materialCreation = new c.MaterialCreation();
				materialCreation.setTypeId(new c.EntityTypePermId("COMPOUND"));
				materialCreation.setCode(code);
				materialCreation.setProperty("DESCRIPTION", "Metal");
				return facade.createMaterials([ materialCreation ]);
			}

			var fCheck = function(material) {
				c.assertEqual(material.getCode(), code, "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(properties["DESCRIPTION"], "Metal", "Property DESCRIPTION");
			}

			testCreate(c, fCreate, c.findMaterial, fCheck);
		});

		QUnit.test("createMaterialTypes()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("MATERIAL_TYPE");

			var fCreate = function(facade) {
				var assignmentCreation = new c.PropertyAssignmentCreation();
				assignmentCreation.setSection("test section");
				assignmentCreation.setOrdinal(10);
				assignmentCreation.setPropertyTypeId(new c.PropertyTypePermId("DESCRIPTION"));
				assignmentCreation.setPluginId(new c.PluginPermId("Diff_time"));
				assignmentCreation.setMandatory(true);
				assignmentCreation.setInitialValueForExistingEntities("initial value");
				assignmentCreation.setShowInEditView(true);
				assignmentCreation.setShowRawValueInForms(true);

				var creation = new c.MaterialTypeCreation();
				creation.setCode(code);
				creation.setDescription("a new description");
				creation.setValidationPluginId(new c.PluginPermId("Has_Parents"));
				creation.setPropertyAssignments([ assignmentCreation ]);

				return facade.createMaterialTypes([ creation ]);
			}

			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getDescription(), "a new description", "Description");

				c.assertEqual(type.getPropertyAssignments().length, 1, "Assignments count");

				var assignment = type.getPropertyAssignments()[0];

				c.assertEqual(assignment.getSection(), "test section", "Assignment section");
				c.assertEqual(assignment.getOrdinal(), 10, "Assignment ordinal");
				c.assertEqual(assignment.getPropertyType().getCode(), "DESCRIPTION", "Assignment property type code");
				c.assertEqual(assignment.isMandatory(), true, "Assignment mandatory");
				c.assertEqual(assignment.isShowInEditView(), true, "Assignment ShowInEditView");
				c.assertEqual(assignment.isShowRawValueInForms(), true, "Assignment ShowRawValueInForms");
			}

			testCreate(c, fCreate, c.findMaterialType, fCheck);
		});

		QUnit.test("createVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("VOCABULARY_TERM");

			var fCreate = function(facade) {
				var termCreation = new c.VocabularyTermCreation();
				termCreation.setVocabularyId(new c.VocabularyPermId("TEST-VOCABULARY"));
				termCreation.setCode(code);
				termCreation.setLabel("test label");
				termCreation.setDescription("test description");
				termCreation.setOfficial(true);
				termCreation.setPreviousTermId(new c.VocabularyTermPermId("TEST-TERM-1", "TEST-VOCABULARY"))
				return facade.createVocabularyTerms([ termCreation ]);
			}

			var fCheck = function(term) {
				c.assertEqual(term.getCode(), code, "Term code");
				c.assertEqual(term.getVocabulary().getCode(), "TEST-VOCABULARY", "Term vocabulary code");
				c.assertEqual(term.getLabel(), "test label", "Term label");
				c.assertEqual(term.getDescription(), "test description", "Term description");
				c.assertEqual(term.isOfficial(), true, "Term official");
				c.assertEqual(term.getOrdinal(), 2, "Term ordinal");
			}

			testCreate(c, fCreate, c.findVocabularyTerm, fCheck);
		});

		QUnit.test("createTags()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("TAG");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var tagCreation = new c.TagCreation();
				tagCreation.setCode(code);
				tagCreation.setDescription(description);
				return facade.createTags([ tagCreation ]);
			}

			var fCheck = function(tag) {
				c.assertEqual(tag.getCode(), code, "Tag code");
				c.assertEqual(tag.getDescription(), description, "Tag description");
			}

			testCreate(c, fCreate, c.findTag, fCheck);
		});

	}

	return function() {
		executeModule("Create tests", openbis);
		executeModule("Create tests (executeOperations)", openbisExecuteOperations);
	}
});
