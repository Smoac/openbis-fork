var createFacade = function(action, url, timeoutOrNull){
	stop();
	
	var facade = new openbis(url);
	
	facade.close = function(){
		facade.logout(function(){
			facade.closed = true;	
		});
	};

	action(facade);

	var timeout = timeoutOrNull ? timeoutOrNull : 30000;
	var checkInterval = 100;
	var intervalTotal = 0;
	
	var startWhenClosed = function(){
		if(facade.closed){
			start();
		}else{
			intervalTotal += checkInterval;
			
			if(intervalTotal < timeout){
				setTimeout(startWhenClosed, checkInterval);
			}else{
				start();
			}
		}
	};

	startWhenClosed();
}

var createFacadeAndLoginForUserAndPassword = function(user, password, action, url, timeoutOrNull){
	createFacade(function(facade){
		facade.login(user, password, function(response){
			if(!response.error){
				action(facade);
			}
		});
	}, url, timeoutOrNull);
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

var assertArrays = function(array1, array2, msg){
	deepEqual(array1.sort(), array2.sort(), msg);
}

var assertObjectsCount = function(objects, count){
	equal(objects.length, count, 'Got ' + count + ' object(s)');
}

var assertObjectsWithValues = function(objects, propertyName, propertyValues){
	var values = {};
	
	$.each(objects, function(index, object){
		var value = object[propertyName];
		if(value in values == false){
			values[value] = true;
		}
	});
	
	deepEqual(Object.keys(values).sort(), propertyValues.sort(), 'Objects have correct ' + propertyName + ' values')
}

var assertObjectsWithValuesFunction = function(objects, propertyName, propertyFunction, propertyValues){
	var values = {};
	
	$.each(objects, function(index, object){
		var value = propertyFunction(object);
		if(value in values == false){
			values[value] = true;
		}
	});
	
	deepEqual(Object.keys(values).sort(), propertyValues.sort(), 'Objects have correct ' + propertyName + ' values')
}

var assertObjectsWithCollections = function(objects, propertyName){
	ok(objects.some(function(object){
		return object[propertyName] && Object.keys(object[propertyName]).length > 0;
	}), 'Objects have ' + propertyName + ' collections');
}

var assertObjectsWithoutCollections = function(objects, propertyName){
	ok(objects.every(function(object){
		return !object[propertyName] || Object.keys(object[propertyName]).length == 0;
	}), 'Object do not have ' + propertyName + ' collections');
}

var assertObjectsWithCodes = function(objects, codes){
	assertObjectsWithValues(objects, 'code', codes);
}

var assertObjectsWithProperties = function(objects){
	assertObjectsWithCollections(objects, 'properties');
}

var assertObjectsWithoutProperties = function(objects){
	assertObjectsWithoutCollections(objects, 'properties');
}

var assertObjectsWithParentCodes = function(objects){
	assertObjectsWithCollections(objects, 'parentCodes');
}

var assertObjectsWithoutParentCodes = function(objects){
	assertObjectsWithoutCollections(objects, 'parentCodes');
}

var assertObjectsWithChildrenCodes = function(objects){
	assertObjectsWithCollections(objects, 'childrenCodes');
}

var assertObjectsWithoutChildrenCodes = function(objects){
	assertObjectsWithoutCollections(objects, 'childrenCodes');
}