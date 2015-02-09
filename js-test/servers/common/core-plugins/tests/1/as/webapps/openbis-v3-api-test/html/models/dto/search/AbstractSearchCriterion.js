/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/ISearchCriterion" ], function(stjs, ISearchCriterion) {
	var AbstractSearchCriterion = function() {
	};
	stjs.extend(AbstractSearchCriterion, null, [ ISearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractSearchCriterion;
})