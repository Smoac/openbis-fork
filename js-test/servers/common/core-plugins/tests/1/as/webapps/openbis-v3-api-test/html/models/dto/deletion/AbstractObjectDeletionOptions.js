/**
 *  @author pkupczyk
 */
define([], function () {
    var AbstractObjectDeletionOptions = function() {};
    stjs.extend(AbstractObjectDeletionOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'AbstractObjectDeletionOptions';
        constructor.serialVersionUID = 1;
        prototype.reason = null;
        prototype.getReason = function() {
            return this.reason;
        };
        prototype.setReason = function(reason) {
            this.reason = reason;
        };
    }, {});
    return AbstractObjectDeletionOptions;
})