/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var SearchForExperimentsOperationResult = function() {
	};
	stjs.extend(SearchForExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.search.SearchExperimentsOperationResult';
	}, {});
	return SearchForExperimentsOperationResult;
})