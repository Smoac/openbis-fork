define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var ExperimentTypeSortOptions = function() {
	};
	stjs.extend(ExperimentTypeSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.experiment.ExperimentTypeSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentTypeSortOptions;
})