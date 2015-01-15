/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractObjectDeletionOptions) {
    var ExperimentDeletionOptions = function() {
        AbstractObjectDeletionOptions.call(this);
    };
    stjs.extend(ExperimentDeletionOptions, AbstractObjectDeletionOptions, [AbstractObjectDeletionOptions], function(constructor, prototype) {
        prototype['@type'] = 'ExperimentDeletionOptions';
        constructor.serialVersionUID = 1;
    }, {});
    return ExperimentDeletionOptions;
})