define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var DeletionSortOptions = function() {
	};
	stjs.extend(DeletionSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.deletion.DeletionSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DeletionSortOptions;
})