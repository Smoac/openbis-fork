define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var TagSortOptions = function() {
	};
	stjs.extend(TagSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.tag.TagSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return TagSortOptions;
})