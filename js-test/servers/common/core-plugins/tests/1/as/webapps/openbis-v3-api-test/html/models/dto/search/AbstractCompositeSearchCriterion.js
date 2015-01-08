/**
 *  @author pkupczyk
 */
define([], function (AbstractSearchCriterion) {
    var AbstractCompositeSearchCriterion = function() {
        AbstractSearchCriterion.call(this);
    };
    stjs.extend(AbstractCompositeSearchCriterion, AbstractSearchCriterion, [AbstractSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'AbstractCompositeSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.criteria = new LinkedList();
        prototype.getCriteria = function() {
            return Collections.unmodifiableCollection(this.criteria);
        };
        prototype.setCriteria = function(criteria) {
            this.criteria = criteria;
        };
        prototype.with = function(criterion) {
            this.criteria.add(criterion);
            return criterion;
        };
        prototype.toString = function() {
            return this.toString("");
        };
        prototype.toString = function(indentation) {
            return this.createBuilder().toString(indentation);
        };
        prototype.createBuilder = function() {
            var builder = new SearchCriterionToStringBuilder();
            builder.setCriteria(this.criteria);
            return builder;
        };
    }, {criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return AbstractCompositeSearchCriterion;
})