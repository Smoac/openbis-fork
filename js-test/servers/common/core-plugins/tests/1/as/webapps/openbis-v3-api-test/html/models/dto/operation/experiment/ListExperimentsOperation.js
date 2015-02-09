/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/operation/IOperation" ], function(stjs, IOperation) {
	var ListExperimentsOperation = function() {
	};
	stjs.extend(ListExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.experiment.ListExperimentsOperation';
	}, {});
	return ListExperimentsOperation;
})