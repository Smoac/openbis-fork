/**
 *  @author pkupczyk
 */
var UnauthorizedObjectAccessException = function(id) {
	this['@type'] = 'UnauthorizedObjectAccessException';
    UserFailureException.call(this, "Access denied to object with " + id.getClass().getSimpleName() + " = [" + id + "].");
    this.objectId = id;
};

stjs.extend(UnauthorizedObjectAccessException, UserFailureException, [], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.objectId = null;
    prototype.getObjectId = function() {
        return this.objectId;
    };
}, {objectId: "IObjectId"});
