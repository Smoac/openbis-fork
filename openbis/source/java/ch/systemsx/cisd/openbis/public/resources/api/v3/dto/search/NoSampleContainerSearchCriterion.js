/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/NoSampleSearchCriterion" ], function(stjs, NoSampleSearchCriterion) {
	var NoSampleContainerSearchCriterion = function() {
	};
	stjs.extend(NoSampleContainerSearchCriterion, NoSampleSearchCriterion, [ NoSampleSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NoSampleContainerSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {});
	return NoSampleContainerSearchCriterion;
})
