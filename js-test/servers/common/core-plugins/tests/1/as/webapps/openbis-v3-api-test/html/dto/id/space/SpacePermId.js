/**
 *  Space perm id.
 *  
 *  @author pkupczyk
 */
var SpacePermId = function(permId) {
	this['@type'] = 'SpacePermId';
	this.permId = permId;
};

stjs.extend(SpacePermId, ObjectPermId, [ISpaceId], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.getPermId = function() {
        var permId = ObjectPermId.prototype.getPermId.call(this);
        if (permId.startsWith("/")) {
            return permId.substring(1);
        } else {
            return permId;
        }
    };
}, {});
