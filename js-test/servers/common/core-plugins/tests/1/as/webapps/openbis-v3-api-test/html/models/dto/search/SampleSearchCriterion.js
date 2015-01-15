/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractEntitySearchCriterion) {
    var SampleSearchCriterion = function() {
        this.this(SampleSearchRelation.SAMPLE);
    };
    stjs.extend(SampleSearchCriterion, AbstractEntitySearchCriterion, [AbstractEntitySearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'SampleSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.relation = null;
        prototype.withSpace = function() {
            return this.with(new SpaceSearchCriterion());
        };
        prototype.withExperiment = function() {
            return this.with(new ExperimentSearchCriterion());
        };
        prototype.withParents = function() {
            return this.with(new SampleParentsSearchCriterion());
        };
        prototype.withChildren = function() {
            return this.with(new SampleChildrenSearchCriterion());
        };
        prototype.withContainer = function() {
            return this.with(new SampleContainerSearchCriterion());
        };
        prototype.withOrOperator = function() {
            return this.withOperator(SearchOperator.OR);
        };
        prototype.withAndOperator = function() {
            return this.withOperator(SearchOperator.AND);
        };
        prototype.getRelation = function() {
            return this.relation;
        };
        prototype.createBuilder = function() {
            var builder = AbstractEntitySearchCriterion.prototype.createBuilder.call(this);
            builder.setName(this.relation.name());
            return builder;
        };
    }, {relation: {name: "Enum", arguments: ["SampleSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return SampleSearchCriterion;
})