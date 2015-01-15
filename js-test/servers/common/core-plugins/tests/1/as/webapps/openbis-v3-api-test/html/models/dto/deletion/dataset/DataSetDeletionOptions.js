/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectDeletionOptions) {
    var DataSetDeletionOptions = function() {
        AbstractObjectDeletionOptions.call(this);
    };
    stjs.extend(DataSetDeletionOptions, AbstractObjectDeletionOptions, [AbstractObjectDeletionOptions], function(constructor, prototype) {
        prototype['@type'] = 'DataSetDeletionOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return DataSetDeletionOptions;
})