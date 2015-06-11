/**
 *  Sample perm id.
 *  
 *  @author pkupczyk
 */
define(["stjs", "dto/id/ObjectPermId", "dto/id/sample/ISampleId"], function (stjs, ObjectPermId, ISampleId) {
    var SamplePermId = /**
     *  @param permId Sample perm id, e.g. "201108050937246-1031".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(SamplePermId, ObjectPermId, [ObjectPermId, ISampleId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.sample.SamplePermId';
        constructor.serialVersionUID = 1;
    }, {});
    return SamplePermId;
})