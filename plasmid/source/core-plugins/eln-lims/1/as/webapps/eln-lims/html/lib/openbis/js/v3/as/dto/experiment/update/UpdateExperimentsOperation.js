/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var UpdateExperimentsOperation = function() {
	};
	stjs.extend(UpdateExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.update.UpdateExperimentsOperation';
	}, {});
	return UpdateExperimentsOperation;
})