define(["support/stjs"], function (stjs, FetchOptions) {
    var EmptyFetchOptions = function() {
        FetchOptions.call(this);
    };
    stjs.extend(EmptyFetchOptions, FetchOptions, [FetchOptions], function(constructor, prototype) {
        prototype['@type'] = 'EmptyFetchOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return EmptyFetchOptions;
})