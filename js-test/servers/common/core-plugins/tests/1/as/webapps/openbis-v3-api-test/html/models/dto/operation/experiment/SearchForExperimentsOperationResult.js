/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var SearchForExperimentsOperationResult = function() {
	};
	stjs.extend(SearchForExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.experiment.SearchForExperimentsOperationResult';
	}, {});
	return SearchForExperimentsOperationResult;
})