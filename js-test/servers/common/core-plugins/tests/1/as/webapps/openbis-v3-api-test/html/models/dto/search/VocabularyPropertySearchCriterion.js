/**
 *  @author pkupczyk
 */
define([], function (VocabularyFieldSearchCriterion) {
    var VocabularyPropertySearchCriterion = function(fieldName) {
        VocabularyFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
    };
    stjs.extend(VocabularyPropertySearchCriterion, VocabularyFieldSearchCriterion, [VocabularyFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'VocabularyPropertySearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return VocabularyPropertySearchCriterion;
})