define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", 
         "as/dto/plugin/fetchoptions/PluginSortOptions" ], function(
		stjs, FetchOptions) {
	var PluginFetchOptions = function() {
	};
	stjs.extend(PluginFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.fetchoptions.PluginFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.script = null;
		prototype.sort = null;

		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withScript = function() {
			this.script = true;
			return this;
		};
		prototype.isWithScript = function() {
			return this.script;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PluginSortOptions = require("as/dto/plugin/fetchoptions/PluginSortOptions");
				this.sort = new PluginSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		sort : "PluginSortOptions"
	});
	return PluginFetchOptions;
})