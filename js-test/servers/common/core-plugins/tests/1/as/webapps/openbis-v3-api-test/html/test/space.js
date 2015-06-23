define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Space tests");

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
	}
});
