define([ 'jquery', 'underscore', 'openbis', 'test/openbis-execute-operations', 'test/common' ], function($, _, openbis, openbisExecuteOperations, common) {
	var executeModule = function(moduleName, openbis) {
		QUnit.module(moduleName);

		var testDeleteWithoutTrash = function(c, fCreate, fFind, fDelete) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return fFind(facade, permId).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(beforeDeletions) {
							c.ok("Got before deletions");
							return fDelete(facade, permId).then(function() {
								c.ok("Entity was deleted");
								return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(afterDeletions) {
									c.ok("Got after deletions");
									c.assertEqual(beforeDeletions.getObjects().length, afterDeletions.getObjects().length, "No new deletions found");
									return fFind(facade, permId).then(function(entityAfterDeletion) {
										c.assertNull(entityAfterDeletion, "Entity was deleted");
										c.finish();
									});
								});
							});
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testDeleteWithTrashAndRevert = function(c, fCreate, fFind, fDelete) {
			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return fFind(facade, permId).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(beforeDeletions) {
							c.ok("Got before deletions");
							return fDelete(facade, permId).then(function(deletionId) {
								c.ok("Entity was deleted");
								c.assertNotEqual(deletionId.getTechId(), "", "Deletion tech id not an empty string");
								return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(afterDeletions) {
									c.ok("Got after deletions");
									c.assertEqual(afterDeletions.getObjects().length, beforeDeletions.getObjects().length + 1, "One new deletion");
									c.assertEqual(afterDeletions.getObjects()[afterDeletions.getObjects().length - 1].getId().getTechId(), deletionId.getTechId(), "Deletion ids match");
									return fFind(facade, permId).then(function(entityAfterDeletion) {
										c.assertNull(entityAfterDeletion, "Entity was deleted");
										return facade.revertDeletions([ deletionId ]).then(function() {
											c.ok("Reverted deletion");
											return fFind(facade, permId).then(function(entityAfterRevert) {
												c.assertNotNull(entityAfterRevert, "Entity is back");
												c.finish();
											});
										});
									});
								});
							});
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		var testDeleteWithTrashAndConfirm = function(c, fCreate, fFind, fDelete) {
			c.start();

			c.createFacadeAndLogin().then(
					function(facade) {
						return fCreate(facade).then(
								function(permId) {
									c.assertNotNull(permId, "Entity was created");
									return fFind(facade, permId).then(
											function(entity) {
												c.assertNotNull(entity, "Entity can be found");
												return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(
														function(deletionsBeforeDeletion) {
															c.ok("Got before deletions");
															return fDelete(facade, permId).then(
																	function(deletionId) {
																		c.ok("Entity was deleted");
																		return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(
																				function(deletionsAfterDeletion) {
																					c.ok("Got after deletions");
																					c.assertEqual(deletionsAfterDeletion.getObjects().length, deletionsBeforeDeletion.getObjects().length + 1,
																							"One new deletion");
																					c.assertEqual(deletionsAfterDeletion.getObjects()[deletionsAfterDeletion.getObjects().length - 1].getId()
																							.getTechId(), deletionId.getTechId(), "Deletion ids match");
																					return fFind(facade, permId).then(
																							function(entityAfterDeletion) {
																								c.assertNull(entityAfterDeletion, "Entity was deleted");
																								return facade.confirmDeletions([ deletionId ]).then(
																										function() {
																											c.ok("Confirmed deletion");
																											return fFind(facade, permId).then(
																													function(entityAfterConfirm) {
																														c.assertNull(entityAfterConfirm, "Entity is still gone");
																														return facade.searchDeletions(new c.DeletionSearchCriteria(),
																																new c.DeletionFetchOptions()).then(
																																function(deletionsAfterConfirm) {
																																	c.assertEqual(deletionsAfterConfirm.getObjects().length,
																																			deletionsBeforeDeletion.getObjects().length,
																																			"New deletion is also gone");
																																	c.finish();
																																});
																													});
																										});
																							});
																				});
																	});
														});
											});
								});
					}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		}

		QUnit.test("deleteSpaces()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createSpace, c.findSpace, c.deleteSpace);
		});

		QUnit.test("deleteProjects()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createProject, c.findProject, c.deleteProject);
		});

		QUnit.test("deleteExperiments() with revert", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithTrashAndRevert(c, c.createExperiment, c.findExperiment, c.deleteExperiment);
		});

		QUnit.test("deleteExperiments() with confirm", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithTrashAndConfirm(c, c.createExperiment, c.findExperiment, c.deleteExperiment);
		});

		QUnit.test("deleteSamples() with revert", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithTrashAndRevert(c, c.createSample, c.findSample, c.deleteSample);
		});

		QUnit.test("deleteSamples() with confirm", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithTrashAndConfirm(c, c.createSample, c.findSample, c.deleteSample);
		});

		QUnit.test("deleteDataSets() with revert", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithTrashAndRevert(c, c.createDataSet, c.findDataSet, c.deleteDataSet);
		});

		QUnit.test("deleteDataSets() with confirm", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithTrashAndConfirm(c, c.createDataSet, c.findDataSet, c.deleteDataSet);
		});

		QUnit.test("deleteDataSets() with disallowed type without force flag", function(assert) {
			var c = new common(assert, openbis);

			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return c.createDataSet(facade, "UNKNOWN").then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return c.deleteDataSet(facade, permId).then(function(deletionId) {
						c.assertNotNull(deletionId, "Entity was moved to trash");
						return facade.confirmDeletions([ deletionId ]).then(function() {
							c.fail("Confirmation of deletion should fail without the force flag");
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.assertContains(error.message, "Deletion failed because the following data sets have 'Disallow deletion' flag set to true in their type", "Expected error message");
				c.finish();
			});
		});

		QUnit.test("deleteDataSets() with disallowed type with force flag", function(assert) {
			var c = new common(assert, openbis);

			c.start();

			c.createFacadeAndLogin().then(function(facade) {
				return c.createDataSet(facade, "UNKNOWN").then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return c.deleteDataSet(facade, permId).then(function(deletionId) {
						c.assertNotNull(deletionId, "Entity was moved to trash");
						var operation = new c.ConfirmDeletionsOperation([ deletionId ]);
						operation.setForceDeletion(true);
						var options = new c.SynchronousOperationExecutionOptions();
						return facade.executeOperations([ operation ], options).then(function() {
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail("Confirmation of deletion should not fail with the force flag");
				c.finish();
			});
		});

		QUnit.test("deleteMaterials()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createMaterial, c.findMaterial, c.deleteMaterial);
		});

		QUnit.test("deletePlugins()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createPlugin, c.findPlugin, c.deletePlugin);
		});

		QUnit.test("deletePropertyTypes()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createPropertyType, c.findPropertyType, c.deletePropertyType);
		});

		QUnit.test("deleteVocabularies()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createVocabulary, c.findVocabulary, c.deleteVocabulary);
		});

		QUnit.test("deleteVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createVocabularyTerm, c.findVocabularyTerm, c.deleteVocabularyTerm);
		});

		QUnit.test("deleteExperimentTypes()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createExperimentType, c.findExperimentType, c.deleteExperimentType);
		});

		QUnit.test("deleteSampleTypes()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createSampleType, c.findSampleType, c.deleteSampleType);
		});

		QUnit.test("deleteDataSetTypes()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createDataSetType, c.findDataSetType, c.deleteDataSetType);
		});

		QUnit.test("deleteMaterialTypes()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createMaterialType, c.findMaterialType, c.deleteMaterialType);
		});

		QUnit.test("deleteExternalDms()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createExternalDms, c.findExternalDms, c.deleteExternalDms);
		});

		QUnit.test("replaceVocabularyTerms()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createVocabularyTerm, c.findVocabularyTerm, c.replaceVocabularyTerm);
		});

		QUnit.test("deleteTags()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createTag, c.findTag, c.deleteTag);
		});

		QUnit.test("deleteAuthorizationGroups()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createAuthorizationGroup, c.findAuthorizationGroup, c.deleteAuthorizationGroup);
		});

		QUnit.test("deleteRoleAssignments()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createRoleAssignment, c.findRoleAssignment, c.deleteRoleAssignment);
		});

		QUnit.test("deleteOperationExecutions()", function(assert) {
			var c = new common(assert, openbis);

			var findNotDeletedOrDeletePending = function(facade, permId) {
				return c.findOperationExecution(facade, permId).then(function(execution) {
					if (!execution || execution.getAvailability() == "DELETE_PENDING" || execution.getAvailability() == "DELETED") {
						return null;
					} else {
						return execution;
					}
				});
			}

			testDeleteWithoutTrash(c, c.createOperationExecution, findNotDeletedOrDeletePending, c.deleteOperationExecution);
		});

		QUnit.test("deleteSemanticAnnotations()", function(assert) {
			var c = new common(assert, openbis);
			testDeleteWithoutTrash(c, c.createSemanticAnnotation, c.findSemanticAnnotation, c.deleteSemanticAnnotation);
		});

	}

	return function() {
		executeModule("Deletion tests", openbis);
		executeModule("Deletion tests (executeOperations)", openbisExecuteOperations);
	}
});
