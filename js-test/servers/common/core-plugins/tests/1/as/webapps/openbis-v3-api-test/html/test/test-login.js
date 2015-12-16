define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Login tests");

		QUnit.test("loginAs()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacade()).then(function(facade) {
				var criteria = new c.SpaceSearchCriteria();
				var fetchOptions = new c.SpaceFetchOptions();
				return facade.login("openbis_test_js", "password").then(function() {
					return facade.searchSpaces(criteria, fetchOptions).then(function(spacesForInstanceAdmin) {
						return facade.loginAs("openbis_test_js", "password", "test_space_admin").then(function() {
							return facade.searchSpaces(criteria, fetchOptions).then(function(spacesForSpaceAdmin) {
								c.assertTrue(spacesForInstanceAdmin.getTotalCount() > spacesForSpaceAdmin.getTotalCount());
								c.assertObjectsWithValues(spacesForSpaceAdmin.getObjects(), "code", [ "TEST" ]);
								c.finish();
							});
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("loginAnonymously()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacade()).then(function(facade) {
				var criteria = new c.SpaceSearchCriteria();
				var fetchOptions = new c.SpaceFetchOptions();
				return facade.loginAnonymously().then(function() {
					return facade.searchSpaces(criteria, fetchOptions).then(function(spaces) {
						c.assertTrue(spaces.getTotalCount() == 1)
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

	}
});
