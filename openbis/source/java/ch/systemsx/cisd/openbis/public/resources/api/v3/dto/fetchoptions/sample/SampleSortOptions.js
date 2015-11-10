define([ "require", "stjs", "dto/fetchoptions/sort/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var SampleSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(SampleSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.sample.SampleSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleSortOptions;
})