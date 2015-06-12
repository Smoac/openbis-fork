define([ 'jquery', 'underscore', 'openbis', 'test/openbis-v3-api-test-common' ], function($, _, openbis, c) {
	return function() {
		QUnit.module("Space tests");

		asyncTest("mapSpaces()", function() {
			$.when(c.createFacadeAndLogin(), c.createSpacePermId("TEST"), c.createSpaceFetchOptions()).then(function(facade, permId, fetchOptions) {
				return facade.mapSpaces([ permId ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(spaces) {
				assertObjectsCount(Object.keys(spaces), 1);

				var space = spaces["TEST"];
				equal(space.getCode(), "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});

		asyncTest("searchSpaces()", function() {
			$.when(c.createFacadeAndLogin(), c.createSpaceSearchCriterion(), c.createSpaceFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST");

				return facade.searchSpaces(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(spaces) {
				assertObjectsCount(spaces, 1);

				var space = spaces[0];
				equal(space.getCode(), "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});
	}
});
