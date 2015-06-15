define([ 'jquery', 'underscore', 'openbis', 'test/openbis-v3-api-test-common' ], function($, _, openbis, c) {
	return function() {
		QUnit.module("Project tests");

		asyncTest("mapProjects()", function() {
			$.when(c.createFacadeAndLogin(), c.createProjectIdentifier("/TEST/TEST-PROJECT"), c.createProjectFetchOptions()).then(function(facade, identifier, fetchOptions) {
				return facade.mapProjects([ identifier ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(projects) {
				assertObjectsCount(Object.keys(projects), 1);

				var project = projects["/TEST/TEST-PROJECT"];
				equal(project.getCode(), "TEST-PROJECT", "Project code");
				equal(project.getSpace().getCode(), "TEST", "Space code");
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});
	}
});
