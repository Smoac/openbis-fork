/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateExperimentsOperationResult = function() {
	};
	stjs.extend(CreateExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentsOperationResult';
		prototype.newExperimentIds = null;
	}, {
		newExperimentIds : {
			name : "List",
			arguments : [ "ExperimentPermId" ]
		}
	});
	return CreateExperimentsOperationResult;
})