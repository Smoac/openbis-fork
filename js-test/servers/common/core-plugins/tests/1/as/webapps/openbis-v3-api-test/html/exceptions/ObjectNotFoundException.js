/**
 *  @author pkupczyk
 */
var ObjectNotFoundException = function(id) {
	this['@type'] = 'ObjectNotFoundException';
    UserFailureException.call(this, "Object with " + id.getClass().getSimpleName() + " = [" + id + "] has not been found.");
    this.objectId = id;
};

stjs.extend(ObjectNotFoundException, UserFailureException, [], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.objectId = null;
    prototype.getObjectId = function() {
        return this.objectId;
    };
}, {objectId: "IObjectId"});
