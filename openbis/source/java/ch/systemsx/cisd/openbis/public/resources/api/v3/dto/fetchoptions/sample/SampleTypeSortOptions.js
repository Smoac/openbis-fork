define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var SampleTypeSortOptions = function() {
	};
	stjs.extend(SampleTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.sample.SampleTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleTypeSortOptions;
})