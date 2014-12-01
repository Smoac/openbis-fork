/**
 *  @author pkupczyk
 */
var UnsupportedObjectIdException = function(id) {
	this['@type'] = 'UnsupportedObjectIdException';
    UserFailureException.call(this, "Unsupported object id [" + id + "] of type " + id.getClass());
};

stjs.extend(UnsupportedObjectIdException, UserFailureException, [], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
