/**
 *  @author pkupczyk
 */
define([], function (DateFieldSearchCriterion) {
    var DatePropertySearchCriterion = function(fieldName) {
        DateFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
    };
    stjs.extend(DatePropertySearchCriterion, DateFieldSearchCriterion, [DateFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'DatePropertySearchCriterion';
        constructor.serialVersionUID = 1;
    }, {DATE_FORMATS: {name: "List", arguments: ["IDateFormat"]}, timeZone: "ITimeZone", fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return DatePropertySearchCriterion;
})