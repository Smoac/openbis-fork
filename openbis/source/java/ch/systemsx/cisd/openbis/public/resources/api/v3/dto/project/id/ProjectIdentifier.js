/**
 * Project identifier.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectIdentifier", "dto/project/id/IProjectId" ], function(stjs, ObjectIdentifier, IProjectId) {
	/**
	 * @param identifier
	 *            Project identifier, e.g. "/MY_SPACE/MY_PROJECT".
	 */
	var ProjectIdentifier = function(identifier) {
		ObjectIdentifier.call(this, identifier);
	};
	stjs.extend(ProjectIdentifier, ObjectIdentifier, [ ObjectIdentifier, IProjectId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.project.id.ProjectIdentifier';
		constructor.serialVersionUID = 1;
	}, {});
	return ProjectIdentifier;
})