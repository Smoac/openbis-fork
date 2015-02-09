/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var UpdateExperimentsOperationResult = function() {
	};
	stjs.extend(UpdateExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.experiment.UpdateExperimentsOperationResult';
	}, {});
	return UpdateExperimentsOperationResult;
})