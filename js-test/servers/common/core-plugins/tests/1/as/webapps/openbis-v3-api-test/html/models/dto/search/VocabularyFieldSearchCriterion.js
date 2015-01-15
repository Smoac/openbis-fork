define(["support/stjs"], function (stjs, AbstractFieldSearchCriterion) {
    var VocabularyFieldSearchCriterion = function(fieldName, fieldType) {
        AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
    };
    stjs.extend(VocabularyFieldSearchCriterion, AbstractFieldSearchCriterion, [AbstractFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'VocabularyFieldSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return VocabularyFieldSearchCriterion;
})