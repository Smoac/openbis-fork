define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/property/fetchoptions/PropertyAssignmentSortOptions" ], function(stjs, FetchOptions) {
	var PropertyAssignmentFetchOptions = function() {
	};
	stjs.extend(PropertyAssignmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyAssignmentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var PropertyAssignmentSortOptions = require("as/dto/property/fetchoptions/PropertyAssignmentSortOptions");
				this.sort = new PropertyAssignmentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "PropertyAssignmentSortOptions"
	});
	return PropertyAssignmentFetchOptions;
})