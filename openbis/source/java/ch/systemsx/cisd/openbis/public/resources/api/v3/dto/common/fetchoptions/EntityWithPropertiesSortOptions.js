define([ "require", "stjs", "dto/common/fetchoptions/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var EntityWithPropertiesSortOptions = function() {
		EntitySortOptions.call(this);
	};

	var fields = {
		PROPERTY : "PROPERTY"
	};

	stjs.extend(EntityWithPropertiesSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.fetchoptions.EntityWithPropertiesSortOptions';
		constructor.serialVersionUID = 1;

		prototype.property = function(propertyName) {
			return this.getOrCreateSorting(fields.PROPERTY + propertyName);
		};
		prototype.getProperty = function(propertyName) {
			return this.getSorting(fields.PROPERTY + propertyName);
		};

	}, {});
	return EntityWithPropertiesSortOptions;
})