/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/SampleSearchCriterion", "dto/search/SampleSearchRelation" ], function(stjs, SampleSearchCriterion, SampleSearchRelation) {
	var SampleContainerSearchCriterion = function() {
		SampleSearchCriterion.call(this, SampleSearchRelation.CONTAINER);
	};
	stjs.extend(SampleContainerSearchCriterion, SampleSearchCriterion, [ SampleSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.SampleContainerSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "SampleSearchRelation" ]
		},
		operator : {
			name : "Enum",
			arguments : [ "SearchOperator" ]
		},
		criteria : {
			name : "Collection",
			arguments : [ "ISearchCriterion" ]
		}
	});
	return SampleContainerSearchCriterion;
})