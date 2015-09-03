/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/ISearchCriterion" ], function(stjs, ISearchCriterion) {
	var NoExperimentSearchCriterion = function() {
	};
	stjs.extend(NoExperimentSearchCriterion, null, [ ISearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NoExperimentSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {});
	return NoExperimentSearchCriterion;
})
