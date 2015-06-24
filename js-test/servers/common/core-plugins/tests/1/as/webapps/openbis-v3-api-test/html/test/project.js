define([ 'jquery', 'underscore', 'openbis', 'test/common', 'dto/entity/project/ProjectCreation', 'dto/deletion/project/ProjectDeletionOptions', 'dto/entity/project/ProjectUpdate', 
         'dto/id/space/SpacePermId', 'dto/entity/attachment/AttachmentCreation', ], function($, _, openbis, common, ProjectCreation, ProjectDeletionOptions, ProjectUpdate, SpacePermId, AttachmentCreation) {
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

		QUnit.test("createAndDeleteAProject", function(assert) {
			var c = new common(assert);
			var done = assert.async();

			var code = "CREATE_JSON_PROJECT_" + (new Date().getTime());
			var projectCreation = new ProjectCreation();
			projectCreation.setSpaceId(new SpacePermId("TEST"));
			projectCreation.setCode(code);
			projectCreation.setDescription("JS test project");
			attachmentCreation = new AttachmentCreation();
			attachmentCreation.setFileName("test_file");
			attachmentCreation.setTitle("test_title");
			attachmentCreation.setDescription("test_description");
			attachmentCreation.setContent(btoa("hello world!"));
			projectCreation.setAttachments([ attachmentCreation ]);

			$.when(c.createFacadeAndLogin(), c.createProjectIdentifier("/TEST/" + code), c.createProjectFetchOptions()).then(function(facade, identifier, fetchOptions) {
				return facade.createProjects([ projectCreation ]).then(function(permIds) {
					return facade.mapProjects(permIds, fetchOptions).done(function() {
						var options = new ProjectDeletionOptions();
						options.setReason("test");
						facade.deleteProjects([ identifier ], options).then(function(deletionId) {
							console.log(deletionId);
							facade.logout();
						}).fail(function(error) {
							c.fail(error.message);
							done();
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
				done();
			}).fail(function(error) {
				c.fail(error.message);
				done();
			});
		});
		
		var asyncUpdateProjectsTest = function(testNamePostfix, projectUpdateModifier, projectCheckerOrExpectedErrorMessage) {
			QUnit.test("updateProjects" + testNamePostfix + "()", function(assert) {
				var c = new common(assert);
				var done = assert.async();
	
				var expectingFailure = _.isFunction(projectCheckerOrExpectedErrorMessage) === false;
				var code = "UPDATE_JSON_PROJECT_" + (new Date().getTime());
				var projectCreation = new ProjectCreation();
				projectCreation.setSpaceId(new SpacePermId("TEST"));
				projectCreation.setCode(code);
				projectCreation.setDescription("JS test project");
				c.createFacadeAndLogin().then(function(facade) {
					var ids = facade.createProjects([ projectCreation ]).then(function(permIds) {
						var projectUpdate = new ProjectUpdate();
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
						done();
					}).fail(function(error) {
						if (expectingFailure) {
							c.assertEqual(error.message, projectCheckerOrExpectedErrorMessage, "Error message");
						} else {
							c.fail(error.message);
						}
						done();
					});
				});
			});	
		}

		asyncUpdateProjectsTest("WithChangedSpaceAndAddedAttachment", function(c, projectUpdate) {
			projectUpdate.setSpaceId(new SpacePermId("PLATONIC"));
			attachmentCreation = new AttachmentCreation();
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
