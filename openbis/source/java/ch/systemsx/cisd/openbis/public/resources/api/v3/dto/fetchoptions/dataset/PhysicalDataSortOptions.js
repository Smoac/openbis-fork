define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var PhysicalDataSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(PhysicalDataSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.PhysicalDataSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PhysicalDataSortOptions;
})