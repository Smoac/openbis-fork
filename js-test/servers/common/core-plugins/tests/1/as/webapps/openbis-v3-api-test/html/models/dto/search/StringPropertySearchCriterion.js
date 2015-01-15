/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, StringFieldSearchCriterion) {
    var StringPropertySearchCriterion = function(fieldName) {
        StringFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
    };
    stjs.extend(StringPropertySearchCriterion, StringFieldSearchCriterion, [StringFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'StringPropertySearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return StringPropertySearchCriterion;
})