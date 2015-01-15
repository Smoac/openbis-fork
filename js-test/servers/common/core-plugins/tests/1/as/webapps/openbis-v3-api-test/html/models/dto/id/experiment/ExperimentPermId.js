/**
 *  Experiment perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectPermId", "dto/id/experiment/IExperimentId"], function (stjs, ObjectPermId, IExperimentId) {
    var ExperimentPermId = /**
     *  @param permId Experiment perm id, e.g. "201108050937246-1031".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(ExperimentPermId, ObjectPermId, [ObjectPermId, IExperimentId], function(constructor, prototype) {
        prototype['@type'] = 'ExperimentPermId';
        constructor.serialVersionUID = 1;
    }, {});
    return ExperimentPermId;
})