/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractObjectSearchCriteria", "dto/search/CodeSearchCriteria", "dto/search/PermIdSearchCriteria" ], function(stjs, AbstractObjectSearchCriteria,
		CodeSearchCriteria, PermIdSearchCriteria) {
	var EntityTypeSearchCriteria = function() {
		AbstractObjectSearchCriteria.call(this);
	};
	stjs.extend(EntityTypeSearchCriteria, AbstractObjectSearchCriteria, [ AbstractObjectSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.EntityTypeSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			return this.addCriteria(new CodeSearchCriteria());
		};
		prototype.withPermId = function() {
			return this.addCriteria(new PermIdSearchCriteria());
		};
		prototype.createBuilder = function() {
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