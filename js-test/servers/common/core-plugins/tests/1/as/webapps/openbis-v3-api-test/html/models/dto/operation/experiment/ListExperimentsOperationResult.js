/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var ListExperimentsOperationResult = function() {
	};
	stjs.extend(ListExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.experiment.ListExperimentsOperationResult';
	}, {});
	return ListExperimentsOperationResult;
})