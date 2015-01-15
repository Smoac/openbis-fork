/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var PropertyFetchOptions = function() {};
    stjs.extend(PropertyFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'PropertyFetchOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return PropertyFetchOptions;
})