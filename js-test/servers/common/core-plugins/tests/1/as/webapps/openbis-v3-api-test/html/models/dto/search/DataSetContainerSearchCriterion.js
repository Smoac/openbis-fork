/**
 *  @author Jakub Straszewski
 */
define(["support/stjs"], function (stjs, DataSetSearchCriterion) {
    var DataSetContainerSearchCriterion = function() {
        DataSetSearchCriterion.call(this, DataSetSearchRelation.CONTAINER);
    };
    stjs.extend(DataSetContainerSearchCriterion, DataSetSearchCriterion, [DataSetSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'DataSetContainerSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {relation: {name: "Enum", arguments: ["DataSetSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return DataSetContainerSearchCriterion;
})