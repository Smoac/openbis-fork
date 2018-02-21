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
						var token = fCheck(entity, facade);
						if (token) {
							token.then(function() {
								c.finish()
							});
						} else {
							c.finish();
						}
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("createPermIdStrings", function(assert) {
			var c = new common(assert, openbis);
			c.start();
			c.createFacadeAndLogin().then(function(facade) {
				facade.createPermIdStrings(7).then(function(permIds) {
					c.assertEqual(permIds.length, 7, "Number of perm ids");
					c.finish();
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

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

		QUnit.test("createDataSets() link data set via DSS", function(assert) {
			var c = new common(assert, openbis);
			var emdsId = null;

			var fCreate = function(facade) {
				return c.createExperiment(facade).then(function(experimentPermId) {
					return c.createFileExternalDms(facade).then(function(emdsPermId) {
						emdsId = emdsPermId;
						var creation = new c.FullDataSetCreation();
						var dataSet = new c.DataSetCreation();
						dataSet.setCode(c.generateId("DATA_SET"));
						dataSet.setTypeId(new c.EntityTypePermId("LINK_TYPE"));
						dataSet.setExperimentId(experimentPermId);
						dataSet.setDataStoreId(new c.DataStorePermId("DSS1"));
						var linkedData = new c.LinkedDataCreation();
						var cc = new c.ContentCopyCreation();
						cc.setExternalDmsId(emdsPermId);
						cc.setPath("my/path");
						linkedData.setContentCopies([ cc ]);
						dataSet.setLinkedData(linkedData);
						creation.setMetadataCreation(dataSet);
						var f1 = new c.DataSetFileCreation();
						f1.setDirectory(true);
						f1.setPath("root/folder");
						var f2 = new c.DataSetFileCreation();
						f2.setDirectory(false);
						f2.setPath("root/my-file-in.txt");
						f2.setFileLength(42);
						f2.setChecksumCRC32(123456);
						creation.setFileMetadata([ f1, f2 ]);
						return facade.getDataStoreFacade("DSS1", "DSS2").createDataSets([ creation ]);
					});
				});
			}

			var fCheck = function(dataSet, facade) {
				c.assertEqual(dataSet.getType().getCode(), "LINK_TYPE", "Data set type");
				var contentCopies = dataSet.getLinkedData().getContentCopies();
				c.assertEqual(contentCopies.length, 1, "Number of content copies");
				var contentCopy = contentCopies[0];
				c.assertEqual(contentCopy.getExternalDms().getPermId().toString(), emdsId.toString(), "External data management system");
				c.assertEqual(contentCopy.getPath(), "/my/path", "Content copy path");
				var dfd = $.Deferred()
				var dataSetCode = dataSet.getCode();
				c.waitUntilIndexed(facade, dataSetCode, 10000).then(function() {
					var criteria = new c.DataSetFileSearchCriteria();
					criteria.withDataSet().withCode().thatEquals(dataSet.getCode());
					facade.getDataStoreFacade("DSS1").searchFiles(criteria, c.createDataSetFileFetchOptions()).then(function(result) {
						var files = result.getObjects();
						c.assertEqual(files.length, 4, "Number of files");
						files.sort(function(f1, f2) {
							return f1.getPath().localeCompare(f2.getPath());
						});
						var expectedPaths = [ "", "root", "root/folder", "root/my-file-in.txt" ];
						var expectedDirectoryFlags = [ true, true, true, false ];
						var expectedSizes = [ 42, 42, 0, 42 ];
						var expectedChecksums = [ 0, 0, 0, 123456 ];
						for (i = 0; i < files.length; i++) {
							var file = files[i];
							var postfix = " of file " + (i + 1);
							c.assertEqual(file.getDataSetPermId().toString(), dataSetCode, "Data set" + postfix);
							c.assertEqual(file.getDataStore().getCode(), "DSS1", "Data store" + postfix);
							c.assertEqual(file.getPath(), expectedPaths[i], "Path" + postfix);
							c.assertEqual(file.isDirectory(), expectedDirectoryFlags[i], "Directory flag" + postfix);
							c.assertEqual(file.getChecksumCRC32(), expectedChecksums[i], "Checksum" + postfix);
						}
						dfd.resolve();
					});
				});
				return dfd.promise();
			}

			testCreate(c, fCreate, c.findDataSet, fCheck);
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
		
		QUnit.test("createPropertyTypes()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("PROPERTY_TYPE");
			
			var fCreate = function(facade) {
				var creation = new c.PropertyTypeCreation();
				creation.setCode(code);
				creation.setDescription("hello");
				creation.setDataType(c.DataType.INTEGER);
				creation.setLabel("Test Property Type");
				return facade.createPropertyTypes([ creation ]);
			}
			
			var fCheck = function(type) {
				c.assertEqual(type.getCode(), code, "Type code");
				c.assertEqual(type.getPermId().getPermId(), code, "Type perm id");
				c.assertEqual(type.getLabel(), "Test Property Type", "Label");
				c.assertEqual(type.getDescription(), "hello", "Description");
				c.assertEqual(type.getDataType(), c.DataType.INTEGER, "Data type");
			}
			
			testCreate(c, fCreate, c.findPropertyType, fCheck);
		});

		QUnit.test("createVocabularies()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("VOCABULARY");

			var fCreate = function(facade) {
				var vocabularyCreation = new c.VocabularyCreation();
				vocabularyCreation.setCode(code);
				vocabularyCreation.setDescription("test description");
				vocabularyCreation.setManagedInternally(true);
				vocabularyCreation.setInternalNameSpace(true);
				vocabularyCreation.setChosenFromList(true);
				vocabularyCreation.setUrlTemplate("https://www.ethz.ch");
				var termCreation = new c.VocabularyTermCreation();
				termCreation.setCode("alpha");
				vocabularyCreation.setTerms([ termCreation ]);
				return facade.createVocabularies([ vocabularyCreation ]);
			}

			var fCheck = function(vocabulary) {
				c.assertEqual(vocabulary.getCode(), "$" + code, "Code");
				c.assertEqual(vocabulary.getDescription(), "test description", "Description");
				c.assertEqual(vocabulary.isManagedInternally(), true, "Managed internally");
				c.assertEqual(vocabulary.isInternalNameSpace(), true, "Internal name space");
				c.assertEqual(vocabulary.isChosenFromList(), true, "Chosen from list");
				c.assertEqual(vocabulary.getUrlTemplate(), "https://www.ethz.ch", "URL template");
			}

			testCreate(c, fCreate, c.findVocabulary, fCheck);
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

		QUnit.test("createExternalDataManagementSystem()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EDMS");

			var fCreate = function(facade) {
				var edmsCreation = new c.ExternalDmsCreation();
				edmsCreation.setCode(code);
				edmsCreation.setLabel("Test EDMS");
				edmsCreation.setAddressType(c.ExternalDmsAddressType.FILE_SYSTEM);
				edmsCreation.setAddress("host:my/path")
				return facade.createExternalDms([ edmsCreation ]);
			}

			var fCheck = function(edms) {
				c.assertEqual(edms.getCode(), code, "EDMS code");
				c.assertEqual(edms.getLabel(), "Test EDMS", "EDMS label");
				c.assertEqual(edms.getAddress(), "host:my/path", "EDMS address");
				c.assertEqual(edms.getUrlTemplate(), "host:my/path", "EDMS URL template");
				c.assertEqual(edms.getAddressType(), "FILE_SYSTEM", "EDMS address type");
				c.assertEqual(edms.isOpenbis(), false, "EDMS is openBIS");
			}

			testCreate(c, fCreate, c.findExternalDms, fCheck);
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

		QUnit.test("createAuthorizationGroups()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("AUTHORIZATION_GROUP");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var creation = new c.AuthorizationGroupCreation();
				creation.setCode(code);
				creation.setDescription(description);
				creation.setUserIds([ new c.PersonPermId("power_user") ]);
				return facade.createAuthorizationGroups([ creation ]);
			}

			var fCheck = function(group) {
				c.assertEqual(group.getCode(), code, "Code");
				c.assertEqual(group.getDescription(), description, "Description");
				var users = $.map(group.getUsers(), function(user) {
					return user.getUserId();
				});
				users.sort();
				c.assertEqual(users.toString(), "power_user", "Users");
			}

			testCreate(c, fCreate, c.findAuthorizationGroup, fCheck);
		});

		QUnit.test("createRoleAssignments() for space user", function(assert) {
			var c = new common(assert, openbis);

			var fCreate = function(facade) {
				return c.createSpace(facade).then(function(spaceId) {
					var creation = new c.RoleAssignmentCreation();
					creation.setRole(c.Role.POWER_USER);
					creation.setUserId(new c.PersonPermId("power_user"));
					creation.setSpaceId(spaceId);
					return facade.createRoleAssignments([ creation ]);
				});
			}

			var fCheck = function(roleAssignment) {
				c.assertEqual(roleAssignment.getUser().getUserId(), "power_user", "User");
				c.assertEqual(roleAssignment.getRole(), c.Role.POWER_USER, "Role");
				c.assertEqual(roleAssignment.getRoleLevel(), c.RoleLevel.SPACE, "Role level");
				c.assertEqual(roleAssignment.getRegistrator().getUserId(), "openbis_test_js", "Registrator");
			}

			testCreate(c, fCreate, c.findRoleAssignment, fCheck);
		});

		QUnit.test("createPersons()", function(assert) {
			var c = new common(assert, openbis);
			var userId = c.generateId("user");

			var fCreate = function(facade) {
				var personCreation = new c.PersonCreation();
				personCreation.setUserId(userId);
				personCreation.setSpaceId(new c.SpacePermId("TEST"))
				return facade.createPersons([ personCreation ]);
			}

			var fCheck = function(person) {
				c.assertEqual(person.getUserId(), userId, "User id");
				c.assertEqual(person.getRegistrator().getUserId(), "openbis_test_js", "Registrator");
				c.assertEqual(person.isActive(), true, "User active");
				c.assertEqual(person.getSpace().getCode(), "TEST", "Home space");
			}

			testCreate(c, fCreate, c.findPerson, fCheck);
		});

		var createSemanticAnnotationCreation = function(c) {
			var creation = new c.SemanticAnnotationCreation();
			creation.setPredicateOntologyId("jsPredicateOntologyId");
			creation.setPredicateOntologyVersion("jsPredicateOntologyVersion");
			creation.setPredicateAccessionId("jsPredicateAccessionId");
			creation.setDescriptorOntologyId("jsDescriptorOntologyId");
			creation.setDescriptorOntologyVersion("jsDescriptorOntologyVersion");
			creation.setDescriptorAccessionId("jsDescriptorAccessionId");
			return creation;
		}

		var checkCreatedSemanticAnnotation = function(c, annotation) {
			c.assertEqual(annotation.getPredicateOntologyId(), "jsPredicateOntologyId", "Predicate ontology id");
			c.assertEqual(annotation.getPredicateOntologyVersion(), "jsPredicateOntologyVersion", "Predicate ontology version");
			c.assertEqual(annotation.getPredicateAccessionId(), "jsPredicateAccessionId", "Predicate accession id");
			c.assertEqual(annotation.getDescriptorOntologyId(), "jsDescriptorOntologyId", "Descriptor ontology id");
			c.assertEqual(annotation.getDescriptorOntologyVersion(), "jsDescriptorOntologyVersion", "Descriptor ontology version");
			c.assertEqual(annotation.getDescriptorAccessionId(), "jsDescriptorAccessionId", "Descriptor accession id");
		}

		QUnit.test("createSemanticAnnotations() with entity type id", function(assert) {
			var c = new common(assert, openbis);

			var fCreate = function(facade) {
				var creation = createSemanticAnnotationCreation(c);
				creation.setEntityTypeId(new c.EntityTypePermId("UNKNOWN", "SAMPLE"));
				return facade.createSemanticAnnotations([ creation ]);
			}

			var fCheck = function(annotation) {
				checkCreatedSemanticAnnotation(c, annotation);
				c.assertEqual(annotation.getEntityType().getCode(), "UNKNOWN", "Entity type code");
			}

			testCreate(c, fCreate, c.findSemanticAnnotation, fCheck);
		});

		QUnit.test("createSemanticAnnotations() with property type id", function(assert) {
			var c = new common(assert, openbis);

			var fCreate = function(facade) {
				var creation = createSemanticAnnotationCreation(c);
				creation.setPropertyTypeId(new c.PropertyTypePermId("CONCENTRATION"));
				return facade.createSemanticAnnotations([ creation ]);
			}

			var fCheck = function(annotation) {
				checkCreatedSemanticAnnotation(c, annotation);
				c.assertEqual(annotation.getPropertyType().getCode(), "CONCENTRATION", "Property type code");
			}

			testCreate(c, fCreate, c.findSemanticAnnotation, fCheck);
		});

		QUnit.test("createSemanticAnnotations() with property assignment id", function(assert) {
			var c = new common(assert, openbis);

			var fCreate = function(facade) {
				var creation = createSemanticAnnotationCreation(c);
				creation.setPropertyAssignmentId(new c.PropertyAssignmentPermId(new c.EntityTypePermId("ILLUMINA_FLOW_CELL", "SAMPLE"), new c.PropertyTypePermId("RUNNINGTIME")));
				return facade.createSemanticAnnotations([ creation ]);
			}

			var fCheck = function(annotation) {
				checkCreatedSemanticAnnotation(c, annotation);
				c.assertEqual(annotation.getPropertyAssignment().getEntityType().getCode(), "ILLUMINA_FLOW_CELL", "Entity type code");
				c.assertEqual(annotation.getPropertyAssignment().getEntityType().getPermId().getEntityKind(), "SAMPLE", "Entity type kind");
				c.assertEqual(annotation.getPropertyAssignment().getPropertyType().getCode(), "RUNNINGTIME", "Property type code");
			}

			testCreate(c, fCreate, c.findSemanticAnnotation, fCheck);
		});

		QUnit.test("createUploadedDataSet()", function(assert) {
			var c = new common(assert, openbis);

			var fCreate = function(facade) {

				// unfortunately old Firefox that is used together with our
				// Selenium tests does not allow to use FormData class which
				// would make constructing form data much easier

				var formData = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file\"; filename=\"file1.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\ncontent1\r\n"
						+ "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"file\"; filename=\"file2.txt\"\r\nContent-Type: text/plain\r\n\r\n\r\ncontent2\r\n"
						+ "------WebKitFormBoundary7MA4YWxkTrZu0gW--";

				var dataStoreFacade = facade.getDataStoreFacade("DSS1");

				return dataStoreFacade.createDataSetUpload("UNKNOWN").then(function(upload) {
					return $.ajax({
						url : upload.url,
						type : "POST",
						processData : false,
						contentType : "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW",
						data : formData
					}).then(function() {
						return c.createExperiment(facade).then(function(experimentPermId) {
							var creation = new c.UploadedDataSetCreation();
							creation.setUploadId(upload.id);
							creation.setTypeId(new c.EntityTypePermId(upload.dataSetType));
							creation.setExperimentId(experimentPermId);
							creation.setProperty("DESCRIPTION", "test description");
							creation.setParentIds([ new c.DataSetPermId("20130424111751432-431") ]);
							return dataStoreFacade.createUploadedDataSet(creation).then(function(permId) {
								return [ permId ];
							})
						});
					});
				});
			}

			var fCheck = function(dataSet, facade) {
				return c.waitUntilIndexed(facade, dataSet.getCode(), 10000).then(function() {
					var dataStoreFacade = facade.getDataStoreFacade("DSS1");

					var criteria = new c.DataSetFileSearchCriteria();
					criteria.withDataSet().withCode().thatEquals(dataSet.getCode());

					return dataStoreFacade.searchFiles(criteria, c.createDataSetFileFetchOptions()).then(function(result) {
						var files = result.getObjects();
						c.assertEqual(files.length, 5, "Number of files");
						c.assertEqual(files[0].path, "", "Path 0");
						c.assertEqual(files[1].path, "original", "Path 1");
						c.assertEqual(files[2].path, "original/upload", "Path 2");
						c.assertEqual(files[3].path, "original/upload/file1.txt", "Path 3");
						c.assertEqual(files[4].path, "original/upload/file2.txt", "Path 4");

						c.assertEqual(dataSet.getType().getCode(), "UNKNOWN", "Type code");
						c.assertEqual(dataSet.getProperty("DESCRIPTION"), "test description", "'DESCRIPTION' property value");
						c.assertEqual(dataSet.getParents().length, 1, "Number of parents");
						c.assertEqual(dataSet.getParents()[0].getCode(), "20130424111751432-431", "Parent code");
					});
				});
			}

			testCreate(c, fCreate, c.findDataSet, fCheck);
		});

	}

	return function() {
		executeModule("Create tests", openbis);
		executeModule("Create tests (executeOperations)", openbisExecuteOperations);
	}
});
