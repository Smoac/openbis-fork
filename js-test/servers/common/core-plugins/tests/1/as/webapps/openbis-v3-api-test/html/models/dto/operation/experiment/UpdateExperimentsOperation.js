/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/operation/IOperation" ], function(stjs, IOperation) {
	var UpdateExperimentsOperation = function() {
	};
	stjs.extend(UpdateExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.experiment.UpdateExperimentsOperation';
	}, {});
	return UpdateExperimentsOperation;
})