/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractValue) {
    var AbstractNumberValue = function(number) {
        AbstractValue.call(this, number);
    };
    stjs.extend(AbstractNumberValue, AbstractValue, [AbstractValue], function(constructor, prototype) {
        prototype['@type'] = 'AbstractNumberValue';
        constructor.serialVersionUID = 1;
    }, {});
    return AbstractNumberValue;
})