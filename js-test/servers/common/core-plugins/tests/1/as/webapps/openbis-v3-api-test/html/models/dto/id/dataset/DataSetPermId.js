/**
 *  Data set perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectIdentifier", "dto/id/dataset/IDataSetId"], function (stjs, ObjectPermId, IDataSetId) {
    var DataSetPermId = /**
     *  @param permId Data set perm id, e.g. "201108050937246-1031".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(DataSetPermId, ObjectPermId, [ObjectPermId, IDataSetId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.dataset.DataSetPermId';
        constructor.serialVersionUID = 1;
    }, {});
    return DataSetPermId;
})