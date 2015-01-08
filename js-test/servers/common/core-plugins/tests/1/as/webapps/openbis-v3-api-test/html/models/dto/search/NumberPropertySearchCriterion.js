/**
 *  @author pkupczyk
 */
define([], function (NumberFieldSearchCriterion) {
    var NumberPropertySearchCriterion = function(fieldName) {
        NumberFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
    };
    stjs.extend(NumberPropertySearchCriterion, NumberFieldSearchCriterion, [NumberFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'NumberPropertySearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return NumberPropertySearchCriterion;
})