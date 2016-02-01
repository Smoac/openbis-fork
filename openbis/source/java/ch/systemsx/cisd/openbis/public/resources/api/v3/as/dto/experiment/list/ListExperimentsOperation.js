/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var ListExperimentsOperation = function() {
	};
	stjs.extend(ListExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.list.ListExperimentsOperation';
	}, {});
	return ListExperimentsOperation;
})