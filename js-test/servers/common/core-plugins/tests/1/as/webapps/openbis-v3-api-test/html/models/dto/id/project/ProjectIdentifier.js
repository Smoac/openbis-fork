/**
 *  Project identifier.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/ObjectIdentifier", "dto/id/project/IProjectId"], function (stjs, ObjectIdentifier, IProjectId) {
    var ProjectIdentifier = /**
     *  @param identifier Project identifier, e.g. "/MY_SPACE/MY_PROJECT".
     */
    function(identifier) {
        ObjectIdentifier.call(this, identifier);
    };
    stjs.extend(ProjectIdentifier, ObjectIdentifier, [ObjectIdentifier, IProjectId], function(constructor, prototype) {
        prototype['@type'] = 'dto.id.project.ProjectIdentifier';
        constructor.serialVersionUID = 1;
    }, {});
    return ProjectIdentifier;
})