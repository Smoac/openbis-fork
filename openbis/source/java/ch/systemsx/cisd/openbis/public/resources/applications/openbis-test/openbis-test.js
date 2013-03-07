/*
 * These tests should be run against openBIS instance 
 * with screening sprint server database version
 */

test("logout", function(){
	createFacade(function(facade){
		facade.logout(function(){
			equal(facade.getSession(), null, 'Session is empty after logout');
			
			facade.restoreSession();
			equal(facade.getSession(), null, 'Restored session is empty after logout');
			
			facade.isSessionActive(function(response){
				equal(response.result, false, 'Session is inactive after logout');
				facade.close();
			});
		});
	});
});

test("login", function() {
	createFacade(function(facade){
		facade.login('admin','password', function(response){
			ok(response.result,'Session from server is not empty after login');
			ok(facade.getSession(), 'Session from facade is not empty after login');

			facade.isSessionActive(function(response){
				equal(response.result, true,'Session is active after login');
				facade.close();
			});
		});
	});
});

test("cookies", function() {
	createFacade(function(facade){
		facade.useSession('session-1');
		facade.rememberSession();
		equal(facade.getSession(), 'session-1', 'Session 1 used')
		
		facade.useSession('session-2');
		equal(facade.getSession(), 'session-2', 'Session 2 used')
		
		facade.restoreSession();
		equal(facade.getSession(), 'session-1', 'Session 1 restored')
		facade.close();
	});
});

test("listNamedRoleSets()", function(){
	createFacadeAndLogin(function(facade){
		facade.listNamedRoleSets(function(response){
			ok(response.result, 'Got results');
			facade.close();
		});
	});
});

test("listSpacesWithProjectsAndRoleAssignments()", function(){
	createFacadeAndLogin(function(facade){
		facade.listSpacesWithProjectsAndRoleAssignments(null, function(response){
			assertObjectsCount(response.result, 4);
			facade.close();
		});
	});
});

test("searchForSamples()", function(){
	createFacadeAndLogin(function(facade){
		
		var sampleCodes = ['PLATE-1', 'PLATE-1-A'];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);
	
		facade.searchForSamples(searchCriteria, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, sampleCodes);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("searchForSamplesWithFetchOptions()", function(){
	createFacadeAndLogin(function(facade){

		var sampleCodes = ['PLATE-1', 'PLATE-1-A'];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);
		var fetchOptions = [ 'BASIC' ];

		facade.searchForSamplesWithFetchOptions(searchCriteria, fetchOptions, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, sampleCodes);
			assertObjectsWithoutProperties(response.result);
			facade.close();
		});
	});
});

test("searchForSamplesOnBehalfOfUser()", function(){
	createFacadeAndLogin(function(facade){
		
		var sampleCodes = ['PLATE-1', 'PLATE-1-A']
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes)
		var fetchOptions = [ 'BASIC' ];
		var userId = 'power_user';

		facade.searchForSamplesOnBehalfOfUser(searchCriteria, fetchOptions, userId, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ 'PLATE-1-A' ]);
			assertObjectsWithoutProperties(response.result);
			facade.close();
		});
	});
});

test("filterSamplesVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		
		var sampleCodes = ['PLATE-1', 'PLATE-1-A'];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var userId = 'power_user';
			
			facade.filterSamplesVisibleToUser(samples, userId, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'PLATE-1-A' ]);
				assertObjectsWithProperties(response.result);
				facade.close();
			});
		});
	});
});

test("listSamplesForExperiment()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifier = '/MICROSCOPY/TEST/TEST';
		
		facade.listSamplesForExperiment(experimentIdentifier, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ 'TEST-SAMPLE' ]);
			facade.close();
		});
	});
});

test("listDataSetsForSamples()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['TEST-SAMPLE']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			
			facade.listDataSetsForSamples(samples, function(response){
				assertObjectsCount(response.result, 19);
				facade.close();
			});
		});
	});
});

test("listExperiments()", function(){
	createFacadeAndLogin(function(facade){
		facade.listProjects(function(response){
			var projects = response.result.filter(function(project){
				return project.code == 'TEST';
			});
			var experimentType = 'MICROSCOPY';
			
			facade.listExperiments(projects, experimentType, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'TEST' ]);
				facade.close();
			});
		});
	});
});

test("listExperimentsHavingSamples()", function(){
	createFacadeAndLogin(function(facade){
		facade.listProjects(function(response){
			var projects = response.result.filter(function(project){
				return project.code == 'TEST';
			});
			var experimentType = 'MICROSCOPY';
			
			facade.listExperimentsHavingSamples(projects, experimentType, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'TEST' ]);
				facade.close();
			});
		});
	});
});

test("listExperimentsHavingDataSets()", function(){
	createFacadeAndLogin(function(facade){
		facade.listProjects(function(response){
			var projects = response.result.filter(function(project){
				return project.code == 'TEST';
			});
			var experimentType = 'MICROSCOPY';
			
			facade.listExperimentsHavingDataSets(projects, experimentType, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'TEST' ]);
				facade.close();
			});
		});
	});
});

test("filterExperimentsVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['TEST', 'E1']);
		
		facade.searchForExperiments(searchCriteria, function(response){
			var experiments = response.result;
			var userId = 'power_user';
			
			facade.filterExperimentsVisibleToUser(experiments, userId, function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithCodes(response.result, [ 'E1' ]);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSample()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var sample = response.result[0];
			var restrictToDirectlyConnected = true;
			
			facade.listDataSetsForSample(sample, restrictToDirectlyConnected, function(response){
				assertObjectsCount(response.result, 6);
				facade.close();
			});
		});
	});
});

test("listDataStores()", function(){
	createFacadeAndLogin(function(facade){
		facade.listDataStores(function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ 'DSS-SCREENING' ]);
			facade.close();
		});
	});
});

test("getDefaultPutDataStoreBaseURL()", function(){
	createFacadeAndLogin(function(facade){
		facade.getDefaultPutDataStoreBaseURL(function(response){
			equal(response.result, 'https://sprint-openbis.ethz.ch:8444', 'URL is correct')
			facade.close();
		});
	});
});

test("tryGetDataStoreBaseURL()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = '20110913111517610-82996';
		
		facade.tryGetDataStoreBaseURL(dataSetCode, function(response){
			equal(response.result, 'https://sprint-openbis.ethz.ch:8444', 'URL is correct')
			facade.close();
		});
	});
});

test("getDataStoreBaseURLs()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCodes = [ '20110913111517610-82996', '20110913111925577-82997' ];
		
		facade.getDataStoreBaseURLs(dataSetCodes, function(response){
			assertObjectsCount(response.result, 1);
			
			var urlForDataSets = response.result[0];
			equal(urlForDataSets.dataStoreURL, 'https://sprint-openbis.ethz.ch:8444', 'URL is correct');
			deepEqual(urlForDataSets.dataSetCodes.sort(), dataSetCodes.sort());
			facade.close();
		});
	});
});

test("listDataSetTypes()", function(){
	createFacadeAndLogin(function(facade){
		facade.listDataSetTypes(function(response){
			assertObjectsCount(response.result, 25);
			facade.close();
		});
	});
});

test("listVocabularies()", function(){
	createFacadeAndLogin(function(facade){
		facade.listVocabularies(function(response){
			assertObjectsCount(response.result, 4);
			facade.close();
		});
	});
});

test("listDataSetsForSamplesWithConnections()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			
			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response){
				assertObjectsCount(response.result, 6);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesOnBehalfOfUser()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1, PLATE-1-A']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			var userId = 'power_user';
			
			facade.listDataSetsForSamplesOnBehalfOfUser(samples, connectionsToGet, userId, function(response){
				assertObjectsCount(response.result, 5);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForExperiments()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['TEST']);
		
		facade.searchForExperiments(searchCriteria, function(response){
			var experiments = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			
			facade.listDataSetsForExperiments(experiments, connectionsToGet, function(response){
				assertObjectsCount(response.result, 19);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForExperimentsOnBehalfOfUser()", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['TEST','E1']);
		
		facade.searchForExperiments(searchCriteria, function(response){
			var experiments = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			var userId = 'power_user';
			
			facade.listDataSetsForExperimentsOnBehalfOfUser(experiments, connectionsToGet, userId, function(response){
				assertObjectsCount(response.result, 428);
				assertObjectsWithParentCodes(response.result);
				facade.close();
			});
		});
	});
});

test("getDataSetMetaData()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCodes = [ '20110817134524954-81697', '20110817134715385-81703' ];
		
		facade.getDataSetMetaData(dataSetCodes, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, dataSetCodes);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("getDataSetMetaDataWithFetchOptions()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCodes = [ '20110817134524954-81697', '20110817134715385-81703' ];
		var fetchOptions = [ 'BASIC' ];
		
		facade.getDataSetMetaDataWithFetchOptions(dataSetCodes, fetchOptions, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, dataSetCodes);
			assertObjectsWithoutProperties(response.result);
			facade.close();
		});
	});
});

test("searchForDataSets()", function(){
	createFacadeAndLogin(function(facade){
		
		var dataSetCodes = [ '20110817134524954-81697', '20110817134715385-81703' ];
		var searchCriteria = createSearchCriteriaForCodes(dataSetCodes);
	
		facade.searchForDataSets(searchCriteria, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, dataSetCodes);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});

test("searchForDataSetsOnBehalfOfUser()", function(){
	createFacadeAndLogin(function(facade){
		
		var dataSetCodes = [ '20110817134524954-81697', '20110817134715385-81703', '20110608134033662-81622' ];
		var searchCriteria = createSearchCriteriaForCodes(dataSetCodes);
		var userId = 'power_user';
	
		facade.searchForDataSetsOnBehalfOfUser(searchCriteria, userId, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithCodes(response.result, [ '20110817134524954-81697', '20110817134715385-81703' ]);
			assertObjectsWithProperties(response.result);
			facade.close();
		});
	});
});
