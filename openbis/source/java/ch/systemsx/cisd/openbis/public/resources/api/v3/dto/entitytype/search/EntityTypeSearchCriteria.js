/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/common/search/AbstractObjectSearchCriteria", "dto/common/search/CodeSearchCriteria", "dto/common/search/PermIdSearchCriteria",
		"dto/common/search/AbstractCompositeSearchCriteria" ], function(require, stjs, AbstractObjectSearchCriteria) {
	var EntityTypeSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(EntityTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entitytype.search.EntityTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			var CodeSearchCriteria = require("dto/common/search/CodeSearchCriteria");
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			var PermIdSearchCriteria = require("dto/common/search/PermIdSearchCriteria");
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.createBuilder = function() {
			var AbstractCompositeSearchCriteria = require("dto/common/search/AbstractCompositeSearchCriteria");
			var builder = AbstractCompositeSearchCriteria.prototype.createBuilder.call(this);
			builder.setName("TYPE");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriteria" ]
		}
	});
	return EntityTypeSearchCriteria;
})