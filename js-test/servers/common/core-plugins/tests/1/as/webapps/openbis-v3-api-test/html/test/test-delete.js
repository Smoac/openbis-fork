define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Deletion tests");

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
									c.assertEqual(beforeDeletions.length, afterDeletions.length, "No new deletions found");
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
								return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(afterDeletions) {
									c.ok("Got after deletions");
									c.assertEqual(afterDeletions.length, beforeDeletions.length + 1, "One new deletion");
									c.assertEqual(afterDeletions[afterDeletions.length - 1].getId().getTechId(), deletionId.getTechId(), "Deletion ids match");
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

			c.createFacadeAndLogin().then(function(facade) {
				return fCreate(facade).then(function(permId) {
					c.assertNotNull(permId, "Entity was created");
					return fFind(facade, permId).then(function(entity) {
						c.assertNotNull(entity, "Entity can be found");
						return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(deletionsBeforeDeletion) {
							c.ok("Got before deletions");
							return fDelete(facade, permId).then(function(deletionId) {
								c.ok("Entity was deleted");
								return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(deletionsAfterDeletion) {
									c.ok("Got after deletions");
									c.assertEqual(deletionsAfterDeletion.length, deletionsBeforeDeletion.length + 1, "One new deletion");
									c.assertEqual(deletionsAfterDeletion[deletionsAfterDeletion.length - 1].getId().getTechId(), deletionId.getTechId(), "Deletion ids match");
									return fFind(facade, permId).then(function(entityAfterDeletion) {
										c.assertNull(entityAfterDeletion, "Entity was deleted");
										return facade.confirmDeletions([ deletionId ]).then(function() {
											c.ok("Confirmed deletion");
											return fFind(facade, permId).then(function(entityAfterConfirm) {
												c.assertNull(entityAfterConfirm, "Entity is still gone");
												return facade.searchDeletions(new c.DeletionSearchCriteria(), new c.DeletionFetchOptions()).then(function(deletionsAfterConfirm) {
													c.assertEqual(deletionsAfterConfirm.length, deletionsBeforeDeletion.length, "New deletion is also gone");
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
			var c = new common(assert);
			testDeleteWithoutTrash(c, c.createSpace, c.findSpace, c.deleteSpace);
		});

		QUnit.test("deleteProjects()", function(assert) {
			var c = new common(assert);
			testDeleteWithoutTrash(c, c.createProject, c.findProject, c.deleteProject);
		});

		QUnit.test("deleteExperiments() with revert", function(assert) {
			var c = new common(assert);
			testDeleteWithTrashAndRevert(c, c.createExperiment, c.findExperiment, c.deleteExperiment);
		});

		QUnit.test("deleteExperiments() with confirm", function(assert) {
			var c = new common(assert);
			testDeleteWithTrashAndConfirm(c, c.createExperiment, c.findExperiment, c.deleteExperiment);
		});

		QUnit.test("deleteSamples() with revert", function(assert) {
			var c = new common(assert);
			testDeleteWithTrashAndRevert(c, c.createSample, c.findSample, c.deleteSample);
		});

		QUnit.test("deleteSamples() with confirm", function(assert) {
			var c = new common(assert);
			testDeleteWithTrashAndConfirm(c, c.createSample, c.findSample, c.deleteSample);
		});

		QUnit.test("deleteDataSets() with revert", function(assert) {
			var c = new common(assert);
			testDeleteWithTrashAndRevert(c, c.createDataSet, c.findDataSet, c.deleteDataSet);
		});

		QUnit.test("deleteDataSets() with confirm", function(assert) {
			var c = new common(assert);
			testDeleteWithTrashAndConfirm(c, c.createDataSet, c.findDataSet, c.deleteDataSet);
		});

		QUnit.test("deleteMaterials()", function(assert) {
			var c = new common(assert);
			testDeleteWithoutTrash(c, c.createMaterial, c.findMaterial, c.deleteMaterial);
		});

	}
});
