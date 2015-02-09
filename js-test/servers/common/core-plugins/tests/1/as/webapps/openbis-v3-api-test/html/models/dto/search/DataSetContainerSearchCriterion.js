/**
 * @author Jakub Straszewski
 */
define([ "support/stjs", "dto/search/DataSetSearchCriterion", "dto/search/DataSetSearchRelation" ], function(stjs, DataSetSearchCriterion, DataSetSearchRelation) {
	var DataSetContainerSearchCriterion = function() {
		DataSetSearchCriterion.call(this, DataSetSearchRelation.CONTAINER);
	};
	stjs.extend(DataSetContainerSearchCriterion, DataSetSearchCriterion, [ DataSetSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetContainerSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		relation : {
			name : "Enum",
			arguments : [ "DataSetSearchRelation" ]
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
	return DataSetContainerSearchCriterion;
})