/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractCompositeSearchCriterion) {
    var AbstractObjectSearchCriterion = function() {
        AbstractCompositeSearchCriterion.call(this);
    };
    stjs.extend(AbstractObjectSearchCriterion, AbstractCompositeSearchCriterion, [AbstractCompositeSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'AbstractObjectSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.withId = function() {
            return this.with(new IdSearchCriterion());
        };
    }, {criteria: {name: "Collection", arguments: ["ISearchCriterion"]}});
    return AbstractObjectSearchCriterion;
})