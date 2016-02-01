/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var UpdateExperimentsOperationResult = function() {
	};
	stjs.extend(UpdateExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.update.UpdateExperimentsOperationResult';
	}, {});
	return UpdateExperimentsOperationResult;
})