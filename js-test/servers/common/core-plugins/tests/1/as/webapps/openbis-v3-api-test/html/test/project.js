define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Project tests");

		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES"), c.createProjectFetchOptions()).then(function(facade, identifier, fetchOptions) {
				return facade.mapProjects([ identifier ], fetchOptions).done(function() {
					facade.logout()
				});
			}).done(function(projects) {
				c.assertObjectsCount(Object.keys(projects), 1);

				var project = projects["/PLATONIC/SCREENING-EXAMPLES"];
				c.assertEqual(project.getPermId().getPermId(), "20130412103942912-1", "PermId");
				c.assertEqual(project.getIdentifier().getIdentifier(), "/PLATONIC/SCREENING-EXAMPLES", "Identifier");
				c.assertEqual(project.getCode(), "SCREENING-EXAMPLES", "Code");
				c.assertEqual(project.getDescription(), null, "Description");
				c.assertDate(project.getRegistrationDate(), "Registration date", 2013, 4, 12, 8, 39);
				// c.assertToday(project.getModificationDate(), "Modification
				// date");
				c.assertObjectsWithCollections(project, function(object) {
					return object.getExperiments()
				});
				c.assertEqual(project.getSpace().getCode(), "PLATONIC", "Space code");
				c.assertEqual(project.getRegistrator().getUserId(), "admin", "Registrator userId");
				// c.assertEqual(project.getModifier().getUserId(),
				// "openbis_test_js", "Modifier userId");
				c.assertEqual(project.getLeader(), null, "Leader");
				c.assertObjectsWithoutCollections(project, function(object) {
					return object.getAttachments()
				});
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

		QUnit.test("searchProjects()", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			$.when(c.createFacadeAndLogin(), c.createProjectSearchCriterion(), c.createProjectFetchOptions()).then(function(facade, criterion, fetchOptions) {

				criterion.withCode().thatEquals("TEST-PROJECT");

				return facade.searchProjects(criterion, fetchOptions).done(function() {
					facade.logout();
				})
			}).done(function(projects) {
				c.assertObjectsCount(projects, 1);

				var project = projects[0];
				c.assertEqual(project.getCode(), "TEST-PROJECT", "Project code");
				c.assertObjectsWithCollections(project, function(object) {
					return object.getAttachments()
				});
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});

	}
});
