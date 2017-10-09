define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/entitytype/search/EntityTypeSearchCriteria", "as/dto/property/search/PropertyTypeSearchCriteria",
		"as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria, EntityTypeSearchCriteria, PropertyTypeSearchCriteria,
		SemanticAnnotationSearchCriteria) {
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
		prototype.withSemanticAnnotations = function() {
			var SemanticAnnotationSearchCriteria = require("as/dto/semanticannotation/search/SemanticAnnotationSearchCriteria");
			return this.addCriteria(new SemanticAnnotationSearchCriteria());
		};
	}, {});

	return PropertyAssignmentSearchCriteria;
})