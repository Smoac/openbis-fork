define([ "require", "stjs", "as/dto/common/fetchoptions/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var EntityWithPropertiesSortOptions = function() {
		EntitySortOptions.call(this);
	};

	var fields = {
		TYPE : "TYPE",
		PROPERTY : "PROPERTY"
	};

	stjs.extend(EntityWithPropertiesSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.fetchoptions.EntityWithPropertiesSortOptions';
		constructor.serialVersionUID = 1;

		prototype.type = function() {
			return this.getOrCreateSorting(fields.TYPE);
		};
		prototype.getType = function() {
			return this.getSorting(fields.TYPE);
		};
		prototype.property = function(propertyName) {
			return this.getOrCreateSorting(fields.PROPERTY + propertyName);
		};
		prototype.getProperty = function(propertyName) {
			return this.getSorting(fields.PROPERTY + propertyName);
		};

	}, {});
	return EntityWithPropertiesSortOptions;
})