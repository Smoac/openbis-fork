/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var ExperimentDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(ExperimentDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.experiment.ExperimentDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentDeletionOptions;
})