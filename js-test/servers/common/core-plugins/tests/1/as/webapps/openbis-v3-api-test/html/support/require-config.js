var requirejs = {
	paths : {
		"jquery" : "/openbis/resources/js/jquery",
		"dto" : "models/dto",
		"sys" : "models/sys"
	},
	shim : {
		'support/stjs' : {
			exports : "stjs",
			deps : [ 'support/underscore-min.js' ]
		},
		'support/underscore-min' : {
			exports : "_"
		}
	}
}
