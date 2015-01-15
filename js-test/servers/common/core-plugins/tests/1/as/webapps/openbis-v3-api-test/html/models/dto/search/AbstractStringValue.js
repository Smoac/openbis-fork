define(["support/stjs"], function (stjs, AbstractValue) {
    var AbstractStringValue = function(value) {
        AbstractValue.call(this, value);
    };
    stjs.extend(AbstractStringValue, AbstractValue, [AbstractValue], function(constructor, prototype) {
        prototype['@type'] = 'AbstractStringValue';
        constructor.serialVersionUID = 1;
    }, {});
    return AbstractStringValue;
})