define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Space tests");

		QUnit.test("createSpaces()", function(assert) {
			var c = new common(assert);
			c.start();

			var creation = new c.SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));
			creation.setDescription("test description");

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createSpaces([ creation ]).then(function(permIds) {
					return facade.mapSpaces(permIds, c.createSpaceFetchOptions()).done(function() {
						facade.logout()
					});
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 1);

				var space = spaces[creation.getCode()];
				c.assertEqual(space.getCode(), creation.getCode(), "Code");
				c.assertEqual(space.getDescription(), creation.getDescription(), "Description");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("updateSpaces()", function(assert) {
			var c = new common(assert);
			c.start();

			var creation = new c.SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));
			creation.setDescription("test description");

			var update = new c.SpaceUpdate();
			update.setDescription("test description 2");

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createSpaces([ creation ]).then(function(permIds) {

					update.setSpaceId(permIds[0]);

					return facade.updateSpaces([ update ]).then(function() {
						return facade.mapSpaces(permIds, c.createSpaceFetchOptions()).done(function() {
							facade.logout()
						});
					});
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 1);

				var space = spaces[creation.getCode()];
				c.assertEqual(space.getCode(), creation.getCode(), "Code");
				c.assertEqual(space.getDescription(), update.getDescription().getValue(), "Description");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("mapSpaces()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.mapSpaces([ new c.SpacePermId("TEST") ], c.createSpaceFetchOptions()).done(function() {
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
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchSpaces()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {

				var criterion = new c.SpaceSearchCriterion();
				criterion.withCode().thatEquals("TEST");

				return facade.searchSpaces(criterion, c.createSpaceFetchOptions()).done(function() {
					facade.logout();
				})
			}).done(function(spaces) {
				c.assertObjectsCount(spaces, 1);

				var space = spaces[0];
				c.assertEqual(space.getCode(), "TEST", "Code");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("deleteSpaces()", function(assert) {
			var c = new common(assert);
			c.start();

			var creation = new c.SpaceCreation();
			creation.setCode("CREATE_JSON_SPACE_" + (new Date().getTime()));

			var deletion = new c.SpaceDeletionOptions();
			deletion.setReason("test reason");

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createSpaces([ creation ]).then(function(permIds) {
					return facade.deleteSpaces(permIds, deletion).then(function() {
						return facade.mapSpaces(permIds, c.createSpaceFetchOptions()).done(function() {
							facade.logout()
						});
					});
				});
			}).done(function(spaces) {
				c.assertObjectsCount(Object.keys(spaces), 0);
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});
	}
});
