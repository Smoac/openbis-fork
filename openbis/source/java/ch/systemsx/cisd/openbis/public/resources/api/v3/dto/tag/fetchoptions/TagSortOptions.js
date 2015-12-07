define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var TagSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(TagSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.tag.fetchoptions.TagSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return TagSortOptions;
})