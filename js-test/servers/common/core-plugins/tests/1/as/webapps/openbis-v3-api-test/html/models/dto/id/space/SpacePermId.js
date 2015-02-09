/**
 *  Space perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectPermId", "dto/id/space/ISpaceId"], function (stjs, ObjectPermId, ISpaceId) {
    var SpacePermId = /**
     *  @param permId Space perm id, e.g. "/MY_SPACE" or "MY_SPACE".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(SpacePermId, ObjectPermId, [ObjectPermId, ISpaceId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.space.SpacePermId';
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
    return SpacePermId;
})