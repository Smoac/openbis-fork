/**
 *  @author pkupczyk
 */
define([], function (DateFieldSearchCriterion) {
    var ModificationDateSearchCriterion = function() {
        DateFieldSearchCriterion.call(this, "modification_date", SearchFieldType.ATTRIBUTE);
    };
    stjs.extend(ModificationDateSearchCriterion, DateFieldSearchCriterion, [DateFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'ModificationDateSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {DATE_FORMATS: {name: "List", arguments: ["IDateFormat"]}, timeZone: "ITimeZone", fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return ModificationDateSearchCriterion;
})