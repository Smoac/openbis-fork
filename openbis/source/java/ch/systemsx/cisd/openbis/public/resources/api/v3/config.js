var require = (function() {

	var getBaseUrl = function() {
		var path = window.location.pathname;
		var index = path.substr(1).indexOf("/") + 1;
		var baseUrl = path.substring(0, index) + "/resources/api/v3";
		return baseUrl;
	}

	return {
		baseUrl : getBaseUrl(),
		paths : {
			"jquery" : "lib/jquery/js/jquery",
			"stjs" : "lib/stjs/js/stjs",
			"underscore" : "lib/underscore/js/underscore",
			"moment" : "lib/moment/js/moment"
		},
		shim : {
			"stjs" : {
				exports : "stjs",
				deps : [ "underscore" ]
			},
			"underscore" : {
				exports : "_"
			}
		}
	}

})();