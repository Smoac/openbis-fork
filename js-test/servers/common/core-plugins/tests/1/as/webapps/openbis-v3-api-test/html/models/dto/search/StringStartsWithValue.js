define(["support/stjs"], function (stjs, AbstractStringValue) {
    var StringStartsWithValue = function(value) {
        AbstractStringValue.call(this, value);
    };
    stjs.extend(StringStartsWithValue, AbstractStringValue, [AbstractStringValue], function(constructor, prototype) {
        prototype['@type'] = 'StringStartsWithValue';
        constructor.serialVersionUID = 1;
        prototype.toString = function() {
            return "starts with '" + this.getValue() + "'";
        };
    }, {});
    return StringStartsWithValue;
})