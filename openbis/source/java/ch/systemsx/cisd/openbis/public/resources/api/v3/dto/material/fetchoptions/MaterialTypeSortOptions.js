define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var MaterialTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(MaterialTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.material.fetchoptions.MaterialTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialTypeSortOptions;
})