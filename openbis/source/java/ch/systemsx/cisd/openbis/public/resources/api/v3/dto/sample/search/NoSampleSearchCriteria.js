/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/ISearchCriteria" ], function(stjs, ISearchCriteria) {
	var NoSampleSearchCriteria = function() {
	};
	stjs.extend(NoSampleSearchCriteria, null, [ ISearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.search.NoSampleSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return NoSampleSearchCriteria;
})
