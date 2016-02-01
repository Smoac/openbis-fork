define([ "require", "stjs", "as/dto/common/fetchoptions/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var ProjectSortOptions = function() {
		EntitySortOptions.call(this);
	};
	stjs.extend(ProjectSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.fetchoptions.ProjectSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ProjectSortOptions;
})