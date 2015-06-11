define([ 'test/openbis-v3-api-experiment-tests', 'test/openbis-v3-api-sample-tests', 'test/openbis-v3-api-dto-search-criterion-tests' ], function() {
	var testSuites = arguments;
	return function() {
		for (var i = 0; i < testSuites.length; i++) {
			testSuites[i]();
		}
	};
});