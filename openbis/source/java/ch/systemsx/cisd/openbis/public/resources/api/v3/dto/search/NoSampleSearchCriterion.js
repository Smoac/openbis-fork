/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/ISearchCriterion" ], function(stjs, ISearchCriterion) {
	var NoSampleSearchCriterion = function() {
	};
	stjs.extend(NoSampleSearchCriterion, null, [ ISearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NoSampleSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {});
	return NoSampleSearchCriterion;
})
