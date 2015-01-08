/**
 *  @author pkupczyk
 */
define([], function (SampleSearchCriterion) {
    var SampleParentsSearchCriterion = function() {
        SampleSearchCriterion.call(this, SampleSearchRelation.PARENTS);
    };
    stjs.extend(SampleParentsSearchCriterion, SampleSearchCriterion, [SampleSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'SampleParentsSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {relation: {name: "Enum", arguments: ["SampleSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return SampleParentsSearchCriterion;
})