/**
 *  @author pkupczyk
 */
define([], function (AbstractObjectSearchCriterion) {
    var AbstractEntitySearchCriterion = function() {
        AbstractObjectSearchCriterion.call(this);
    };
    stjs.extend(AbstractEntitySearchCriterion, AbstractObjectSearchCriterion, [AbstractObjectSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'AbstractEntitySearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.operator = SearchOperator.AND;
        prototype.withOperator = function(anOperator) {
            this.operator = anOperator;
            return this;
        };
        prototype.getOperator = function() {
            return this.operator;
        };
        prototype.withCode = function() {
            return this.with(new CodeSearchCriterion());
        };
        prototype.withType = function() {
            return this.with(new EntityTypeSearchCriterion());
        };
        prototype.withPermId = function() {
            return this.with(new PermIdSearchCriterion());
        };
        prototype.withRegistrationDate = function() {
            return this.with(new RegistrationDateSearchCriterion());
        };
        prototype.withModificationDate = function() {
            return this.with(new ModificationDateSearchCriterion());
        };
        prototype.withTag = function() {
            return this.with(new TagSearchCriterion());
        };
        prototype.withProperty = function(propertyName) {
            return this.with(new StringPropertySearchCriterion(propertyName));
        };
        prototype.withDateProperty = function(propertyName) {
            return this.with(new DatePropertySearchCriterion(propertyName));
        };
        prototype.withAnyProperty = function() {
            return this.with(new AnyPropertySearchCriterion());
        };
        prototype.withAnyField = function() {
            return this.with(new AnyFieldSearchCriterion());
        };
        prototype.createBuilder = function() {
            var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
            builder.setOperator(this.operator);
            return builder;
        };
    }, {operator: {name: "Enum", arguments: ["SearchOperator"]}, criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return AbstractEntitySearchCriterion;
})