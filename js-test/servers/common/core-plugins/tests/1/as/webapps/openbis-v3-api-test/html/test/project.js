define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, c) {
	return function() {
		QUnit.module("Project tests");

		asyncTest("mapProjects()", function() {
			$.when(c.createFacadeAndLogin(), c.createProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"), c.createProjectFetchOptions()).then(function(facade, identifier, fetchOptions) {
				return facade.mapProjects([ identifier ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(projects) {
				assertObjectsCount(Object.keys(projects), 1);

				var project = projects["/PLATONIC/SCREENING-EXAMPLES"];
				equal(project.getPermId().getPermId(), "20130412103942912-1", "PermId");
				equal(project.getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES", "Identifier");
				equal(project.getCode(), "SCREENING-EXAMPLES", "Code");
				equal(project.getDescription(), null, "Description");
				assertDate(project.getRegistrationDate(), "Registration date", 2013, 4, 12, 8, 39);
				assertToday(project.getModificationDate(), "Modification date");
				assertObjectsWithCollections(project, function(object) {
					return object.getExperiments()
				});
				equal(project.getSpace().getCode(), "PLATONIC", "Space code");
				equal(project.getRegistrator().getUserId(), "admin", "Registrator userId");
				equal(project.getModifier().getUserId(), "openbis_test_js", "Modifier userId");
				equal(project.getLeader(), null, "Leader");
				assertObjectsWithoutCollections(project, function(object) {
					return object.getAttachments()
				});
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});

		asyncTest("searchProjects()", function() {
			$.when(c.createFacadeAndLogin(), c.createProjectSearchCriterion(), c.createProjectFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST-PROJECT");

				return facade.searchProjects(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(projects) {
				assertObjectsCount(projects, 1);

				var project = projects[0];
				equal(project.getCode(), "TEST-PROJECT", "Project code");
				assertObjectsWithCollections(project, function(object) {
					return object.getAttachments()
				});
				start();
			}).fail(function(error) {
				ok(false, error.message);
				start();
			});
		});

	}
});
