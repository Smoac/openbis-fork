define([ "stjs" ], function(stjs) {
	var WebAppSettings = function() {
	};
	stjs.extend(WebAppSettings, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.webapp.WebAppSettings';
		constructor.serialVersionUID = 1;
		prototype.webAppId = null;
		prototype.settings = {};

		prototype.getWebAppId = function() {
			return this.webAppId;
		};
		prototype.setWebAppId = function(webAppId) {
			this.webAppId = webAppId;
		};
		
		prototype.getSettings = function() {
			return this.settings;
		};
		prototype.setSettings = function(settings) {
			this.settings = settings;
		};
	}, {
		settings : {
			name : "Map",
			arguments : [ null, null ]
		},
	});
	return WebAppSettings;
})