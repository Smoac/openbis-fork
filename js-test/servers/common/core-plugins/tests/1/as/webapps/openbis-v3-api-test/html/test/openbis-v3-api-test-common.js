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
			var dfd = $.Deferred();

			require([ 'dto/id/space/SpacePermId' ], function(SpacePermId) {
				var id = new SpacePermId(permId);
				dfd.resolve(id);
			});

			return dfd.promise();
		},

		createExperimentPermId : function(permId) {
			var dfd = $.Deferred();

			require([ 'dto/id/experiment/ExperimentPermId' ], function(ExperimentPermId) {
				var id = new ExperimentPermId(permId);
				dfd.resolve(id);
			});

			return dfd.promise();
		},

		createExperimentIdentifier : function(identifier) {
			var dfd = $.Deferred();

			require([ 'dto/id/experiment/ExperimentIdentifier' ], function(ExperimentIdentifier) {
				var id = new ExperimentIdentifier(identifier);
				dfd.resolve(id);
			});

			return dfd.promise();
		},

		createSamplePermId : function(permId) {
			var dfd = $.Deferred();

			require([ 'dto/id/sample/SamplePermId' ], function(SamplePermId) {
				var id = new SamplePermId(permId);
				dfd.resolve(id);
			});

			return dfd.promise();
		},

		createSpaceSearchCriterion : function() {
			var dfd = $.Deferred();

			require([ 'dto/search/SpaceSearchCriterion' ], function(SpaceSearchCriterion) {
				var criterion = new SpaceSearchCriterion();
				dfd.resolve(criterion);
			});

			return dfd.promise();
		},

		createSpaceFetchOptions : function() {
			var dfd = $.Deferred();

			require([ 'dto/fetchoptions/space/SpaceFetchOptions' ], function(sfo) {
				var fo = new sfo;
				fo.withProjects();
				fo.withSamples();
				fo.withRegistrator();

				dfd.resolve(fo);
			});

			return dfd.promise();
		},

		createExperimentSearchCriterion : function() {
			var dfd = $.Deferred();

			require([ 'dto/search/ExperimentSearchCriterion' ], function(ExperimentSearchCriterion) {
				var criterion = new ExperimentSearchCriterion();
				dfd.resolve(criterion);
			});

			return dfd.promise();
		},

		createExperimentFetchOptions : function() {
			var dfd = $.Deferred();

			require([ 'dto/fetchoptions/experiment/ExperimentFetchOptions' ], function(efo) {
				var fo = new efo;
				fo.withType();
				fo.withProject().withSpace();
				fo.withProperties();
				fo.withTags();
				fo.withRegistrator();
				fo.withModifier();
				fo.withAttachments().withContent();

				dfd.resolve(fo);
			});

			return dfd.promise();
		},

		createSampleSearchCriterion : function() {
			var dfd = $.Deferred();

			require([ 'dto/search/SampleSearchCriterion' ], function(SampleSearchCriterion) {
				var criterion = new SampleSearchCriterion();
				dfd.resolve(criterion);
			});

			return dfd.promise();
		},

		createSampleFetchOptions : function() {
			var dfd = $.Deferred();

			require([ 'dto/fetchoptions/sample/SampleFetchOptions' ], function(sfo) {
				var fo = new sfo;
				fo.withType();
				fo.withExperiment().withProject().withSpace();
				fo.withSpace();
				fo.withProperties();
				fo.withTags();
				fo.withRegistrator();
				fo.withModifier();
				fo.withAttachments();
				fo.withChildrenUsing(fo);
				dfd.resolve(fo);
			});
			return dfd.promise();
		}
	});

	return new Common();
})
