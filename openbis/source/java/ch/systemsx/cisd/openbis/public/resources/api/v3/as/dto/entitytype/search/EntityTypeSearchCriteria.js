define([ "require", "stjs", "as/dto/common/search/AbstractObjectSearchCriteria", "as/dto/common/search/CodeSearchCriteria", "as/dto/entitytype/search/EntityKindSearchCriteria" ], function(require,
		stjs, AbstractObjectSearchCriteria, CodeSearchCriteria, EntityKindSearchCriteria) {
	var EntityTypeSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(EntityTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.search.EntityTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("as/dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withKind = function() {
			var EntityKindSearchCriteria = require("as/dto/entitytype/search/EntityKindSearchCriteria");
			return this.addCriteria(new EntityKindSearchCriteria());
		};
	}, {});

	return EntityTypeSearchCriteria;
})