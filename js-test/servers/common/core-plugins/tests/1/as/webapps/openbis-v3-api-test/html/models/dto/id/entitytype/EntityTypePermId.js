/**
 *  Entity type perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectPermId", "dto/id/entitytype/IEntityTypeId"], function (stjs, ObjectPermId, IEntityTypeId) {
    var EntityTypePermId = /**
     *  @param permId Entity type perm id, e.g. "MY_ENTITY_TYPE".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(EntityTypePermId, ObjectPermId, [ObjectPermId, IEntityTypeId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.entitytype.EntityTypePermId';
        constructor.serialVersionUID = 1;
    }, {});
    return EntityTypePermId;
})