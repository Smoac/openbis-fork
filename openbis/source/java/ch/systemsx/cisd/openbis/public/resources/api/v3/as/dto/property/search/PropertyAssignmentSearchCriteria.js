define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/entitytype/search/EntityTypeSearchCriteria", "as/dto/property/search/PropertyTypeSearchCriteria" ], function(
		require, stjs, AbstractObjectSearchCriteria, EntityTypeSearchCriteria, PropertyTypeSearchCriteria) {
	var PropertyAssignmentSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(PropertyAssignmentSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.search.PropertyAssignmentSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withEntityType = function() {
			var EntityTypeSearchCriteria = require("as/dto/entitytype/search/EntityTypeSearchCriteria");
			return this.addCriteria(new EntityTypeSearchCriteria());
		};
		prototype.withPropertyType = function() {
			var PropertyTypeSearchCriteria = require("as/dto/property/search/PropertyTypeSearchCriteria");
			return this.addCriteria(new PropertyTypeSearchCriteria());
		};
	}, {});

	return PropertyAssignmentSearchCriteria;
})