/**
 *  @author pkupczyk
 */
define([], function (StringFieldSearchCriterion) {
    var CodeSearchCriterion = function() {
        StringFieldSearchCriterion.call(this, "code", SearchFieldType.ATTRIBUTE);
    };
    stjs.extend(CodeSearchCriterion, StringFieldSearchCriterion, [StringFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'CodeSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return CodeSearchCriterion;
})