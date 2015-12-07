define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var DeletionSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(DeletionSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.fetchoptions.DeletionSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DeletionSortOptions;
})