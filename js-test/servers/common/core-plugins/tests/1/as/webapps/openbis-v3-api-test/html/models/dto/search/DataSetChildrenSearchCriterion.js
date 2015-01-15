/**
 *  @author Jakub Straszewski
 */
define(["support/stjs"], function (stjs, DataSetSearchCriterion) {
    var DataSetChildrenSearchCriterion = function() {
        DataSetSearchCriterion.call(this, DataSetSearchRelation.CHILDREN);
    };
    stjs.extend(DataSetChildrenSearchCriterion, DataSetSearchCriterion, [DataSetSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'DataSetChildrenSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {relation: {name: "Enum", arguments: ["DataSetSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return DataSetChildrenSearchCriterion;
})