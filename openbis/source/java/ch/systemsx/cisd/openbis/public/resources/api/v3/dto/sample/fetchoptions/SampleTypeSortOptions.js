define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var SampleTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(SampleTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.fetchoptions.SampleTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SampleTypeSortOptions;
})