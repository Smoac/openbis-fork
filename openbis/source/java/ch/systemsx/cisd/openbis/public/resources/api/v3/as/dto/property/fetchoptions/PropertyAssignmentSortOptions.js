define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PropertyAssignmentSortOptions = function() {
		SortOptions.call(this);
	};

	var fields = {
		CODE : "CODE",
		LABEL : "LABEL"
	};

	stjs.extend(PropertyAssignmentSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.fetchoptions.PropertyAssignmentSortOptions';
		constructor.serialVersionUID = 1;
		prototype.code = function() {
			return this.getOrCreateSorting(fields.CODE);
		};
		prototype.getCode = function() {
			return this.getSorting(fields.CODE);
		};
		prototype.label = function() {
			return this.getOrCreateSorting(fields.LABEL);
		};
		prototype.getLabel = function() {
			return this.getSorting(fields.LABEL);
		};
	}, {});
	return PropertyAssignmentSortOptions;
})