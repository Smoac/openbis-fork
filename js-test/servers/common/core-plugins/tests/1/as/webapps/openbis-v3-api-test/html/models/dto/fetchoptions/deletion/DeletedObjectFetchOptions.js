/**
 *  @author pkupczyk
 */
define([], function () {
    var DeletedObjectFetchOptions = function() {};
    stjs.extend(DeletedObjectFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'DeletedObjectFetchOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return DeletedObjectFetchOptions;
})