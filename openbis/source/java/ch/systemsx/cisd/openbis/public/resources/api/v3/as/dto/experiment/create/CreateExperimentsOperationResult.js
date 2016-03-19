/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateExperimentsOperationResult = function(permIds) {
		this.permIds = permIds;
	};
	stjs.extend(CreateExperimentsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.CreateExperimentsOperationResult';
		prototype.permIds = null;
		prototype.getPermIds = function() {
			return this.permIds;
		};
	}, {
		permIds : {
			name : "List",
			arguments : [ "ExperimentPermId" ]
		}
	});
	return CreateExperimentsOperationResult;
})