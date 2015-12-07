/**
 * Project perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/project/id/IProjectId" ], function(stjs, ObjectPermId, IProjectId) {
	/**
	 * @param permId
	 *            Project perm id, e.g. "201108050937246-1031".
	 */
	var ProjectPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(ProjectPermId, ObjectPermId, [ ObjectPermId, IProjectId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.project.id.ProjectPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return ProjectPermId;
})