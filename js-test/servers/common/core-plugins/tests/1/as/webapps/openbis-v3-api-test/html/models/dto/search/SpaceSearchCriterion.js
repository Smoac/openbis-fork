/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectSearchCriterion) {
    var SpaceSearchCriterion = function() {
        AbstractObjectSearchCriterion.call(this);
    };
    stjs.extend(SpaceSearchCriterion, AbstractObjectSearchCriterion, [AbstractObjectSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'SpaceSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.withCode = function() {
            return this.with(new CodeSearchCriterion());
        };
        prototype.withPermId = function() {
            return this.with(new PermIdSearchCriterion());
        };
        prototype.createBuilder = function() {
            var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
            builder.setName("SPACE");
            return builder;
        };
    }, {criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return SpaceSearchCriterion;
})