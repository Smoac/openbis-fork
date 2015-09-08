define([ "require", "stjs", "dto/fetchoptions/sort/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var SpaceSortOptions = function() {
		EntitySortOptions.call(this);
	};
	stjs.extend(SpaceSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.space.SpaceSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return SpaceSortOptions;
})