define([ 'jquery', 'openbis' ], function($, openbis) {
	/*
	 * These tests should be run against openBIS instance with screening sprint
	 * server database version
	 */

	var testProtocol = window.location.protocol;
	var testHost = window.location.hostname;
	var testPort = window.location.port;
	var testUrl = testProtocol + "//" + testHost + ":" + testPort;
	var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";

	var testUserId = "openbis_test_js";
	var testUserPassword = "password";

	var Common = function() {
	}

	$.extend(Common.prototype, {

		createObject : function() {
			var dfd = $.Deferred();
			var objectPath = arguments[0];
			var objectParameters = [];

			for (var i = 1; i < arguments.length; i++) {
				objectParameters.push(arguments[i]);
			}

			require([ objectPath ], function(objectClass) {
				objectParameters.unshift(objectClass);
				var object = new (objectClass.bind.apply(objectClass, objectParameters))();
				dfd.resolve(object);
			});

			return dfd.promise();
		},

		createFacade : function(action) {
			stop();

			var facade = new openbis(testApiUrl);

			action(facade);
		},

		createFacadeAndLogin : function() {
			var dfd = $.Deferred();

			this.createFacade(function(facade) {
				facade.login(testUserId, testUserPassword).done(function() {
					dfd.resolve(facade);
					start();
				}).fail(function() {
					dfd.reject(arguments);
					start();
				});
			});

			return dfd.promise();
		},

		createSpacePermId : function(permId) {
			return this.createObject('dto/id/space/SpacePermId', permId);
		},

		createProjectPermId : function(permId) {
			return this.createObject('dto/id/project/ProjectPermId', permId);
		},

		createProjectIdentifier : function(identifier) {
			return this.createObject('dto/id/project/ProjectIdentifier', identifier);
		},

		createExperimentPermId : function(permId) {
			return this.createObject('dto/id/experiment/ExperimentPermId', permId);
		},

		createExperimentIdentifier : function(identifier) {
			return this.createObject('dto/id/experiment/ExperimentIdentifier', identifier);
		},

		createSamplePermId : function(permId) {
			return this.createObject('dto/id/sample/SamplePermId', permId);
		},

		createSpaceSearchCriterion : function() {
			return this.createObject('dto/search/SpaceSearchCriterion');
		},

		createProjectSearchCriterion : function() {
			return this.createObject('dto/search/ProjectSearchCriterion');
		},

		createExperimentSearchCriterion : function() {
			return this.createObject('dto/search/ExperimentSearchCriterion');
		},

		createSampleSearchCriterion : function() {
			return this.createObject('dto/search/SampleSearchCriterion');
		},

		createSpaceFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/space/SpaceFetchOptions');
			promise.done(function(fo) {
				fo.withProjects();
				fo.withSamples();
				fo.withRegistrator();
			});
			return promise;
		},

		createProjectFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/project/ProjectFetchOptions');
			promise.done(function(fo) {
				fo.withSpace();
				fo.withExperiments();
				fo.withRegistrator();
				fo.withModifier();
				fo.withLeader();
				fo.withAttachments();
			});
			return promise;
		},

		createExperimentFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/experiment/ExperimentFetchOptions');
			promise.done(function(fo) {
				fo.withType();
				fo.withProject().withSpace();
				fo.withProperties();
				fo.withTags();
				fo.withRegistrator();
				fo.withModifier();
				fo.withAttachments().withContent();
			})
			return promise;
		},

		createSampleFetchOptions : function() {
			var promise = this.createObject('dto/fetchoptions/sample/SampleFetchOptions');
			promise.done(function(fo) {
				fo.withType();
				fo.withExperiment().withProject().withSpace();
				fo.withSpace();
				fo.withProperties();
				fo.withTags();
				fo.withRegistrator();
				fo.withModifier();
				fo.withAttachments();
				fo.withChildrenUsing(fo);
			});
			return promise;
		}
	});

	return new Common();
})
