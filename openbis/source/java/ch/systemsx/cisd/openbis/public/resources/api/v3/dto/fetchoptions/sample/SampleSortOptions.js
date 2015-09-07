define([ "require", "stjs", "dto/fetchoptions/sort/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var SampleSortOptions = function() {
	};
	stjs.extend(SampleSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.sample.SampleSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleSortOptions;
})