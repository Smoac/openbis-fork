define([], function () {
    var FetchOptions = function() {};
    stjs.extend(FetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'FetchOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return FetchOptions;
})