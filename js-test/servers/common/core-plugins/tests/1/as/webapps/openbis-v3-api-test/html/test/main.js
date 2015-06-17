define([ 'test/space', 'test/project', 'test/experiment', 'test/sample', 'test/material', 'test/dto' ], function() {
	var testSuites = arguments;
	return function() {
		for (var i = 0; i < testSuites.length; i++) {
			testSuites[i]();
		}
	};
});