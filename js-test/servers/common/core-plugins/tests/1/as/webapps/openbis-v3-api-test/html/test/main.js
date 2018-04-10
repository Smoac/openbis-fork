define([ 
         'test/test-login', 
         'test/test-jsVSjava', 
         'test/test-create', 'test/test-update', 'test/test-search', 

         'test/test-get', 
         'test/test-delete', 

//         'test/test-dto', 
         'test/test-dto-roundtrip', 
         'test/test-custom-services',
         'test/test-dss-services',
         'test/test-archive-unarchive'
         ], function() {
	var testSuites = arguments;
	return function() {
		for (var i = 0; i < testSuites.length; i++) {
			testSuites[i]();
		}
	};
});