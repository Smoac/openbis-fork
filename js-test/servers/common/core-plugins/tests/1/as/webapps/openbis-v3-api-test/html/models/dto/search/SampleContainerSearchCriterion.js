/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, SampleSearchCriterion) {
    var SampleContainerSearchCriterion = function() {
        SampleSearchCriterion.call(this, SampleSearchRelation.CONTAINER);
    };
    stjs.extend(SampleContainerSearchCriterion, SampleSearchCriterion, [SampleSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'SampleContainerSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {relation: {name: "Enum", arguments: ["SampleSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return SampleContainerSearchCriterion;
})