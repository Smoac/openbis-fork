define([ 'jquery', 'underscore', 'openbis', 'test/common', 'dto/entity/space/SpaceCreation', 'dto/entity/space/SpaceUpdate', 'dto/deletion/space/SpaceDeletionOptions' ], function($, _, openbis,
		common, SpaceCreation, SpaceUpdate, SpaceDeletionOptions) {
	return function() {
		QUnit.module("Space tests");

		QUnit.test("createSpaces()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var creation = new SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));
			creation.setDescription("test description");

			$.when(c.createFacadeAndLogin(), c.createSpaceFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createSpaces([ creation ]).then(function(permIds) {
					return facade.mapSpaces(permIds, fetchOptions).done(function() {
						facade.logout()
					});
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 1);

				var space = spaces[creation.getCode()];
				c.assertEqual(space.getCode(), creation.getCode(), "Code");
				c.assertEqual(space.getDescription(), creation.getDescription(), "Description");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("updateSpaces()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var creation = new SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));
			creation.setDescription("test description");

			var update = new SpaceUpdate();
			update.setDescription("test description 2");

			$.when(c.createFacadeAndLogin(), c.createSpaceFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createSpaces([ creation ]).then(function(permIds) {

					update.setSpaceId(permIds[0]);

					return facade.updateSpaces([ update ]).then(function() {
						return facade.mapSpaces(permIds, fetchOptions).done(function() {
							facade.logout()
						});
					});
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 1);

				var space = spaces[creation.getCode()];
				c.assertEqual(space.getCode(), creation.getCode(), "Code");
				c.assertEqual(space.getDescription(), update.getDescription().getValue(), "Description");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createSpacePermId("TEST"), c.createSpaceFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapSpaces([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 1);

				var space = spaces["TEST"];
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
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("searchSpaces()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createSpaceSearchCriterion(), c.createSpaceFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST");

				return facade.searchSpaces(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(spaces) {
				c.assertObjectsCount(spaces, 1);

				var space = spaces[0];
				c.assertEqual(space.getCode(), "TEST", "Code");
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("deleteSpaces()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var creation = new SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));

			var deletion = new SpaceDeletionOptions();
			deletion.setReason("test reason");

			$.when(c.createFacadeAndLogin(), c.createSpaceFetchOptions()).then(function(facade, fetchOptions) {
				return facade.createSpaces([ creation ]).then(function(permIds) {
					return facade.deleteSpaces(permIds, deletion).then(function() {
						return facade.mapSpaces(permIds, fetchOptions).done(function() {
							facade.logout()
						});
					});
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 0);
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});
	}
});
