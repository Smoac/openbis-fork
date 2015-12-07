/**
 * Experiment identifier.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectIdentifier", "dto/experiment/id/IExperimentId" ], function(stjs, ObjectIdentifier, IExperimentId) {
	/**
	 * @param identifier
	 *            Experiment identifier, e.g.
	 *            "/MY_SPACE/MY_PROJECT/MY_EXPERIMENT".
	 */
	var ExperimentIdentifier = function(identifier) {
		ObjectIdentifier.call(this, identifier);
	};
	stjs.extend(ExperimentIdentifier, ObjectIdentifier, [ ObjectIdentifier, IExperimentId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.id.ExperimentIdentifier';
		constructor.serialVersionUID = 1;
	}, {});
	return ExperimentIdentifier;
})