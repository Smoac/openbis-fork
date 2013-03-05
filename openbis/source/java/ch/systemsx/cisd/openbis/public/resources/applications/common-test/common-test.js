var createFacade = function(){
	return new openbis('http://127.0.0.1:8888/openbis/openbis');
}

var createFacadeAndLogin = function(action, timeoutOrNull){
	stop();
	
	var facade = createFacade();

	facade.login('admin','password', function(){
		action(facade);
	});
	
	setTimeout(function(){
		start();
	}, (timeoutOrNull ? timeoutOrNull : 1000));
}

var createSearchCriteriaForCodes = function(codes){
	var clauses = [];
	
	$.each(codes, function(index, code){
		clauses.push({"@type":"AttributeMatchClause",
			attribute : "CODE",
			fieldType : "ATTRIBUTE",
			desiredValue : code 
		});
	});
	
	var searchCriteria = {
		"@type" : "SearchCriteria",
		matchClauses : clauses,
		operator : "MATCH_ANY_CLAUSES"
	};
	
	return searchCriteria;
}

var assertObjectsCount = function(objects, count){
	equal(objects.length, count, 'Got ' + count + ' object(s)');
}

var assertObjectsWithCodes = function(objects, codes){
	deepEqual(objects.map(function(object){
		return object.code;
	}).sort(), codes.sort(), 'Objects have correct codes')
}

var assertObjectsWithProperties = function(objects){
	ok(objects.some(function(object){
		return object.properties && Object.keys(object.properties).length > 0;
	}), 'Object properties have been fetched');
}

var assertObjectsWithoutProperties = function(objects){
	ok(objects.every(function(object){
		return !object.properties || Object.keys(object.properties).length == 0;
	}), 'Object properties havent been fetched');
}

var assertObjectsWithParentCodes = function(objects){
	equal(objects.some(function(object){
		return object.parentCodes && object.parentCodes.length > 0;
	}), true, 'Object parent codes have been fetched');
}

var assertObjectsWithoutParentCodes = function(objects){
	ok(objects.every(function(object){
		return !object.parentCodes || object.parentCodes.length == 0;
	}), 'Object parent codes havent been fetched');
}