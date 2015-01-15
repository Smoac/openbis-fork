/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/search/IDate"], function (stjs, AbstractValue, IDate) {
    var AbstractDateValue = function(value) {
        AbstractValue.call(this, value);
    };
    stjs.extend(AbstractDateValue, AbstractValue, [AbstractValue, IDate], function(constructor, prototype) {
        prototype['@type'] = 'AbstractDateValue';
        constructor.serialVersionUID = 1;
    }, {});
    return AbstractDateValue;
})