/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var ListExperimentsOperationResult = function() {
	};
	stjs.extend(ListExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.list.ListExperimentsOperationResult';
	}, {});
	return ListExperimentsOperationResult;
})