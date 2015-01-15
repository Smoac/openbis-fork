define(["support/stjs"], function (stjs, AbstractStringValue) {
    var StringEqualToValue = function() {
        AbstractStringValue.call(this, null);
    };
    stjs.extend(StringEqualToValue, AbstractStringValue, [AbstractStringValue], function(constructor, prototype) {
        prototype['@type'] = 'StringEqualToValue';
        constructor.serialVersionUID = 1;
        prototype.toString = function() {
            return "equal to '" + this.getValue() + "'";
        };
    }, {});
    return StringEqualToValue;
})