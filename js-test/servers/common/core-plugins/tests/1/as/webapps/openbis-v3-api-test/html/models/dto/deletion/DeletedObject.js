/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var DeletedObject = function() {};
    stjs.extend(DeletedObject, null, [], function(constructor, prototype) {
        prototype['@type'] = 'DeletedObject';
        prototype.id = null;
        prototype.getId = function() {
            return this.id;
        };
        prototype.setId = function(id) {
            this.id = id;
        };
    }, {id: "IObjectId"});
    return DeletedObject;
})