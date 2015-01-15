/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, SampleSearchCriterion) {
    var SampleChildrenSearchCriterion = function() {
        SampleSearchCriterion.call(this, SampleSearchRelation.CHILDREN);
    };
    stjs.extend(SampleChildrenSearchCriterion, SampleSearchCriterion, [SampleSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'SampleChildrenSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {relation: {name: "Enum", arguments: ["SampleSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return SampleChildrenSearchCriterion;
})