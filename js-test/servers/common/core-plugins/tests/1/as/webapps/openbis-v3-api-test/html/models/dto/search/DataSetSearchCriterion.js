/**
 *  @author pkupczyk
 */
define([], function (AbstractEntitySearchCriterion) {
    var DataSetSearchCriterion = function() {
        this.this(DataSetSearchRelation.DATASET);
    };
    stjs.extend(DataSetSearchCriterion, AbstractEntitySearchCriterion, [AbstractEntitySearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'DataSetSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.relation = null;
        prototype.withParents = function() {
            return this.with(new DataSetParentsSearchCriterion());
        };
        prototype.withChildren = function() {
            return this.with(new DataSetChildrenSearchCriterion());
        };
        prototype.withContainer = function() {
            return this.with(new DataSetContainerSearchCriterion());
        };
        prototype.withExperiment = function() {
            return this.with(new ExperimentSearchCriterion());
        };
        prototype.withSample = function() {
            return this.with(new SampleSearchCriterion());
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
    }, {relation: {name: "Enum", arguments: ["DataSetSearchRelation"]}, operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return DataSetSearchCriterion;
})