define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var RoleAssignment = function() {
	};
	stjs.extend(RoleAssignment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.RoleAssignment';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.id = null;
		prototype.role = null;
		prototype.roleLevel = null;
		prototype.space = null;
		prototype.project = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
		prototype.getRole = function() {
			return this.role;
		};
		prototype.setRole = function(role) {
			this.role = role;
		};
		prototype.getRoleLevel = function() {
			return this.roleLevel;
		};
		prototype.setRoleLevel = function(roleLevel) {
			this.roleLevel = roleLevel;
		};
		prototype.getSpace = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasSpace()) {
				return this.space;
			} else {
				throw new exceptions.NotFetchedException("Space has not been fetched.");
			}
		};
		prototype.setSpace = function(space) {
			this.space = space;
		};
		prototype.getProject = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasProject()) {
				return this.project;
			} else {
				throw new exceptions.NotFetchedException("Project has not been fetched.");
			}
		};
		prototype.setProject = function(project) {
			this.project = project;
		};
	}, {
		fetchOptions : "RoleAssignmentFetchOptions",
		id : "IRoleAssignmentId",
		role : "Role",
		roleLevel : "RoleLevel",
		space : "Space",
		project : "Project"
	});
	return RoleAssignment;
})