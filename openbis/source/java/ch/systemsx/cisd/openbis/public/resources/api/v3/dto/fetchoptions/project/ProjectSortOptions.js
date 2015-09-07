define([ "require", "stjs", "dto/fetchoptions/sort/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var ProjectSortOptions = function() {
	};
	stjs.extend(ProjectSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.project.ProjectSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ProjectSortOptions;
})