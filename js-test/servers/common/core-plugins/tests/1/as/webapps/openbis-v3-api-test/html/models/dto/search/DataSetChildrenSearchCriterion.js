/**
 * @author Jakub Straszewski
 */
define([ "support/stjs", "dto/search/DataSetSearchCriterion", "dto/search/DataSetSearchRelation" ], function(stjs, DataSetSearchCriterion, DataSetSearchRelation) {
	var DataSetChildrenSearchCriterion = function() {
		DataSetSearchCriterion.call(this, DataSetSearchRelation.CHILDREN);
	};
	stjs.extend(DataSetChildrenSearchCriterion, DataSetSearchCriterion, [ DataSetSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DataSetChildrenSearchCriterion';
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
	return DataSetChildrenSearchCriterion;
})