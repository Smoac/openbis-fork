/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var CreateExperimentsOperation = function() {
	};
	stjs.extend(CreateExperimentsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentsOperation';
		prototype.creations = null;
		prototype.getCreations = function() {
			return this.creations;
		};
	}, {
		newExperiments : {
			name : "List",
			arguments : [ "ExperimentCreation" ]
		}
	});
	return CreateExperimentsOperation;
})