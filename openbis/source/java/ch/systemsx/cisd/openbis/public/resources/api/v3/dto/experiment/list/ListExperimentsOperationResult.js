/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var ListExperimentsOperationResult = function() {
	};
	stjs.extend(ListExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.list.ListExperimentsOperationResult';
	}, {});
	return ListExperimentsOperationResult;
})