define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PropertyTypeSortOptions = function() {
		SortOptions.call(this);
	};

	stjs.extend(PropertyTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyTypeSortOptions;
})