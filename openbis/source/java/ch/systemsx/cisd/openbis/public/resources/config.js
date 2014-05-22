var require = (function() {

	var getBaseUrl = function() {
		var path = window.location.pathname;
		var index = path.substr(1).indexOf("/") + 1;
		var baseUrl = path.substring(0, index) + "/resources";
		return baseUrl;
	}

	return {
		baseUrl : getBaseUrl(),
		paths : {
			"jquery" : "js/jquery",
			"openbis" : "js/openbis",
			"openbis-screening" : "js/openbis-screening",
			"bootstrap" : "lib/bootstrap/js/bootstrap.min",
			"bootstrap-slider" : "lib/bootstrap-slider/js/bootstrap-slider.min"
		},
		shim : {
			"openbis" : {
				deps : [ "jquery" ],
				exports : "openbis"
			},
			"openbis-screening" : {
				deps : [ "openbis" ],
				exports : "openbis"
			}
		}
	}

})();
