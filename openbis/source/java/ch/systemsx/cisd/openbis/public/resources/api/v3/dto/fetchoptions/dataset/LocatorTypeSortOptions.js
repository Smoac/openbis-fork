define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var LocatorTypeSortOptions = function() {
	};
	stjs.extend(LocatorTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.LocatorTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return LocatorTypeSortOptions;
})