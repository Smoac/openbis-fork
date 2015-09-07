define([ "require", "stjs", "dto/fetchoptions/sort/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var MaterialSortOptions = function() {
	};
	stjs.extend(MaterialSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.material.MaterialSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialSortOptions;
})