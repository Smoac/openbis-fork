/**
 *  Project perm id.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectIdentifier", "dto/id/project/IProjectId"], function (stjs, ObjectPermId, IProjectId) {
    var ProjectPermId = /**
     *  @param permId Project perm id, e.g. "201108050937246-1031".
     */
    function(permId) {
        ObjectPermId.call(this, permId);
    };
    stjs.extend(ProjectPermId, ObjectPermId, [ObjectPermId, IProjectId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.project.ProjectPermId';
        constructor.serialVersionUID = 1;
    }, {});
    return ProjectPermId;
})