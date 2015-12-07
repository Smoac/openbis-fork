/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var UpdateExperimentsOperation = function() {
	};
	stjs.extend(UpdateExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.update.UpdateExperimentsOperation';
	}, {});
	return UpdateExperimentsOperation;
})