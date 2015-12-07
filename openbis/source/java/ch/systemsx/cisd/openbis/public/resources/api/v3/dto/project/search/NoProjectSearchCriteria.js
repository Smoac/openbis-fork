/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoProjectSearchCriteria = function() {
	};
	stjs.extend(NoProjectSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.project.search.NoProjectSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return NoProjectSearchCriteria;
})
