define([ 'jquery', 'underscore', 'openbis', 'test/common', 'dto/id/entitytype/EntityTypePermId', 'dto/entity/space/SpaceCreation', 'dto/entity/project/ProjectCreation',
		'dto/entity/experiment/ExperimentCreation', 'dto/entity/sample/SampleCreation', 'dto/entity/material/MaterialCreation', 'dto/deletion/space/SpaceDeletionOptions',
		'dto/deletion/project/ProjectDeletionOptions', 'dto/deletion/experiment/ExperimentDeletionOptions', 'dto/deletion/sample/SampleDeletionOptions',
		'dto/deletion/material/MaterialDeletionOptions', 'dto/fetchoptions/deletion/DeletionFetchOptions' ], function($, _, openbis, common, EntityTypePermId, SpaceCreation, ProjectCreation,
		ExperimentCreation, SampleCreation, MaterialCreation, SpaceDeletionOptions, ProjectDeletionOptions, ExperimentDeletionOptions, SampleDeletionOptions, MaterialDeletionOptions,
		DeletionFetchOptions) {
	return function() {
		QUnit.module("Deletion tests");

		var createSpace = function(c, facade) {
			var creation = new SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));
			return c.createSpaceFetchOptions().then(function(fetchOptions) {
				return facade.createSpaces([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		};

		var createProject = function(c, facade) {
			return createSpace(c, facade).then(function(spacePermId) {

				var creation = new ProjectCreation();
				creation.setCode("CREATE_JSON_PROJECT_" + (new Date().getTime()));
				creation.setSpaceId(spacePermId);

				return c.createProjectFetchOptions().then(function(fetchOptions) {
					return facade.createProjects([ creation ]).then(function(permIds) {
						return permIds[0];
					});
				});
			});
		};

		var createExperiment = function(c, facade) {
			return createProject(c, facade).then(function(projectPermId) {

				var creation = new ExperimentCreation();
				creation.setCode("CREATE_JSON_EXPERIMENT_" + (new Date().getTime()));
				creation.setTypeId(new EntityTypePermId("UNKNOWN"));
				creation.setProjectId(projectPermId);

				return c.createExperimentFetchOptions().then(function(fetchOptions) {
					return facade.createExperiments([ creation ]).then(function(permIds) {
						return permIds[0];
					});
				});
			});
		};

		var createSample = function(c, facade) {
			return createSpace(c, facade).then(function(spacePermId) {

				var creation = new SampleCreation();
				creation.setCode("CREATE_JSON_SAMPLE_" + (new Date().getTime()));
				creation.setTypeId(new EntityTypePermId("UNKNOWN"));
				creation.setSpaceId(spacePermId);

				return c.createSampleFetchOptions().then(function(fetchOptions) {
					return facade.createSamples([ creation ]).then(function(permIds) {
						return permIds[0];
					});
				});
			});
		};

		var createMaterial = function(c, facade) {
			var creation = new MaterialCreation();
			creation.setCode("CREATE_JSON_MATERIAL_" + (new Date().getTime()));
			creation.setTypeId(new EntityTypePermId("COMPOUND"));

			return c.createMaterialFetchOptions().then(function(fetchOptions) {
				return facade.createMaterials([ creation ]).then(function(permIds) {
					return permIds[0];
				});
			});
		};

		var findSpace = function(c, facade, id) {
			return $.when(c.createSpaceFetchOptions()).then(function(fetchOptions) {
				return facade.mapSpaces([ id ], fetchOptions).then(function(spaces) {
					return spaces[id];
				});
			});
		};

		var findProject = function(c, facade, id) {
			return $.when(c.createProjectFetchOptions()).then(function(fetchOptions) {
				return facade.mapProjects([ id ], fetchOptions).then(function(projects) {
					return projects[id];
				});
			});
		};

		var findExperiment = function(c, facade, id) {
			return $.when(c.createExperimentFetchOptions()).then(function(fetchOptions) {
				return facade.mapExperiments([ id ], fetchOptions).then(function(experiments) {
					return experiments[id];
				});
			});
		};

		var findSample = function(c, facade, id) {
			return $.when(c.createSampleFetchOptions()).then(function(fetchOptions) {
				return facade.mapSamples([ id ], fetchOptions).then(function(samples) {
					return samples[id];
				});
			});
		};

		var findMaterial = function(c, facade, id) {
			return $.when(c.createMaterialFetchOptions()).then(function(fetchOptions) {
				return facade.mapMaterials([ id ], fetchOptions).then(function(materials) {
					return materials[id];
				});
			});
		};

		var deleteSpace = function(c, facade, id) {
			var options = new SpaceDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSpaces([ id ], options);
		};

		var deleteProject = function(c, facade, id) {
			var options = new ProjectDeletionOptions();
			options.setReason("test reason");
			return facade.deleteProjects([ id ], options);
		};

		var deleteExperiment = function(c, facade, id) {
			var options = new ExperimentDeletionOptions();
			options.setReason("test reason");
			return facade.deleteExperiments([ id ], options);
		};

		var deleteSample = function(c, facade, id) {
			var options = new SampleDeletionOptions();
			options.setReason("test reason");
			return facade.deleteSamples([ id ], options);
		};

		var deleteMaterial = function(c, facade, id) {
			var options = new MaterialDeletionOptions();
			options.setReason("test reason");
			return facade.deleteMaterials([ id ], options);
		};

		var testDeleteWithoutTrash = function(assert, fCreate, fFind, fDelete) {
			var c = new common(assert);
			var done = assert.async();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(c, facade).then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return fFind(c, facade, permId).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						return facade.listDeletions(new DeletionFetchOptions()).then(function(beforeDeletions) {
							c.ok("Got before deletions");
							return fDelete(c, facade, permId).then(function() {
								c.ok("Entity was deleted");
								return facade.listDeletions(new DeletionFetchOptions()).then(function(afterDeletions) {
									c.ok("Got after deletions");
									c.assertEqual(beforeDeletions.length, afterDeletions.length, "No new deletions found");
									return fFind(c, facade, permId).then(function(entityAfterDeletion) {
										c.assertNull(entityAfterDeletion, "Entity was deleted");
										done();
									});
								});
							});
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		}

		var testDeleteWithTrashAndRevert = function(assert, fCreate, fFind, fDelete) {
			var c = new common(assert);
			var done = assert.async();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(c, facade).then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return fFind(c, facade, permId).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						return facade.listDeletions(new DeletionFetchOptions()).then(function(beforeDeletions) {
							c.ok("Got before deletions");
							return fDelete(c, facade, permId).then(function(deletionId) {
								c.ok("Entity was deleted");
								return facade.listDeletions(new DeletionFetchOptions()).then(function(afterDeletions) {
									c.ok("Got after deletions");
									c.assertEqual(afterDeletions.length, beforeDeletions.length + 1, "One new deletion");
									c.assertEqual(afterDeletions[afterDeletions.length - 1].getId().getTechId(), deletionId.getTechId(), "Deletion ids match");
									return fFind(c, facade, permId).then(function(entityAfterDeletion) {
										c.assertNull(entityAfterDeletion, "Entity was deleted");
										return facade.revertDeletions([ deletionId ]).then(function() {
											c.ok("Reverted deletion");
											return fFind(c, facade, permId).then(function(entityAfterRevert) {
												c.assertNotNull(entityAfterRevert, "Entity is back");
												done();
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
				done();
			});
		}

		var testDeleteWithTrashAndConfirm = function(assert, fCreate, fFind, fDelete) {
			var c = new common(assert);
			var done = assert.async();

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(c, facade).then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return fFind(c, facade, permId).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						return facade.listDeletions(new DeletionFetchOptions()).then(function(deletionsBeforeDeletion) {
							c.ok("Got before deletions");
							return fDelete(c, facade, permId).then(function(deletionId) {
								c.ok("Entity was deleted");
								return facade.listDeletions(new DeletionFetchOptions()).then(function(deletionsAfterDeletion) {
									c.ok("Got after deletions");
									c.assertEqual(deletionsAfterDeletion.length, deletionsBeforeDeletion.length + 1, "One new deletion");
									c.assertEqual(deletionsAfterDeletion[deletionsAfterDeletion.length - 1].getId().getTechId(), deletionId.getTechId(), "Deletion ids match");
									return fFind(c, facade, permId).then(function(entityAfterDeletion) {
										c.assertNull(entityAfterDeletion, "Entity was deleted");
										return facade.confirmDeletions([ deletionId ]).then(function() {
											c.ok("Confirmed deletion");
											return fFind(c, facade, permId).then(function(entityAfterConfirm) {
												c.assertNull(entityAfterConfirm, "Entity is still gone");
												return facade.listDeletions(new DeletionFetchOptions()).then(function(deletionsAfterConfirm) {
													c.assertEqual(deletionsAfterConfirm.length, deletionsBeforeDeletion.length, "New deletion is also gone");
													done();
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
				done();
			});
		}

		QUnit.test("deleteSpaces()", function(assert) {
			testDeleteWithoutTrash(assert, createSpace, findSpace, deleteSpace);
		});

		QUnit.test("deleteProjects()", function(assert) {
			testDeleteWithoutTrash(assert, createProject, findProject, deleteProject);
		});

		QUnit.test("deleteExperiments() with revert", function(assert) {
			testDeleteWithTrashAndRevert(assert, createExperiment, findExperiment, deleteExperiment);
		});

		QUnit.test("deleteExperiments() with confirm", function(assert) {
			testDeleteWithTrashAndConfirm(assert, createExperiment, findExperiment, deleteExperiment);
		});

		QUnit.test("deleteSamples() with revert", function(assert) {
			testDeleteWithTrashAndRevert(assert, createSample, findSample, deleteSample);
		});

		QUnit.test("deleteSamples() with confirm", function(assert) {
			testDeleteWithTrashAndConfirm(assert, createSample, findSample, deleteSample);
		});

		QUnit.test("deleteMaterials()", function(assert) {
			testDeleteWithoutTrash(assert, createMaterial, findMaterial, deleteMaterial);
		});

	}
});
