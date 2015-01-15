/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectSearchCriterion) {
    var TagSearchCriterion = function() {
        AbstractObjectSearchCriterion.call(this);
    };
    stjs.extend(TagSearchCriterion, AbstractObjectSearchCriterion, [AbstractObjectSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'TagSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.withCode = function() {
            return this.with(new CodeSearchCriterion());
        };
        prototype.withPermId = function() {
            return this.with(new PermIdSearchCriterion());
        };
        prototype.createBuilder = function() {
            var builder = AbstractCompositeSearchCriterion.prototype.createBuilder.call(this);
            builder.setName("TAG");
            return builder;
        };
    }, {criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return TagSearchCriterion;
})