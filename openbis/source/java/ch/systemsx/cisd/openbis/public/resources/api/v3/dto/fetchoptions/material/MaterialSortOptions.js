define([ "require", "stjs", "dto/fetchoptions/sort/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var MaterialSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(MaterialSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.material.MaterialSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return MaterialSortOptions;
})