define([ 'jquery', 'openbis-v3-api' ], function($, openbis) {
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

	var createFacade = function(action) {
		stop();

		var facade = new openbis(testApiUrl);

		action(facade);
	}

	var createFacadeAndLogin = function() {
		var dfd = $.Deferred();

		createFacade(function(facade) {
			facade.login(testUserId, testUserPassword).done(function() {
				dfd.resolve(facade);
				start();
			}).fail(function() {
				dfd.reject(arguments);
				start();
			});
		});

		return dfd.promise();
	}

	var createExperimentPermId = function(permId) {
		var dfd = $.Deferred();

		require([ 'dto/id/experiment/ExperimentPermId' ], function(ExperimentPermId) {
			var id = new ExperimentPermId(permId);
			dfd.resolve(id);
		});

		return dfd.promise();
	}

	var createSamplePermId = function(permId) {
		var dfd = $.Deferred();

		require([ 'dto/id/sample/SamplePermId' ], function(SamplePermId) {
			var id = new SamplePermId(permId);
			dfd.resolve(id);
		});

		return dfd.promise();
	}

	var createExperimentSearchCriterion = function() {
		var dfd = $.Deferred();

		require([ 'dto/search/ExperimentSearchCriterion' ], function(ExperimentSearchCriterion) {
			var criterion = new ExperimentSearchCriterion();
			dfd.resolve(criterion);
		});

		return dfd.promise();
	}

	var createExperimentFetchOptions = function() {
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
	}
	
	var createSampleSearchCriterion = function() {
		var dfd = $.Deferred();

		require([ 'dto/search/SampleSearchCriterion' ], function(SampleSearchCriterion) {
			var criterion = new SampleSearchCriterion();
			dfd.resolve(criterion);
		});

		return dfd.promise();
	}	

	var createSampleFetchOptions = function() {
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

	var Common = function() {}

	Common.prototype.createFacade = createFacade;
	Common.prototype.createFacadeAndLogin = createFacadeAndLogin;
	Common.prototype.createExperimentPermId = createExperimentPermId;
	Common.prototype.createSamplePermId = createSamplePermId;
	Common.prototype.createExperimentSearchCriterion = createExperimentSearchCriterion;
	Common.prototype.createExperimentFetchOptions = createExperimentFetchOptions;
	Common.prototype.createSampleSearchCriterion = createSampleSearchCriterion;
	Common.prototype.createSampleFetchOptions = createSampleFetchOptions;

	return new Common();
})
