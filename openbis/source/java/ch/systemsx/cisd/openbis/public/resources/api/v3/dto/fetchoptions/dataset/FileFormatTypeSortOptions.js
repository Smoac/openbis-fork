define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var FileFormatTypeSortOptions = function() {
	};
	stjs.extend(FileFormatTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.FileFormatTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return FileFormatTypeSortOptions;
})