/**
 *  @author pkupczyk
 */
define([], function (StringFieldSearchCriterion) {
    var PermIdSearchCriterion = function() {
        StringFieldSearchCriterion.call(this, "perm id", SearchFieldType.ATTRIBUTE);
    };
    stjs.extend(PermIdSearchCriterion, StringFieldSearchCriterion, [StringFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'PermIdSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return PermIdSearchCriterion;
})