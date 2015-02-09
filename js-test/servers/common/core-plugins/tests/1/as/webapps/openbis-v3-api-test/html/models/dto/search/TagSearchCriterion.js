/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractObjectSearchCriterion", "dto/search/CodeSearchCriterion", "dto/search/PermIdSearchCriterion", "dto/search/AbstractCompositeSearchCriterion" ], function(
		stjs, AbstractObjectSearchCriterion, CodeSearchCriterion, PermIdSearchCriterion, AbstractCompositeSearchCriterion) {
	var TagSearchCriterion = function() {
		AbstractObjectSearchCriterion.call(this);
	};
	stjs.extend(TagSearchCriterion, AbstractObjectSearchCriterion, [ AbstractObjectSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.TagSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.withCode = function() {
			return this.addCriterion(new CodeSearchCriterion());
		};
		prototype.withPermId = function() {
			return this.addCriterion(new PermIdSearchCriterion());
		};
		prototype.createBuilder = function() {
			var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
			builder.setName("TAG");
			return builder;
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return TagSearchCriterion;
})