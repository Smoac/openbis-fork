/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoProjectSearchCriteria = function() {
	};
	stjs.extend(NoProjectSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NoProjectSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return NoProjectSearchCriteria;
})
