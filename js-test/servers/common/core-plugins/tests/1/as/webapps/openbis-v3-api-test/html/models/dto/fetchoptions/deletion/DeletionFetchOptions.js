/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var DeletionFetchOptions = function() {};
    stjs.extend(DeletionFetchOptions, null, [], function(constructor, prototype) {
        prototype['@type'] = 'DeletionFetchOptions';
        constructor.serialVersionUID = 1;
        prototype.deletedObjects = null;
        prototype.fetchDeletedObjects = function() {
            if (this.deletedObjects == null) {
                this.deletedObjects = new DeletedObjectFetchOptions();
            }
            return this.deletedObjects;
        };
        prototype.hasDeletedObjects = function() {
            return this.deletedObjects != null;
        };
    }, {deletedObjects: "DeletedObjectFetchOptions"});
    return DeletionFetchOptions;
})