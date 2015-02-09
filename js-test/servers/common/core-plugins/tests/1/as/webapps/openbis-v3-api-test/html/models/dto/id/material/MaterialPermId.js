/**
 *  Material perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectIdentifier", "dto/id/material/IMaterialId"], function (stjs, ObjectPermId, IMaterialId) {
    var MaterialPermId = /**
     *  @param permId Material perm id, e.g. "MY_MATERIAL (MY_MATERIAL_TYPE)".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(MaterialPermId, ObjectPermId, [ObjectPermId, IMaterialId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.material.MaterialPermId';
        constructor.serialVersionUID = 1;
    }, {});
    return MaterialPermId;
})