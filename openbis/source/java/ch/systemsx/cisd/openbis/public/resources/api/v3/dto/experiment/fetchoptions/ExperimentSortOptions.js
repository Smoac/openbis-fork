define([ "require", "stjs", "dto/common/fetchoptions/EntityWithPropertiesSortOptions" ], function(require, stjs, EntityWithPropertiesSortOptions) {
	var ExperimentSortOptions = function() {
		EntityWithPropertiesSortOptions.call(this);
	};
	stjs.extend(ExperimentSortOptions, EntityWithPropertiesSortOptions, [ EntityWithPropertiesSortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.fetchoptions.ExperimentSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentSortOptions;
})