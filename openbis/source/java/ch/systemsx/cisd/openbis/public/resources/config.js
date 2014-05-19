var require = {
	baseUrl : "/openbis/resources",
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
