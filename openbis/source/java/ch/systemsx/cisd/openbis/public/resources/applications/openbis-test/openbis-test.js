/*
 * These tests should be run against openBIS instance 
 * with screening sprint server database version
 */

var createMaterialIdentifier = function(identifierString){
	var parts = identifierString.split("/");
	
	return {
		"@type" : "MaterialIdentifierGeneric",
		"materialTypeIdentifier" : {
			"@type" : "MaterialTypeIdentifierGeneric",
			"materialTypeCode" : parts[1]
		},
		"materialCode" : parts[2]
	};
}

var createMetaprojectIdentifierId = function(identifierString){
	return {
		"@type" : "MetaprojectIdentifierId",
		"identifier" : identifierString
	};
}

var createProjectIdentifierId = function(identifierString){
	return {
		"@type" : "ProjectIdentifierId",
		"identifier" : identifierString
	};
}

var createExperimentIdentifierId = function(identifierString){
	return {
		"@type" : "ExperimentIdentifierId",
		"identifier" : identifierString
	};
}

var createSampleIdentifierId = function(identifierString){
	return {
		"@type" : "SampleIdentifierId",
		"identifier" : identifierString
	};
}

var createNewVocabularyTerm = function(code, previousTermOrdinal){
	return {
		"@type" : "NewVocabularyTerm",
		"code" : code,
		"previousTermOrdinal" : previousTermOrdinal
	};
}

var createNewMetaproject = function(facade, identifierString, action){
	var parts = identifierString.split("/");
	var ownerId = parts[1];
	var name = parts[2];

	facade.listMetaprojects(function(response){
		var exists = response.result.some(function(metaproject){
			return metaproject.ownerId == ownerId && metaproject.name == name;
		});
		
		if(exists){
			var id = createMetaprojectIdentifierId(identifierString);
			
			facade.deleteMetaproject(id, function(response){
				facade.createMetaproject(name, null, function(response){
					action();
				});
			});
		}else{
			facade.createMetaproject(name, null, function(response){
				action();
			});
		}
	});
}

var findVocabulary = function(vocabularies, code){
	return vocabularies.filter(function(vocabulary){
		return vocabulary.code == code;
	})[0];
};

var findVocabularyTerm = function(vocabulary, code){
	return vocabulary.terms.filter(function(term){
		return term.code == code;
	})[0];
};

var findVocabularyMaxOrdinal = function(vocabulary){
	var max = 0;
	vocabulary.terms.forEach(function(term){
		max = Math.max(max, term.ordinal);
	});
	return max;
};

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

test("filterDataSetsVisibleToUser()", function(){
	createFacadeAndLogin(function(facade){
		
		var dataSetCodes = [ '20110817134524954-81697', '20110817134715385-81703', '20110608134033662-81622' ];
		var searchCriteria = createSearchCriteriaForCodes(dataSetCodes);
	
		facade.searchForDataSets(searchCriteria, function(response){
			var dataSets = response.result;
			var userId = "power_user";
			
			facade.filterDataSetsVisibleToUser(dataSets, userId, function(response){
				assertObjectsCount(response.result, 2);
				assertObjectsWithCodes(response.result, [ '20110817134524954-81697', '20110817134715385-81703' ]);
				assertObjectsWithProperties(response.result);
				facade.close();
			});
		});
	});
});

test("listExperimentsForIdentifiers()", function(){
	createFacadeAndLogin(function(facade){
		var experimentIdentifiers = [ "/TEST/TEST-PROJECT/E1", "/PLATONIC/MICROSCOPY-EXAMPLES/EXP-1" ];
	
		facade.listExperimentsForIdentifiers(experimentIdentifiers, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, 'identifier', experimentIdentifiers);
			facade.close();
		});
	});
});

test("searchForExperiments()", function(){
	createFacadeAndLogin(function(facade){
		var experimentCodes = [ 'EXP-1' ];
		var searchCriteria = createSearchCriteriaForCodes(experimentCodes);
	
		facade.searchForExperiments(searchCriteria, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, 'identifier', [ "/PLATONIC/SCREENING-EXAMPLES/EXP-1", "/PLATONIC/MICROSCOPY-EXAMPLES/EXP-1" ]);
			facade.close();
		});
	});
});

test("listProjects()", function(){
	createFacadeAndLogin(function(facade){
		facade.listProjects(function(response){
			assertObjectsCount(response.result, 4);
			assertObjectsWithCodes(response.result, [ "MICROSCOPY-EXAMPLES", "SCREENING-EXAMPLES", "TEST", "TEST-PROJECT" ]);
			facade.close();
		});
	});
});

test("listProjectsOnBehalfOfUser()", function(){
	createFacadeAndLogin(function(facade){
		var userId = "power_user";
		
		facade.listProjectsOnBehalfOfUser(userId, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithCodes(response.result, [ "TEST-PROJECT" ]);
			facade.close();
		});
	});
});

test("getMaterialByCodes()", function(){
	createFacadeAndLogin(function(facade){
		var materialIdentifiers = [ createMaterialIdentifier("/GENE/1") ];
		
		facade.getMaterialByCodes(materialIdentifiers, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'materialCode', [ "1" ]);
			facade.close();
		});
	});
});

test("searchForMaterials()", function(){
	createFacadeAndLogin(function(facade){
		var materialCodes = [ "1" ];
		var searchCriteria = createSearchCriteriaForCodes(materialCodes);
		
		facade.searchForMaterials(searchCriteria, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'materialCode', [ "1" ]);
			facade.close();
		});
	});
});

test("listMetaprojects()", function(){
	createFacadeAndLogin(function(facade){
		createNewMetaproject(facade, "/admin/JS_TEST_METAPROJECT", function(response){
			facade.listMetaprojects(function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithValues(response.result, 'name', ['JS_TEST_METAPROJECT']);
				facade.close();
			});
		});
	});
});

test("getMetaproject()", function(){
	createFacadeAndLogin(function(facade){
		var metaprojectIdentifier = "/admin/JS_TEST_METAPROJECT";
		
		createNewMetaproject(facade, metaprojectIdentifier, function(response){
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);
			
			facade.getMetaproject(metaprojectId, function(response){
				equal(response.result.metaproject.identifier, metaprojectIdentifier, 'Metaproject identifier is correct');
				facade.close();
			});
		});
	});
});

test("listAttachmentsForProject()", function(){
	createFacadeAndLogin(function(facade){
		var projectId = createProjectIdentifierId("/TEST/TEST-PROJECT");
		
		facade.listAttachmentsForProject(projectId, false, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, 'fileName', ['dogs.txt']);
			facade.close();
		});
	});
});

/*

TODO add attachments

test("listAttachmentsForExperiment()", function(){
	createFacadeAndLogin(function(facade){
		var experimentId = createExperimentIdentifierId("/TEST/TEST-PROJECT/E1");
		
		facade.listAttachmentsForExperiment(experimentId, false, function(response){
			assertObjectsCount(response.result, 1);
			facade.close();
		});
	});
});

*/

/*

TODO add attachments

test("listAttachmentsForSample()", function(){
	createFacadeAndLogin(function(facade){
		var sampleId = createSampleIdentifierId("/PLATONIC/PLATE-1");
		
		facade.listAttachmentsForSample(sampleId, false, function(response){
			assertObjectsCount(response.result, 1);
			facade.close();
		});
	});
});

*/

test("updateSampleProperties()", function(){
	createFacadeAndLogin(function(facade){
		var sampleCodes = ['PLATE-1'];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);

		facade.searchForSamples(searchCriteria, function(response){
			var sample = response.result[0];
			var description = new Date().getTime().toString();
			var properties = {
				"DESCRIPTION" : description
			};
			
			facade.updateSampleProperties(sample.id, properties, function(response){
				facade.searchForSamples(searchCriteria, function(response){
					var sample = response.result[0];
					equal(sample.properties["DESCRIPTION"], description, "Property value has been changed")
					facade.close();
				});
			});
		});
	});
});

test("addUnofficialVocabularyTerm()", function(){
	createFacadeAndLogin(function(facade){
		var vocabularyCode = "MICROSCOPE";
		var termCode = new Date().getTime().toString();

		facade.listVocabularies(function(response){
			var originalVocabulary = findVocabulary(response.result, vocabularyCode);
			var originalTerm = findVocabularyTerm(originalVocabulary, termCode);
			
			ok(!originalTerm, 'Term did not exist');
			
			var term = createNewVocabularyTerm(termCode, findVocabularyMaxOrdinal(originalVocabulary));
			
			facade.addUnofficialVocabularyTerm(originalVocabulary.id, term, function(response){
				facade.listVocabularies(function(response){
					var updatedVocabulary = findVocabulary(response.result, vocabularyCode);
					var updatedTerm = findVocabularyTerm(updatedVocabulary, termCode);
					
					ok(updatedTerm, 'Term has been added');
					equal(updatedTerm.code, termCode, 'Term has correct code');
					
					facade.close();
				});
			});
		});
	});
});
