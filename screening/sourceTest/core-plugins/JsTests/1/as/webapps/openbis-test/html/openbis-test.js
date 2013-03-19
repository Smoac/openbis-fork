/*
 * These tests should be run against openBIS instance 
 * with screening sprint server database version
 */

var testProtocol = "http";
var testHost = "127.0.0.1";
var testPort = "8888";
var testUrl = testProtocol + "://" + testHost + ":" + testPort;

var testUserId = "openbis_test_js";
var testUserPassword = "password";

var createFacadeAndLogin = function(action, urlOrNull, timeoutOrNull){
	createFacadeAndLoginForUserAndPassword(testUserId, testUserPassword, action, urlOrNull ? urlOrNull : testUrl, timeoutOrNull);
}

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

var createWebAppSettings = function(webAppId, settings){
	return {
		"@type" : "WebAppSettings",
		"webAppId" : webAppId,
		"settings" : settings
	};
}

var createDataSetFileDTO = function(dataSetCode, path, isRecursive){
	return {
		"@type" : "DataSetFileDTO",
		"dataSetCode" : dataSetCode,
		"path" : path,
		"isRecursive" : isRecursive 
	};
}

var createNewMetaproject = function(facade, identifierString, action){
	var parts = identifierString.split("/");
	var ownerId = parts[1];
	var name = parts[2];

	facade.listMetaprojects(function(response){
		var metaproject = findMetaproject(response.result, identifierString);
		
		if(metaproject){
			var id = createMetaprojectIdentifierId(identifierString);
			
			facade.deleteMetaproject(id, function(response){
				facade.createMetaproject(name, null, function(response){
					action(response);
				});
			});
		}else{
			facade.createMetaproject(name, null, function(response){
				action(response);
			});
		}
	});
}

var findMetaproject = function(metaprojects, identifierString){
	var parts = identifierString.split("/");
	var ownerId = parts[1];
	var name = parts[2];
	
	return metaprojects.filter(function(metaproject){
		return metaproject.ownerId == ownerId && metaproject.name == name;
	})[0];
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

var findQuery = function(queries, name){
	return queries.filter(function(query){
		return query.name == name;
	})[0];
}

var findVocabularyMaxOrdinal = function(vocabulary){
	var max = 0;
	vocabulary.terms.forEach(function(term){
		max = Math.max(max, term.ordinal);
	});
	return max;
};

var downloadFile = function(url, action){
	$.ajax({
		url: url,
		cache: false,
		dataType: "text",
		success: function(data) {
			action(data);
		},
		error: function(){
			action(null);
		}
	});
}

var uploadFileToSessionWorkspace = function(facade, fileName, fileContent, dataStoreCode, action){
	facade._internal.getDataStoreUrlForDataStoreCode(dataStoreCode, function(dataStoreUrl){
		var uploadUrl = dataStoreUrl + "/session_workspace_file_upload" +
		"?filename=" + fileName +
		"&id=0" +
		"&startByte=0" +
		"&endByte=0" + 
		"&sessionID=" + facade.getSession();
	
		$.ajax({
			url : uploadUrl,
			type : "POST",
			data : fileContent,
			contentType : "multipart/form-data"
		}).done(function(response){
			action(response);
		});
	}); 
}

var generateRandomString = function(){
	return Math.random().toString();
}

test("new openbis()", function(){
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server without url");
		facade.close();
	});
});

test("new openbis(protocol, host, port)", function(){
	var url = testProtocol + "://" + testHost + ":" + testPort;
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis)", function(){
	var url = testProtocol + "://" + testHost + ":" + testPort + "/openbis";
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis/)", function(){
	var url = testProtocol + "://" + testHost + ":" + testPort + "/openbis/";
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis/openbis)", function(){
	var url = testProtocol + "://" + testHost + ":" + testPort + "/openbis/openbis";
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /openbis/openbis/)", function(){
	var url = testProtocol + "://" + testHost + ":" + testPort + "/openbis/openbis/";
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

test("new openbis(protocol, host, port, /someRandomPath/)", function(){
	var url = testProtocol + "://" + testHost + ":" + testPort + "/someRandomPath/";
	createFacadeAndLogin(function(facade){
		ok(true, "Successfully connected to server with url: " + url);
		facade.close();
	}, url);
});

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
	}, testUrl);
});

test("login", function() {
	createFacade(function(facade){
		facade.login(testUserId, testUserPassword, function(response){
			ok(response.result,'Session from server is not empty after login');
			ok(facade.getSession(), 'Session from facade is not empty after login');

			facade.isSessionActive(function(response){
				equal(response.result, true,'Session is active after login');
				facade.close();
			});
		});
	}, testUrl);
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
	}, testUrl);
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
			equal(response.result, "http://localhost:8889", 'URL is correct')
			facade.close();
		});
	});
});

test("tryGetDataStoreBaseURL()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = '20110913111517610-82996';
		
		facade.tryGetDataStoreBaseURL(dataSetCode, function(response){
			equal(response.result, "http://localhost:8889", 'URL is correct')
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
			equal(urlForDataSets.dataStoreURL, "http://localhost:8889", 'URL is correct');
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

test("listDataSetsForSamplesWithConnections() with parents", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS' ];
			
			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response){
				assertObjectsCount(response.result, 6);
				assertObjectsWithParentCodes(response.result);
				assertObjectsWithoutChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesWithConnections() with children", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var connectionsToGet = [ 'CHILDREN' ];
			
			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response){
				assertObjectsCount(response.result, 6);
				assertObjectsWithoutParentCodes(response.result);
				assertObjectsWithChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesWithConnections() with parents and children", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var connectionsToGet = [ 'PARENTS', 'CHILDREN' ];
			
			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response){
				assertObjectsCount(response.result, 6);
				assertObjectsWithParentCodes(response.result);
				assertObjectsWithChildrenCodes(response.result);
				facade.close();
			});
		});
	});
});

test("listDataSetsForSamplesWithConnections() without parents and children", function(){
	createFacadeAndLogin(function(facade){
		var searchCriteria = createSearchCriteriaForCodes(['PLATE-1']);
		
		facade.searchForSamples(searchCriteria, function(response){
			var samples = response.result;
			var connectionsToGet = [ ];
			
			facade.listDataSetsForSamplesWithConnections(samples, connectionsToGet, function(response){
				assertObjectsCount(response.result, 6);
				assertObjectsWithoutParentCodes(response.result);
				assertObjectsWithoutChildrenCodes(response.result);
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

test("createMetaproject(), listMetaprojects()", function(){
	createFacadeAndLogin(function(facade){
		createNewMetaproject(facade, "/" + testUserId + "/JS_TEST_METAPROJECT", function(response){
			facade.listMetaprojects(function(response){
				assertObjectsCount(response.result, 1);
				assertObjectsWithValues(response.result, 'name', ['JS_TEST_METAPROJECT']);
				facade.close();
			});
		});
	});
});

test("createMetaproject(), getMetaproject()", function(){
	createFacadeAndLogin(function(facade){
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";
		
		createNewMetaproject(facade, metaprojectIdentifier, function(response){
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);
			
			facade.getMetaproject(metaprojectId, function(response){
				equal(response.result.metaproject.identifier, metaprojectIdentifier, 'Metaproject identifier is correct');
				facade.close();
			});
		});
	});
});

test("createMetaproject(), updateMetaproject()", function(){
	createFacadeAndLogin(function(facade){
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";
		
		createNewMetaproject(facade, metaprojectIdentifier, function(response){
			var metaproject = response.result;
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);
			var description = generateRandomString();
			
			ok(!metaproject.description, "Metaproject description was empty");
			
			facade.updateMetaproject(metaprojectId, metaproject.name, description, function(response){
				facade.getMetaproject(metaprojectId, function(response){
					equal(response.result.metaproject.description, description, "Metaproject description properly updated");
					facade.close();
				});
			})
		});
	});
});

test("createMetaproject(), deleteMetaproject()", function(){
	createFacadeAndLogin(function(facade){
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";
		
		createNewMetaproject(facade, metaprojectIdentifier, function(response){
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);
			
			facade.deleteMetaproject(metaprojectId, function(response){
				facade.listMetaprojects(function(response){
					ok(!findMetaproject(response.result, metaprojectIdentifier), "Metaproject has been deleted");
					facade.close();
				});
			})
		});
	});
});

test("createMetaproject(), addToMetaproject(), removeFromMetaproject()", function(){
	createFacadeAndLogin(function(facade){
		var metaprojectIdentifier = "/" + testUserId + "/JS_TEST_METAPROJECT";
		
		createNewMetaproject(facade, metaprojectIdentifier, function(response){
			var metaprojectId = createMetaprojectIdentifierId(metaprojectIdentifier);
			
			facade.getMetaproject(metaprojectId, function(response){
				var assignments = response.result;
				assertObjectsCount(response.result.samples, 0);
				
				var assignmentsIds = {
					"@type" : "MetaprojectAssignmentsIds",
					"samples" : [ createSampleIdentifierId("/PLATONIC/PLATE-1") ]
				};
				
				facade.addToMetaproject(metaprojectId, assignmentsIds, function(response){
					facade.getMetaproject(metaprojectId, function(response){
						var assignments = response.result;
						assertObjectsCount(assignments.samples, 1);
						assertObjectsWithCodes(assignments.samples, ['PLATE-1']);
						
						facade.removeFromMetaproject(metaprojectId, assignmentsIds, function(response){
							facade.getMetaproject(metaprojectId, function(response){
								var assignments = response.result;
								assertObjectsCount(assignments.samples, 0);
								facade.close();
							});
						});
					});
				});
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

test("listAttachmentsForExperiment()", function(){
	createFacadeAndLogin(function(facade){
		var experimentId = createExperimentIdentifierId("/TEST/TEST-PROJECT/E1");
		
		facade.listAttachmentsForExperiment(experimentId, false, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "fileName", ["experiment_attachment.txt"]);
			facade.close();
		});
	});
});

test("listAttachmentsForSample()", function(){
	createFacadeAndLogin(function(facade){
		var sampleId = createSampleIdentifierId("/PLATONIC/PLATE-1");
		
		facade.listAttachmentsForSample(sampleId, false, function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "fileName", ["sample_attachment.txt"]);
			facade.close();
		});
	});
});

test("updateSampleProperties(), searchForSamples()", function(){
	createFacadeAndLogin(function(facade){
		var sampleCodes = ['PLATE-1'];
		var searchCriteria = createSearchCriteriaForCodes(sampleCodes);

		facade.searchForSamples(searchCriteria, function(response){
			var sample = response.result[0];
			var description = generateRandomString();
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

test("addUnofficialVocabularyTerm(), listVocabularies()", function(){
	createFacadeAndLogin(function(facade){
		var vocabularyCode = "MICROSCOPE";
		var termCode = generateRandomString();

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

test("setWebAppSettings(), getWebAppSettings()", function(){
	createFacadeAndLogin(function(facade){
		var webAppId = generateRandomString();
		
		facade.getWebAppSettings(webAppId, function(response){
			deepEqual(response.result.settings, {}, 'Web app settings are empty');
			
			var settings = {
				"param1" : "value1",
				"param2" : "value2"
			};
			var webAppSettings = createWebAppSettings(webAppId, settings);
			
			facade.setWebAppSettings(webAppSettings, function(response){
				facade.getWebAppSettings(webAppId, function(response){
					deepEqual(response.result.settings, settings, "Web app settings properly updated");
					facade.close();
				});
			});
		});
	});
});

test("listQueries()", function(){
	createFacadeAndLogin(function(facade){
		facade.listQueries(function(response){
			assertObjectsCount(response.result, 1);
			assertObjectsWithValues(response.result, "name", ["Test Query"]);
			facade.close();
		});
	});
});

test("executeQuery()", function(){
	createFacadeAndLogin(function(facade){
		facade.listQueries(function(response){
			var queryId = findQuery(response.result, "Test Query").id;
			var parameterBindings = {};
			
			facade.executeQuery(queryId, parameterBindings, function(response){
				ok(response.result, "Query has been executed");
				equal(response.result.columns[0].title , "test_column", "Returned column has correct title");
				equal(response.result.rows[0][0].value , "test_value", "Returned row has correct value");
				facade.close();
			});
		});
	});
});

test("listTableReportDescriptions()", function(){
	createFacadeAndLogin(function(facade){
		facade.listTableReportDescriptions(function(response){
			assertObjectsCount(response.result, 3);
			facade.close();
		});
	});
});

test("createReportFromDataSets()", function(){
	createFacadeAndLogin(function(facade){
		var dataStoreCode = "DSS-SCREENING";
		var serviceKey = "default-plate-image-analysis";
		var dataSetCodes = [ "20110913112215416-82999" ];
		
		facade.createReportFromDataSets(dataStoreCode, serviceKey, dataSetCodes, function(response){
			ok(response.result, "Report has been created");
			facade.close();
		});
	});
});

test("listAggregationServices()", function(){
	createFacadeAndLogin(function(facade){
		facade.listAggregationServices(function(response){
			assertObjectsCount(response.result, 4);
			facade.close();
		});
	});
});

test("createReportFromAggregationService()", function(){
	createFacadeAndLogin(function(facade){
		var dataStoreCode = "DSS-SCREENING";
		var serviceKey = "test-aggregation-service";
		var parameters = {};
		
		facade.createReportFromAggregationService(dataStoreCode, serviceKey, parameters, function(response){
			ok(response.result, "Report has been created");
			facade.close();
		});
	});
});

test("getSessionTokenFromServer()", function(){
	createFacadeAndLogin(function(facade){
		facade.getSessionTokenFromServer(function(response){
			ok(response.result, "Got session token");
			facade.close();
		});
	});
});

test("listFilesForDataSetFile()", function(){
	createFacadeAndLogin(function(facade){
		var fileOrFolder = createDataSetFileDTO("20110913114645299-83009", "/original", true);
		
		facade.listFilesForDataSetFile(fileOrFolder, function(response){
			assertObjectsCount(response.result, 151);
			facade.close();
		});
	});
});

test("getDownloadUrlForFileForDataSetFile()", function(){
	createFacadeAndLogin(function(facade){
		var fileOrFolder = createDataSetFileDTO("20110913114645299-83009", "/original/SERIES-1/bPLATE_w_s1_z0_t0_cRGB.png", false);
		
		facade.getDownloadUrlForFileForDataSetFile(fileOrFolder, function(response){
			ok(response.result, "Got download url");
			
			downloadFile(response.result, function(data){
				ok(data, "Download url works");
				facade.close();
			});
		});
	});
});

test("getDownloadUrlForFileForDataSetFileWithTimeout()", function(){
	createFacadeAndLogin(function(facade){
		var fileOrFolder = createDataSetFileDTO("20110913114645299-83009", "/original/SERIES-1/bPLATE_w_s1_z0_t0_cRGB.png", false);
		var validityDurationInSeconds = 10;
		
		facade.getDownloadUrlForFileForDataSetFileWithTimeout(fileOrFolder, validityDurationInSeconds, function(response){
			ok(response.result, "Got download url");

			downloadFile(response.result, function(data){
				ok(data, "Download url works once");
				
				downloadFile(response.result, function(data){
					ok(!data, "Download url does not work anymore");
					facade.close();	
				});
			});
		});
	});
});

test("listFilesForDataSet()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = "20110913114645299-83009";
		var path = "/original";
		var recursive = true;
		
		facade.listFilesForDataSet(dataSetCode, path, recursive, function(response){
			assertObjectsCount(response.result, 151);
			facade.close();
		});
	});
});

test("getDownloadUrlForFileForDataSet()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = "20110913114645299-83009";
		var path = "/original/SERIES-1/bPLATE_w_s1_z0_t0_cRGB.png";
		
		facade.getDownloadUrlForFileForDataSet(dataSetCode, path, function(response){
			ok(response.result, "Got download url");
			
			downloadFile(response.result, function(data){
				ok(data, "Download url works");
				facade.close();
			});
		});
	});
});

test("getDownloadUrlForFileForDataSetWithTimeout()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = "20110913114645299-83009";
		var path = "/original/SERIES-1/bPLATE_w_s1_z0_t0_cRGB.png";
		var validityDurationInSeconds = 10;
		
		facade.getDownloadUrlForFileForDataSetWithTimeout(dataSetCode, path, validityDurationInSeconds, function(response){
			ok(response.result, "Got download url");

			downloadFile(response.result, function(data){
				ok(data, "Download url works once");
				
				downloadFile(response.result, function(data){
					ok(!data, "Download url does not work anymore");
					facade.close();	
				});
			});
		});
	});
});

test("createSessionWorkspaceDownloadUrl()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = null;
		
		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function(){
			facade.createSessionWorkspaceDownloadUrl(fileName, function(downloadUrl){
				downloadFile(downloadUrl, function(response){
					equal(response, fileContent, "Download url is correct");
					facade.close();
				});
			});
		});
	});
});

test("createSessionWorkspaceDownloadUrlForDataStore()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS-SCREENING";
		
		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function(){
			facade.createSessionWorkspaceDownloadUrlForDataStore(fileName, dataStoreCode, function(downloadUrl){
				downloadFile(downloadUrl, function(response){
					equal(response, fileContent, "Download url is correct");
					facade.close();
				});
			});
		});
	});
});

test("createSessionWorkspaceDownloadLink()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var linkText = generateRandomString();
		
		facade.createSessionWorkspaceDownloadUrl(fileName, function(downloadUrl){
			facade.createSessionWorkspaceDownloadLink(fileName, linkText, function(link){
				equal($(link).attr("href"), downloadUrl, "Link has correct url");
				equal($(link).text(), linkText, "Link has correct text");
				facade.close();
			});
		});
	});
});

test("createSessionWorkspaceDownloadLinkForDataStore()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var linkText = generateRandomString();
		var dataStoreCode = "DSS-SCREENING";
		
		facade.createSessionWorkspaceDownloadUrlForDataStore(fileName, dataStoreCode, function(downloadUrl){
			facade.createSessionWorkspaceDownloadLinkForDataStore(fileName, linkText, dataStoreCode, function(link){
				equal($(link).attr("href"), downloadUrl, "Link has correct url");
				equal($(link).text(), linkText, "Link has correct text");
				facade.close();
			});
		});
	});
});

test("downloadSessionWorkspaceFile()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = null;
		
		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function(){
			facade.downloadSessionWorkspaceFile(fileName, function(response){
				equal(response, fileContent, "File has been downloaded");
				facade.close();
			});
		});
	});
});

test("downloadSessionWorkspaceFileForDataStore()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS-SCREENING";
		
		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function(){
			facade.downloadSessionWorkspaceFileForDataStore(fileName, dataStoreCode, function(response){
				equal(response, fileContent, "File has been downloaded");
				facade.close();
			});
		});
	});
});

test("deleteSessionWorkspaceFile()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = null;
		
		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function(){
			facade.deleteSessionWorkspaceFile(fileName, function(){
				facade.downloadSessionWorkspaceFile(fileName, function(response){
					ok(response.error.indexOf("No such file or directory"), "File has been deleted");
					facade.close();
				});
			});
		});
	});
});

test("deleteSessionWorkspaceFileForDataStore()", function(){
	createFacadeAndLogin(function(facade){
		var fileName = generateRandomString();
		var fileContent = generateRandomString();
		var dataStoreCode = "DSS-SCREENING";
		
		uploadFileToSessionWorkspace(facade, fileName, fileContent, dataStoreCode, function(){
			facade.deleteSessionWorkspaceFileForDataStore(fileName, dataStoreCode, function(){
				facade.downloadSessionWorkspaceFileForDataStore(fileName, dataStoreCode, function(response){
					ok(response.error.indexOf("No such file or directory"), "File has been deleted");
					facade.close();
				});
			});
		});
	});
});

test("getPathToDataSet()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = "20110913114645299-83009";
		var overrideStoreRootPathOrNull = "";
		
		facade.getPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response){
			equal(response.result, "/1/678243C3-BD97-42E4-B04B-34DA0C43564D/04/b7/53/20110913114645299-83009", "Data set path is correct");
			facade.close();
		});
	});
});

test("listAllShares()", function(){
	createFacadeAndLogin(function(facade){
		facade.listAllShares(function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "shareId", ["1", "2"]);
			facade.close();
		});
	});
});

test("listAllSharesForDataStore()", function(){
	createFacadeAndLogin(function(facade){
		var dataStoreCode = "DSS-SCREENING";
		
		facade.listAllSharesForDataStore(dataStoreCode, function(response){
			assertObjectsCount(response.result, 2);
			assertObjectsWithValues(response.result, "shareId", ["1", "2"]);
			facade.close();
		});
	});
});

test("shuffleDataSet()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetCode = "20110817134351959-81695";
		var share1Id = "1";
		var share2Id = "2";
		var overrideStoreRootPathOrNull = "";
		var isInShare = function(dataSetPath, shareId){
			return dataSetPath.indexOf("/" + shareId + "/") == 0
		}
		
		facade.getPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response){
			var toShareId = null;
			
			if(isInShare(response.result, share1Id)){
				toShareId = share2Id;
			}else{
				toShareId = share1Id;
			}
			
			facade.shuffleDataSet(dataSetCode, toShareId, function(response){
				facade.getPathToDataSet(dataSetCode, overrideStoreRootPathOrNull, function(response){
					ok(isInShare(response.result, toShareId), "Data set has been moved to a different share");
					facade.close();
				});
			});
		});
	});
});

test("getValidationScript()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetTypeOrNull = "HCS_IMAGE_RAW";
		
		facade.getValidationScript(dataSetTypeOrNull, function(response){
			ok(response.result, "Got a validation script");
			facade.close();
		});
	});
});

test("getValidationScriptForDataStore()", function(){
	createFacadeAndLogin(function(facade){
		var dataSetTypeOrNull = "HCS_IMAGE_RAW";
		var dataStoreCode = "DSS-SCREENING";
		
		facade.getValidationScriptForDataStore(dataSetTypeOrNull, dataStoreCode, function(response){
			ok(response.result, "Got a validation script");
			facade.close();
		});
	});
});

test("openbisWebAppContext()", function(){
	createFacadeAndLogin(function(facade){
		var context = new openbisWebAppContext();
		equal(context.getWebappCode(), "openbis-test", "Webapp code is correct");
		ok(!context.getEntityKind(), "Entity kind is correct");
		ok(!context.getEntityType(), "Entity type is correct");
		ok(!context.getEntityIdentifier(), "Entity identifier is correct");
		ok(!context.getEntityPermId(), "Entity perm id is correct");
		
		facade.getSessionTokenFromServer(function(response){
			equal(context.getSessionId(), response.result, "Session token is correct");
			facade.close();
		});
	});
});