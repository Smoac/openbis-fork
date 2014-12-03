/**
 *  Project perm id.
 *  
 *  @author pkupczyk
 */
var ProjectPermId = function(permId) {
	this['@type'] = 'ProjectPermId';
	this.permId = permId;
};
stjs.extend(ProjectPermId, ObjectPermId, [IProjectId], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
}, {});
