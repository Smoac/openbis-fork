/**
 * @author pkupczyk
 */
define([ "stjs", "dto/entity/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var ExperimentDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(ExperimentDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.deletion.experiment.ExperimentDeletionOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentDeletionOptions;
})