define([ 'jquery', 'underscore', 'openbis', 'test/common' ], function($, _, openbis, common) {
	return function() {
		QUnit.module("Project tests");

		QUnit.test("mapProjects()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.mapProjects([ new c.ProjectIdentifier("/PLATONIC/SCREENING-EXAMPLES") ], c.createProjectFetchOptions()).done(function() {
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
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("searchProjects()", function(assert) {
			var c = new common(assert);
			c.start();

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				var criterion = new c.ProjectSearchCriterion();
				criterion.withCode().thatEquals("TEST-PROJECT");

				return facade.searchProjects(criterion, c.createProjectFetchOptions()).done(function() {
					facade.logout();
				})
			}).done(function(projects) {
				c.assertObjectsCount(projects, 1);

				var project = projects[0];
				c.assertEqual(project.getCode(), "TEST-PROJECT", "Project code");
				c.assertObjectsWithCollections(project, function(object) {
					return object.getAttachments()
				});
				c.finish();
				;
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		QUnit.test("createAndDeleteAProject", function(assert) {
			var c = new common(assert);
			c.start();

			var code = "CREATE_JSON_PROJECT_" + (new Date().getTime());
			var projectCreation = new c.ProjectCreation();
			projectCreation.setSpaceId(new c.SpacePermId("TEST"));
			projectCreation.setCode(code);
			projectCreation.setDescription("JS test project");
			attachmentCreation = new c.AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world!"));
			projectCreation.setAttachments([ attachmentCreation ]);

			$.when(c.createFacadeAndLogin()).then(function(facade) {
				return facade.createProjects([ projectCreation ]).then(function(permIds) {
					return facade.mapProjects(permIds, c.createProjectFetchOptions()).done(function() {
						var options = new c.ProjectDeletionOptions();
						options.setReason("test");
						facade.deleteProjects([ new c.ProjectIdentifier("/TEST/" + code) ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							c.fail(error.message);
							c.finish();
						});
					})
				})
			}).done(function(projects) {
				var keys = Object.keys(projects);
				c.assertObjectsCount(keys, 1);
				var project = projects[keys[0]];
				c.assertEqual(project.getCode(), code, "Project code");
				c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
				c.assertEqual(project.getDescription(), "JS test project", "Description");
				var attachments = project.getAttachments();
				c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
				c.assertEqual(attachments[0].title, "test_title", "Attachment title");
				c.assertEqual(attachments[0].description, "test_description", "Attachment description");
				c.assertEqual(atob(attachments[0].content), "hello world!", "Attachment content");
				c.assertEqual(attachments.length, 1, "Number of attachments");
				c.finish();
			}).fail(function(error) {
				c.fail(error.message);
				c.finish();
			});
		});

		var asyncUpdateProjectsTest = function(testNamePostfix, projectUpdateModifier, projectCheckerOrExpectedErrorMessage) {
			QUnit.test("updateProjects" + testNamePostfix + "()", function(assert) {
				var c = new common(assert);
				c.start();

				var expectingFailure = _.isFunction(projectCheckerOrExpectedErrorMessage) === false;
				var code = "UPDATE_JSON_PROJECT_" + (new Date().getTime());
				var projectCreation = new c.ProjectCreation();
				projectCreation.setSpaceId(new c.SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				c.createFacadeAndLogin().then(function(facade) {
					var ids = facade.createProjects([ projectCreation ]).then(function(permIds) {
						var projectUpdate = new c.ProjectUpdate();
						projectUpdate.setProjectId(permIds[0]);
						projectUpdateModifier(c, projectUpdate);
						return facade.updateProjects([ projectUpdate ]).then(function() {
							return permIds;
						});
					});
					$.when(ids, c.createProjectFetchOptions()).then(function(permIds, fetchOptions) {
						return facade.mapProjects(permIds, fetchOptions).done(function() {
							facade.logout();
						});
					}).done(function(projects) {
						if (expectingFailure) {
							c.fail("Project update didn't failed as expected.");
						} else {
							var keys = Object.keys(projects);
							c.assertObjectsCount(keys, 1);
							var project = projects[keys[0]];
							projectCheckerOrExpectedErrorMessage(c, code, project);
						}
						c.finish();
					}).fail(function(error) {
						if (expectingFailure) {
							c.assertEqual(error.message, projectCheckerOrExpectedErrorMessage, "Error message");
						} else {
							c.fail(error.message);
						}
						c.finish();
					});
				});
			});
		}

		asyncUpdateProjectsTest("WithChangedSpaceAndAddedAttachment", function(c, projectUpdate) {
			projectUpdate.setSpaceId(new c.SpacePermId("PLATONIC"));
			attachmentCreation = new c.AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world"));
			projectUpdate.getAttachments().add([ attachmentCreation ]);
		}, function(c, code, project) {
			c.assertEqual(project.getCode(), code, "Project code");
			c.assertEqual(project.getSpace().getCode(), "PLATONIC", "Space code");
			c.assertEqual(project.getDescription(), "JS test project", "Description");
			var attachments = project.getAttachments();
			c.assertEqual(attachments[0].fileName, "test_file", "Attachment file name");
			c.assertEqual(attachments[0].title, "test_title", "Attachment title");
			c.assertEqual(attachments[0].description, "test_description", "Attachment description");
			c.assertEqual(atob(attachments[0].content), "hello world", "Attachment content");
			c.assertEqual(attachments.length, 1, "Number of attachments");
		});

		asyncUpdateProjectsTest("WithUnChangedSpaceButChangedProperties", function(c, projectUpdate) {
			projectUpdate.setDescription("test_description");
		}, function(c, code, project) {
			c.assertEqual(project.getCode(), code, "Project code");
			c.assertEqual(project.getSpace().getCode(), "TEST", "Space code");
			c.assertEqual(project.getDescription(), "test_description", "Description");
		});

		asyncUpdateProjectsTest("WithRemovedSpace", function(c, projectUpdate) {
			projectUpdate.setSpaceId(null);
		}, "Space id cannot be null (Context: [])");
	}
});
