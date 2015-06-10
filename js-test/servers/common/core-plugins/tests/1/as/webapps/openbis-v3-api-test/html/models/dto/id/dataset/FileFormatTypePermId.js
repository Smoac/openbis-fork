/**
 *  File format type perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectIdentifier", "dto/id/dataset/IFileFormatTypeId"], function (stjs, ObjectPermId, IFileFormatTypeId) {
    var FileFormatTypePermId = /**
     *  @param permId File format type perm id, e.g. "PROPRIETARY".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(DataSetPermId, ObjectPermId, [ObjectPermId, IFileFormatTypeId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.dataset.FileFormatTypePermId';
        constructor.serialVersionUID = 1;
    }, {});
    return FileFormatTypePermId;
})