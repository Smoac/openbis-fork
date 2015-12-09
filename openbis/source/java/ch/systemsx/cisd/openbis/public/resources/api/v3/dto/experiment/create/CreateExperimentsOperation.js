/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var CreateExperimentsOperation = function() {
	};
	stjs.extend(CreateExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.create.CreateExperimentsOperation';
		prototype.newExperiments = null;
	}, {
		newExperiments : {
			name : "List",
			arguments : [ "ExperimentCreation" ]
		}
	});
	return CreateExperimentsOperation;
})