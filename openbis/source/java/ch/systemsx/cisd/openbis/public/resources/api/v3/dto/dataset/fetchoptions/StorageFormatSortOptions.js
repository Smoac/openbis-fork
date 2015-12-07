define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var StorageFormatSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(StorageFormatSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.fetchoptions.StorageFormatSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return StorageFormatSortOptions;
})