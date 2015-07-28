define([ 'test/login', 'test/space', 'test/project', 'test/experiment', 'test/sample', 'test/dataset', 'test/material', 'test/deletion', 'test/dto' ], function() {
	var testSuites = arguments;
	return function() {
		for (var i = 0; i < testSuites.length; i++) {
			testSuites[i]();
		}
	};
});