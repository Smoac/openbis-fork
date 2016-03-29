define([ "require", "stjs", "as/dto/common/fetchoptions/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var SampleSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(SampleSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.fetchoptions.SampleSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleSortOptions;
})