/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectSearchCriterion) {
    var EntityTypeSearchCriterion = function() {
        AbstractObjectSearchCriterion.call(this);
    };
    stjs.extend(EntityTypeSearchCriterion, AbstractObjectSearchCriterion, [AbstractObjectSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'EntityTypeSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.withCode = function() {
            return this.with(new CodeSearchCriterion());
        };
        prototype.withPermId = function() {
            return this.with(new PermIdSearchCriterion());
        };
        prototype.createBuilder = function() {
            var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
            builder.setName("TYPE");
            return builder;
        };
    }, {criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return EntityTypeSearchCriterion;
})