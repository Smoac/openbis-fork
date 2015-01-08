/**
 *  Project identifier.
 *  
 *  @author pkupczyk
 */
define(["dto/id/ObjectPermId", "dto/id/project/IProjectId"], function (ObjectIdentifier, IProjectId) {
    var ProjectIdentifier = /**
     *  @param identifier Project identifier, e.g. "/MY_SPACE/MY_PROJECT".
     */
    function(identifier) {
        ObjectIdentifier.call(this, identifier);
    };
    stjs.extend(ProjectIdentifier, ObjectIdentifier, [ObjectIdentifier, IProjectId], function(constructor, prototype) {
        prototype['@type'] = 'ProjectIdentifier';
        constructor.serialVersionUID = 1;
    }, {});
    return ProjectIdentifier;
})