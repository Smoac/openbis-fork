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
		

		QUnit.test("getSessionInformation()", function(assert) {
			var c = new common(assert);
			c.start();
			
			$.when(c.createFacade()).then(function(facade) {
				return facade.login("openbis_test_js", "password").then(function() {
					
					return facade.getSessionInformation().then(function(sessionInformation) {
						c.assertTrue(sessionInformation != null);
						c.assertTrue(sessionInformation.getPerson() != null);
						c.finish();
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});
		
		QUnit.test("setWebAppSettings() getWebAppSettings", function(assert) {
			var c = new common(assert);
			c.start();
			var webAppId = c.generateId("WEBAPP");

			$.when(c.createFacade()).then(function(facade) {
				return facade.login("openbis_test_js", "password").then(function() {
					var settings = new c.WebAppSettings();
					settings.setWebAppId(webAppId);
					settings.setSettings({"param1" : "value1", "param2" : "value2"});
					return facade.setWebAppSettings(settings).then(function() {
						facade.getWebAppSettings(webAppId).then(function(settings2) {
							c.assertEqual(settings2.getWebAppId(), webAppId);
							c.assertEqual(settings2.getSettings()["param1"], "value1");
							c.assertEqual(settings2.getSettings()["param2"], "value2");
							c.finish();
						});
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});
		

		QUnit.test("loginAsAnonymousUser()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacade()).then(function(facade) {
				var criteria = new c.SpaceSearchCriteria();
				var fetchOptions = new c.SpaceFetchOptions();
				return facade.loginAsAnonymousUser().then(function() {
					return facade.searchSpaces(criteria, fetchOptions).then(function(spaces) {
						c.assertTrue(spaces.getTotalCount() == 1)
						c.finish();						
					});
				});
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

	}
});
