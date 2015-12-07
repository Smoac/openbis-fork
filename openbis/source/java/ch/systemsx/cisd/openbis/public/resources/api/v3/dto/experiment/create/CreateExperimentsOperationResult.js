/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateExperimentsOperationResult = function() {
	};
	stjs.extend(CreateExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.create.CreateExperimentsOperationResult';
		prototype.newExperimentIds = null;
	}, {
		newExperimentIds : {
			name : "List",
			arguments : [ "ExperimentPermId" ]
		}
	});
	return CreateExperimentsOperationResult;
})