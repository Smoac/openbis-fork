define([ "require", "stjs", "dto/fetchoptions/sort/EntitySortOptions" ], function(require, stjs, EntitySortOptions) {
	var ExperimentSortOptions = function() {
	};
	stjs.extend(ExperimentSortOptions, EntitySortOptions, [ EntitySortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.experiment.ExperimentSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentSortOptions;
})