var require = (function() {

	var getBaseUrl = function() {
		var path = window.location.pathname + "lib/openbis/js/v3";
		return path;
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