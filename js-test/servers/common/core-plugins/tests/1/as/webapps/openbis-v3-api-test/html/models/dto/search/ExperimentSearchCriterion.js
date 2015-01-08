/**
 *  @author pkupczyk
 */
define([], function (AbstractEntitySearchCriterion) {
    var ExperimentSearchCriterion = function() {
        AbstractEntitySearchCriterion.call(this);
    };
    stjs.extend(ExperimentSearchCriterion, AbstractEntitySearchCriterion, [AbstractEntitySearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'ExperimentSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.withProject = function() {
            return this.with(new ProjectSearchCriterion());
        };
        prototype.withOrOperator = function() {
            return this.withOperator(SearchOperator.OR);
        };
        prototype.withAndOperator = function() {
            return this.withOperator(SearchOperator.AND);
        };
        prototype.createBuilder = function() {
            var builder = AbstractEntitySearchCriterion.prototype.createBuilder.call(this);
            builder.setName("EXPERIMENT");
            return builder;
        };
    }, {operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return ExperimentSearchCriterion;
})