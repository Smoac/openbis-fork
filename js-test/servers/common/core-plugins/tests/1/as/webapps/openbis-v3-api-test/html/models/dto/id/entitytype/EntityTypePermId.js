/**
 *  Entity type perm id.
 *  
 *  @author pkupczyk
 */
define(["dto/id/ObjectIdentifier", "dto/id/entitytype/IEntityTypeId"], function (ObjectPermId, IEntityTypeId) {
    var EntityTypePermId = /**
     *  @param permId Entity type perm id, e.g. "MY_ENTITY_TYPE".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(EntityTypePermId, ObjectPermId, [ObjectPermId, IEntityTypeId], function(constructor, prototype) {
        prototype['@type'] = 'EntityTypePermId';
        constructor.serialVersionUID = 1;
    }, {});
    return EntityTypePermId;
})