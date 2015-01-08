/**
 *  @author pkupczyk
 */
define([], function (StringFieldSearchCriterion) {
    var AnyPropertySearchCriterion = function() {
        StringFieldSearchCriterion.call(this, "any", SearchFieldType.ANY_PROPERTY);
    };
    stjs.extend(AnyPropertySearchCriterion, StringFieldSearchCriterion, [StringFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'AnyPropertySearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return AnyPropertySearchCriterion;
})