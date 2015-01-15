/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractDateValue) {
    var DateEqualToValue = function(value) {
        AbstractDateValue.call(this, value);
    };
    stjs.extend(DateEqualToValue, AbstractDateValue, [AbstractDateValue], function(constructor, prototype) {
        prototype['@type'] = 'DateEqualToValue';
        constructor.serialVersionUID = 1;
        prototype.toString = function() {
            return "equal to '" + this.getValue() + "'";
        };
    }, {});
    return DateEqualToValue;
})