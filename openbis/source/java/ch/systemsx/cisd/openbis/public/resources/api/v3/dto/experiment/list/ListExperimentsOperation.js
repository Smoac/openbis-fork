/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var ListExperimentsOperation = function() {
	};
	stjs.extend(ListExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.list.ListExperimentsOperation';
	}, {});
	return ListExperimentsOperation;
})