/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractCompositeSearchCriterion", "dto/search/IdSearchCriterion" ], function(stjs, AbstractCompositeSearchCriterion, IdSearchCriterion) {
	var AbstractObjectSearchCriterion = function() {
		AbstractCompositeSearchCriterion.call(this);
	};
	stjs.extend(AbstractObjectSearchCriterion, AbstractCompositeSearchCriterion, [ AbstractCompositeSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractObjectSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.withId = function() {
			return this.addCriterion(new IdSearchCriterion());
		};
	}, {
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return AbstractObjectSearchCriterion;
})