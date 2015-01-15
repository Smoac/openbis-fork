/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var DeletedObjectFetchOptions = function() {};
    stjs.extend(DeletedObjectFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'DeletedObjectFetchOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return DeletedObjectFetchOptions;
})