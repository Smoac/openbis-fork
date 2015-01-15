define(["support/stjs", "dto/search/IDate"], function (stjs, AbstractValue, IDate) {
    var AbstractDateObjectValue = function(value) {
        AbstractValue.call(this, value);
    };
    stjs.extend(AbstractDateObjectValue, AbstractValue, [AbstractValue, IDate], function(constructor, prototype) {
        prototype['@type'] = 'AbstractDateObjectValue';
        constructor.serialVersionUID = 1;
        prototype.getFormattedValue = function() {
            return null;
        };
    }, {});
    return AbstractDateObjectValue;
})