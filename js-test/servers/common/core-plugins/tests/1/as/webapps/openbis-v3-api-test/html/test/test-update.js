define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testUpdate = function(c, fCreate, fUpdate, fFind, fCheck, fCheckError) {
			c.start();

			var ctx = {
				facade : null,
				permIds : null
			};
			c.createFacadeAndLogin().then(function(facade) {
				ctx.facade = facade;
				return fCreate(facade)
			}).then(function(permIds) {
				ctx.permIds = permIds;
				c.assertTrue(permIds != null && permIds.length == 1, "Entity was created");
				return fFind(ctx.facade, permIds[0])
			}).then(function(entity) {
				c.assertNotNull(entity, "Entity can be found");
				return fUpdate(ctx.facade, ctx.permIds[0])
			}).then(function() {
				c.ok("Entity was updated");
				return fFind(ctx.facade, ctx.permIds[0])
			}).then(function(entity) {
				if (fCheck) {
					fCheck(entity);
				}
				c.finish();
			}).fail(function(error) {
				if (fCheckError) {
					fCheckError(error.message, ctx.permIds[0]);
				} else {
					c.fail(error.message);
				}
				c.finish();
			});
		}

		QUnit.test("updateSpaces()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("SPACE");

			var fCreate = function(facade) {
				var creation = new c.SpaceCreation();
				creation.setCode(code);
				creation.setDescription("test description");
				return facade.createSpaces([ creation ]);
			}

			var fUpdate = function(facade, permId) {
				var update = new c.SpaceUpdate();
				update.setSpaceId(permId);
				update.setDescription("test description 2");
				return facade.updateSpaces([ update ]);
			}

			var fCheck = function(space) {
				c.assertEqual(space.getCode(), code, "Code");
				c.assertEqual(space.getDescription(), "test description 2", "Description");
			}

			testUpdate(c, fCreate, fUpdate, c.findSpace, fCheck);
		});

		QUnit.test("updateProjects() added attachments", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new c.ProjectCreation();
				projectCreation.setSpaceId(new c.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				return facade.createProjects([ projectCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var projectUpdate = new c.ProjectUpdate();
				projectUpdate.setProjectId(permId);
				projectUpdate.setSpaceId(new c.SpacePermId("PLATONIC"));
				attachmentCreation = new c.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world"));
				projectUpdate.getAttachments().add([ attachmentCreation ]);
				return facade.updateProjects([ projectUpdate ]);
			}

			var fCheck = function(project) {
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "PLATONIC", "Space code");
				c.assertEqual(project.getDescription(), "JS test project", "Description");
				var attachments = project.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
			}

			testUpdate(c, fCreate, fUpdate, c.findProject, fCheck);
		});

		QUnit.test("updateProjects() changed attributes", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new c.ProjectCreation();
				projectCreation.setSpaceId(new c.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				return facade.createProjects([ projectCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var projectUpdate = new c.ProjectUpdate();
				projectUpdate.setProjectId(permId);
				projectUpdate.setDescription("test_description");
				return facade.updateProjects([ projectUpdate ]);
			}

			var fCheck = function(project) {
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(project.getDescription(), "test_description", "Description");
			}

			testUpdate(c, fCreate, fUpdate, c.findProject, fCheck);
		});

		QUnit.test("updateProjects() removed space", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("PROJECT");

			var fCreate = function(facade) {
				var projectCreation = new c.ProjectCreation();
				projectCreation.setSpaceId(new c.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				return facade.createProjects([ projectCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var projectUpdate = new c.ProjectUpdate();
				projectUpdate.setProjectId(permId);
				projectUpdate.setSpaceId(null);
				return facade.updateProjects([ projectUpdate ]);
			}

			var fCheckError = function(error, permId) {
				c.assertContains(error, "Space id cannot be null", "Error");
			}

			testUpdate(c, fCreate, fUpdate, c.findProject, null, fCheckError);
		});

		QUnit.test("updateExperiments() changed attributes + added tag + added attachment", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new c.ExperimentCreation();
				experimentCreation.setCode(code);
				experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "EXPRESSION");
				experimentCreation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
				return facade.createExperiments([ experimentCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var experimentUpdate = new c.ExperimentUpdate();
				experimentUpdate.setExperimentId(permId);
				experimentUpdate.setProjectId(new c.ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"));
				experimentUpdate.getTagIds().add(new c.TagCode("CREATE_ANOTHER_JSON_TAG"));
				attachmentCreation = new c.AttachmentCreation();
				attachmentCreation.setFileName("test_file");
				attachmentCreation.setTitle("test_title");
				attachmentCreation.setDescription("test_description");
				attachmentCreation.setContent(btoa("hello world"));
				experimentUpdate.getAttachments().add([ attachmentCreation ]);
				return facade.updateExperiments([ experimentUpdate ]);
			}

			var fCheck = function(experiment) {
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "SCREENING-EXAMPLES", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "PLATONIC", "Space code");
				var tags = _.sortBy(experiment.getTags(), "code");
				c.assertEqual(tags[0].code, 'CREATE_ANOTHER_JSON_TAG', "tags");
				c.assertEqual(tags[1].code, 'CREATE_JSON_TAG', "tags");
				c.assertEqual(tags.length, 2, "Number of tags");
				var attachments = experiment.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, fCheck);
		});

		QUnit.test("updateExperiments() changed properties + removed tag", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new c.ExperimentCreation();
				experimentCreation.setCode(code);
				experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setProperty("EXPERIMENT_DESIGN", "EXPRESSION");
				experimentCreation.setTagIds([ new c.TagCode("CREATE_JSON_TAG") ]);
				experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
				return facade.createExperiments([ experimentCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var experimentUpdate = new c.ExperimentUpdate();
				experimentUpdate.setExperimentId(permId);
				experimentUpdate.setProperty("EXPERIMENT_DESIGN", "OTHER");
				experimentUpdate.getTagIds().remove([ new c.TagCode("UNKNOWN_TAG"), new c.TagCode("CREATE_JSON_TAG") ]);
				return facade.updateExperiments([ experimentUpdate ]);
			}

			var fCheck = function(experiment) {
				c.assertEqual(experiment.getCode(), code, "Experiment code");
				c.assertEqual(experiment.getType().getCode(), "HT_SEQUENCING", "Type code");
				c.assertEqual(experiment.getProject().getCode(), "TEST-PROJECT", "Project code");
				c.assertEqual(experiment.getProject().getSpace().getCode(), "TEST", "Space code");
				var properties = experiment.getProperties();
				c.assertEqual(properties["EXPERIMENT_DESIGN"], "OTHER", "Property EXPERIMENT_DESIGN");
				c.assertEqual(Object.keys(properties), "EXPERIMENT_DESIGN", "Properties");
				c.assertEqual(experiment.getTags().length, 0, "Number of tags");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, fCheck);
		});

		QUnit.test("updateExperiments() removed project", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EXPERIMENT");

			var fCreate = function(facade) {
				var experimentCreation = new c.ExperimentCreation();
				experimentCreation.setCode(code);
				experimentCreation.setTypeId(new c.EntityTypePermId("HT_SEQUENCING"));
				experimentCreation.setProjectId(new c.ProjectIdentifier("/TEST/TEST-PROJECT"));
				return facade.createExperiments([ experimentCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var experimentUpdate = new c.ExperimentUpdate();
				experimentUpdate.setExperimentId(permId);
				experimentUpdate.setProjectId(null);
				return facade.updateExperiments([ experimentUpdate ]);
			}

			var fCheckError = function(error, permId) {
				c.assertContains(error, "Project id cannot be null", "Error");
			}

			testUpdate(c, fCreate, fUpdate, c.findExperiment, null, fCheckError);
		});

		QUnit.test("updateSamples()", function(assert) {
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

			var fUpdate = function(facade, permId) {
				var update = new c.SampleUpdate();
				update.setSampleId(permId);
				update.getTagIds().remove(new c.TagCode("CREATE_JSON_TAG"));
				update.getTagIds().add(new c.TagCode("CREATE_JSON_TAG_2"));
				update.getTagIds().add(new c.TagCode("CREATE_JSON_TAG_3"));
				return facade.updateSamples([ update ]);
			}

			var fCheck = function(sample) {
				c.assertEqual(sample.getCode(), code, "Sample code");
				c.assertEqual(sample.getType().getCode(), "UNKNOWN", "Type code");
				c.assertEqual(sample.getSpace().getCode(), "TEST", "Space code");
				c.assertObjectsCount(sample.getTags(), 2);
				c.assertObjectsWithValues(sample.getTags(), "code", [ "CREATE_JSON_TAG_2", "CREATE_JSON_TAG_3" ]);
			}

			testUpdate(c, fCreate, fUpdate, c.findSample, fCheck);
		});

		QUnit.test("updateDataSets()", function(assert) {
			var c = new common(assert, openbis);
			var code = null;

			var fCreate = function(facade) {
				return c.createDataSet(facade).then(function(permId) {
					code = permId.getPermId();
					return [ permId ];
				});
			}

			var fUpdate = function(facade, permId) {
				var physicalUpdate = new c.PhysicalDataUpdate();
				physicalUpdate.setFileFormatTypeId(new c.FileFormatTypePermId("TIFF"));

				var update = new c.DataSetUpdate();
				update.setDataSetId(permId);
				update.setProperty("NOTES", "new 409 description");
				update.setPhysicalData(physicalUpdate);

				return facade.updateDataSets([ update ]);
			}

			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Code");
				c.assertEqual(dataSet.getProperties()["NOTES"], "new 409 description", "Property NOTES");
				c.assertEqual(dataSet.getPhysicalData().getFileFormatType().getCode(), "TIFF", "File format type");
			}

			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});

		QUnit.test("updateDataSets() link data set", function(assert) {
			var c = new common(assert, openbis);
			var code = "20160613195437233-437";
			
			var fCreate = function(facade) {
				return [new c.DataSetPermId(code)];
			}

			var fUpdate = function(facade, permId) {
				var linkUpdate = new c.LinkedDataUpdate();
				linkUpdate.setExternalCode("new code");
				
				var update = new c.DataSetUpdate();
				update.setDataSetId(permId);
				update.setLinkedData(linkUpdate);
				
				return facade.updateDataSets([ update ]);
			}
			
			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Code");
				c.assertEqual(dataSet.getLinkedData().getExternalCode(), "new code", "External code");
				c.assertEqual(dataSet.getLinkedData().getExternalDms().getPermId().toString(), "DMS_1", "External DMS");
			}
			
			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});
		
		QUnit.test("updateDataSets() add content copy to link data set", function(assert) {
			var c = new common(assert, openbis);
			var edmsId = null;
			
			var fCreate = function(facade) {
				return c.createLinkDataSet(facade, "root/path", "my-hash", "my-repository-id").then(function(permId) {
					code = permId.getPermId();
					return [ permId ];
				});
			}

			var fUpdate = function(facade, permId) {
				return c.findDataSet(facade, permId).then(function(dataSet) {
					var contentCopy = dataSet.getLinkedData().getContentCopies()[0];
					edmsId = contentCopy.getExternalDms().getPermId();
					var contentCopyListUpdateValue = new c.ContentCopyListUpdateValue();
					var contentCopyCreation = new c.ContentCopyCreation();
					contentCopyCreation.setExternalDmsId(edmsId);
					contentCopyCreation.setPath("my/data/path");
					contentCopyCreation.setGitCommitHash("my-git-hash");
					contentCopyCreation.setGitRepositoryId("my-git-repository-id");
					contentCopyListUpdateValue.add([contentCopyCreation]);
					contentCopyListUpdateValue.remove([contentCopy.getId()]);
					
					var linkUpdate = new c.LinkedDataUpdate();
					linkUpdate.setContentCopyActions(contentCopyListUpdateValue.getActions());
					
					var update = new c.DataSetUpdate();
					update.setDataSetId(permId);
					update.setLinkedData(linkUpdate);
					
					return facade.updateDataSets([ update ]);
				});
			}
			
			var fCheck = function(dataSet) {
				c.assertEqual(dataSet.getCode(), code, "Code");
				var contentCopies = dataSet.getLinkedData().getContentCopies();
				c.assertEqual(contentCopies.length, 1, "Number of content copies");
				c.assertEqual(contentCopies[0].getExternalDms().getPermId().toString(), edmsId.toString(), "External DMS");
				c.assertEqual(contentCopies[0].getExternalCode(), null, "External code");
				c.assertEqual(contentCopies[0].getPath(), "/my/data/path", "Path");
				c.assertEqual(contentCopies[0].getGitCommitHash(), "my-git-hash", "Git commit hash");
				c.assertEqual(contentCopies[0].getGitRepositoryId(), "my-git-repository-id", "Git repository id");
			}
			
			testUpdate(c, fCreate, fUpdate, c.findDataSet, fCheck);
		});
		
		QUnit.test("updateMaterials()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("MATERIAL");

			var fCreate = function(facade) {
				var materialCreation = new c.MaterialCreation();
				materialCreation.setTypeId(new c.EntityTypePermId("COMPOUND"));
				materialCreation.setCode(code);
				materialCreation.setProperty("DESCRIPTION", "Metal");
				return facade.createMaterials([ materialCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var materialUpdate = new c.MaterialUpdate();
				materialUpdate.setMaterialId(permId);
				materialUpdate.setProperty("DESCRIPTION", "Alloy");
				return facade.updateMaterials([ materialUpdate ]);
			}

			var fCheck = function(material) {
				c.assertEqual(material.getCode(), code, "Material code");
				c.assertEqual(material.getType().getCode(), "COMPOUND", "Type code");
				var properties = material.getProperties();
				c.assertEqual(properties["DESCRIPTION"], "Alloy", "Property DESCRIPTION");
			}

			testUpdate(c, fCreate, fUpdate, c.findMaterial, fCheck);
		});

		QUnit.test("updateVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("VOCABULARY_TERM");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var termCreation = new c.VocabularyTermCreation();
				termCreation.setVocabularyId(new c.VocabularyPermId("TEST-VOCABULARY"));
				termCreation.setCode(code);
				return facade.createVocabularyTerms([ termCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var termUpdate = new c.VocabularyTermUpdate();
				termUpdate.setVocabularyTermId(permId);
				termUpdate.setDescription(description);
				return facade.updateVocabularyTerms([ termUpdate ]);
			}

			var fCheck = function(term) {
				c.assertEqual(term.getCode(), code, "Term code");
				c.assertEqual(term.getVocabulary().getCode(), "TEST-VOCABULARY", "Term vocabulary code");
				c.assertEqual(term.getDescription(), description, "Term description");
			}

			testUpdate(c, fCreate, fUpdate, c.findVocabularyTerm, fCheck);
		});
		
		QUnit.test("updateExternalDms()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("EDMS");
			
			var fCreate = function(facade) {
				var edmsCreation = new c.ExternalDmsCreation();
				edmsCreation.setCode(code);
				edmsCreation.setLabel("Test EDMS");
				edmsCreation.setAddressType(c.ExternalDmsAddressType.OPENBIS);
				edmsCreation.setAddress("https://my-site/q=${term}")
				return facade.createExternalDms([ edmsCreation ]);
			}
			
			var fUpdate = function(facade, permId) {
				var edmsUpdate = new c.ExternalDmsUpdate();
				edmsUpdate.setExternalDmsId(permId);
				edmsUpdate.setLabel("Test EDMS 2");
				edmsUpdate.setAddress("https://my-second-site/q=${term}");
				return facade.updateExternalDataManagementSystems([ edmsUpdate ]);
			}
			
			var fCheck = function(edms) {
				c.assertEqual(edms.getCode(), code, "EDMS code");
				c.assertEqual(edms.getLabel(), "Test EDMS 2", "EDMS label");
				c.assertEqual(edms.getAddress(), "https://my-second-site/q=${term}", "EDMS address");
				c.assertEqual(edms.getUrlTemplate(), "https://my-second-site/q=${term}", "EDMS URL template");
				c.assertEqual(edms.getAddressType(), "OPENBIS", "EDMS address type");
				c.assertEqual(edms.isOpenbis(), true, "EDMS is openBIS");
			}
			
			testUpdate(c, fCreate, fUpdate, c.findExternalDms, fCheck);
		});

		QUnit.test("updateTags()", function(assert) {
			var c = new common(assert, openbis);
			var code = c.generateId("TAG");
			var description = "Description of " + code;

			var fCreate = function(facade) {
				var tagCreation = new c.TagCreation();
				tagCreation.setCode(code);
				return facade.createTags([ tagCreation ]);
			}

			var fUpdate = function(facade, permId) {
				var tagUpdate = new c.TagUpdate();
				tagUpdate.setTagId(permId);
				tagUpdate.setDescription(description);
				return facade.updateTags([ tagUpdate ]);
			}

			var fCheck = function(tag) {
				c.assertEqual(tag.getCode(), code, "Tag code");
				c.assertEqual(tag.getDescription(), description, "Tag description");
			}

			testUpdate(c, fCreate, fUpdate, c.findTag, fCheck);
		});

		QUnit.test("updateOperationExecutions()", function(assert) {
			var c = new common(assert, openbis);
			var permId = null;

			var fCreate = function(facade) {
				return c.createOperationExecution(facade).then(function(id) {
					permId = id;
					return [ id ];
				});
			}

			var fUpdate = function(facade, permId) {
				var executionUpdate = new c.OperationExecutionUpdate();
				executionUpdate.setExecutionId(permId);
				executionUpdate.setDescription("Description " + permId.getPermId());
				return facade.updateOperationExecutions([ executionUpdate ]);
			}

			var fCheck = function(execution) {
				c.assertEqual(execution.getPermId().getPermId(), permId.getPermId(), "PermId");
				c.assertEqual(execution.getDescription(), "Description " + permId.getPermId(), "Execution description");
			}

			testUpdate(c, fCreate, fUpdate, c.findOperationExecution, fCheck);
		});

	}

	return function() {
		executeModule("Update tests", openbis);
		executeModule("Update tests (executeOperations)", openbisExecuteOperations);
	}
});
