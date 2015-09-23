define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var DataSetTypeSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(DataSetTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.DataSetTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetTypeSortOptions;
})