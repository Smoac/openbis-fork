/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/NoSampleSearchCriteria" ], function(stjs, NoSampleSearchCriteria) {
	var NoSampleContainerSearchCriteria = function() {
	};
	stjs.extend(NoSampleContainerSearchCriteria, NoSampleSearchCriteria, [ NoSampleSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NoSampleContainerSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return NoSampleContainerSearchCriteria;
})
