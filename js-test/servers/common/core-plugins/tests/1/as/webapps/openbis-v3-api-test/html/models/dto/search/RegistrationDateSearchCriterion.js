/**
 *  @author pkupczyk
 */
define([], function (DateFieldSearchCriterion) {
    var RegistrationDateSearchCriterion = function() {
        DateFieldSearchCriterion.call(this, "registration_date", SearchFieldType.ATTRIBUTE);
    };
    stjs.extend(RegistrationDateSearchCriterion, DateFieldSearchCriterion, [DateFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'RegistrationDateSearchCriterion';
        constructor.serialVersionUID = 1;
    }, {DATE_FORMATS: {name: "List", arguments: ["IDateFormat"]}, timeZone: "ITimeZone", fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return RegistrationDateSearchCriterion;
})