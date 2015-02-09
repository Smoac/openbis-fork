/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractObjectSearchCriterion", "dto/search/CodeSearchCriterion", "dto/search/PermIdSearchCriterion" ], function(stjs, AbstractObjectSearchCriterion,
		CodeSearchCriterion, PermIdSearchCriterion) {
	var EntityTypeSearchCriterion = function() {
		AbstractObjectSearchCriterion.call(this);
	};
	stjs.extend(EntityTypeSearchCriterion, AbstractObjectSearchCriterion, [ AbstractObjectSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.EntityTypeSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			return this.addCriterion(new CodeSearchCriterion());
		};
		prototype.withPermId = function() {
			return this.addCriterion(new PermIdSearchCriterion());
		};
		prototype.createBuilder = function() {
			var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
			builder.setName("TYPE");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return EntityTypeSearchCriterion;
})