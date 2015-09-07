define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var MaterialTypeSortOptions = function() {
	};
	stjs.extend(MaterialTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.material.MaterialTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialTypeSortOptions;
})