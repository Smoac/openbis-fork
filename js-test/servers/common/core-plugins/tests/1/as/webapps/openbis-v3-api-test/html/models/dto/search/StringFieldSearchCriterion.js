define(["support/stjs"], function (stjs, AbstractFieldSearchCriterion) {
    var StringFieldSearchCriterion = function(fieldName, fieldType) {
        AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
        this.setFieldValue(new AnyStringValue());
    };
    stjs.extend(StringFieldSearchCriterion, AbstractFieldSearchCriterion, [AbstractFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'StringFieldSearchCriterion';
        constructor.serialVersionUID = 1;
        prototype.thatEquals = function(string) {
            this.setFieldValue(new StringEqualToValue(string));
        };
        prototype.thatStartsWith = function(prefix) {
            this.setFieldValue(new StringStartsWithValue(prefix));
        };
        prototype.thatEndsWith = function(suffix) {
            this.setFieldValue(new StringEndsWithValue(suffix));
        };
        prototype.thatContains = function(string) {
            this.setFieldValue(new StringContainsValue(string));
        };
    }, {fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return StringFieldSearchCriterion;
})