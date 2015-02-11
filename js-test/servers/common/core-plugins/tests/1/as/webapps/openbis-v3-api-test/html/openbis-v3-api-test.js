define([ 'experiment-tests', 'sample-tests' ], function(experimentTests, sampleTests) {
	return function() {
		experimentTests();
		sampleTests();
	}
});