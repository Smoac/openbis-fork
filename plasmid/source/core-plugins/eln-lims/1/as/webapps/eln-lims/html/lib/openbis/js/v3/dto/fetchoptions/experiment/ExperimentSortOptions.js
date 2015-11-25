define([ "require", "stjs", "dto/fetchoptions/sort/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var ExperimentSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(ExperimentSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.experiment.ExperimentSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentSortOptions;
})