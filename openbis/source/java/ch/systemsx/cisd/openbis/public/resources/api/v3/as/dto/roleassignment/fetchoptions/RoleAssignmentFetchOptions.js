define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/space/fetchoptions/SpaceFetchOptions", 
         "as/dto/project/fetchoptions/ProjectFetchOptions", "as/dto/roleassignment/fetchoptions/RoleAssignmentSortOptions" ], function(require, stjs,
		FetchOptions) {
	var RoleAssignmentFetchOptions = function() {
	};
	stjs.extend(RoleAssignmentFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.space = null;
		prototype.sort = null;
		prototype.project = null;
		prototype.withSpace = function() {
			if (this.space == null) {
				var SpaceFetchOptions = require("as/dto/space/fetchoptions/SpaceFetchOptions");
				this.space = new SpaceFetchOptions();
			}
			return this.space;
		};
		prototype.withSpaceUsing = function(fetchOptions) {
			return this.space = fetchOptions;
		};
		prototype.hasSpace = function() {
			return this.space != null;
		};
		prototype.withProject = function() {
			if (this.project == null) {
				var ProjectFetchOptions = require("as/dto/project/fetchoptions/ProjectFetchOptions");
				this.project = new ProjectFetchOptions();
			}
			return this.project;
		};
		prototype.withProjectUsing = function(fetchOptions) {
			return this.project = fetchOptions;
		};
		prototype.hasProject = function() {
			return this.project != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var RoleAssignmentSortOptions = require("as/dto/roleassignment/fetchoptions/RoleAssignmentSortOptions");
				this.sort = new RoleAssignmentSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		space : "SpaceFetchOptions",
		project : "ProjectFetchOptions",
		sort : "RoleAssignmentSortOptions"
	});
	return RoleAssignmentFetchOptions;
})