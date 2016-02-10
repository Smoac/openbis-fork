/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var SearchForExperimentsOperationResult = function() {
	};
	stjs.extend(SearchForExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.search.SearchExperimentsOperationResult';
	}, {});
	return SearchForExperimentsOperationResult;
})