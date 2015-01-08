/**
 *  @author pkupczyk
 */
define([], function (StringFieldSearchCriterion) {
    var AnyFieldSearchCriterion = function() {
        StringFieldSearchCriterion.call(this, "any", SearchFieldType.ANY_FIELD);
    };
    stjs.extend(AnyFieldSearchCriterion, StringFieldSearchCriterion, [StringFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'AnyFieldSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return AnyFieldSearchCriterion;
})