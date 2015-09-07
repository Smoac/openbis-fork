define([ "require", "stjs", "dto/fetchoptions/sort/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var DataSetSortOptions = function() {
	};
	stjs.extend(DataSetSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.DataSetSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetSortOptions;
})