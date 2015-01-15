/**
 *  Tag perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectPermId", "dto/id/tag/ITagId"], function (stjs, ObjectPermId, ITagId) {
    var TagPermId = /**
     *  @param permId Tag perm id, e.g. "/MY_USER/MY_TAG".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(TagPermId, ObjectPermId, [ObjectPermId, ITagId], function(constructor, prototype) {
        prototype['@type'] = 'TagPermId';
        constructor.serialVersionUID = 1;
    }, {});
    return TagPermId;
})