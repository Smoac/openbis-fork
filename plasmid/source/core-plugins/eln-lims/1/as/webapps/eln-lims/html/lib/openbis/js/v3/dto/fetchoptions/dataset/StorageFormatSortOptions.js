define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var StorageFormatSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(StorageFormatSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.StorageFormatSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return StorageFormatSortOptions;
})