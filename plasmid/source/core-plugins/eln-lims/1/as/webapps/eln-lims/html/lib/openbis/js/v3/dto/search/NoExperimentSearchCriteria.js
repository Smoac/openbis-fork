/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoExperimentSearchCriteria = function() {
	};
	stjs.extend(NoExperimentSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NoExperimentSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return NoExperimentSearchCriteria;
})
