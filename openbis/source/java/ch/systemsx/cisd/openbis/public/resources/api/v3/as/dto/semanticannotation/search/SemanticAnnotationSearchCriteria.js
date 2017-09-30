define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/PermIdSearchCriteria", "as/dto/entitytype/search/EntityTypeSearchCriteria",
		"as/dto/property/search/PropertyTypeSearchCriteria", "as/dto/property/search/PropertyAssignmentSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var SemanticAnnotationSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(SemanticAnnotationSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.semanticannotation.search.SemanticAnnotationSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("as/dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.withEntityType = function() {
			var EntityTypeSearchCriteria = require("as/dto/entitytype/search/EntityTypeSearchCriteria");
			return this.addCriteria(new EntityTypeSearchCriteria());
		};
		prototype.withPropertyType = function() {
			var PropertyTypeSearchCriteria = require("as/dto/property/search/PropertyTypeSearchCriteria");
			return this.addCriteria(new PropertyTypeSearchCriteria());
		};
		prototype.withPropertyAssignment = function() {
			var PropertyAssignmentSearchCriteria = require("as/dto/property/search/PropertyAssignmentSearchCriteria");
			return this.addCriteria(new PropertyAssignmentSearchCriteria());
		};
	}, {});

	return SemanticAnnotationSearchCriteria;
})