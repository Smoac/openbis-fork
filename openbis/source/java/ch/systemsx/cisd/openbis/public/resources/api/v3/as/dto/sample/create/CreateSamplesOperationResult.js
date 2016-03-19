/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateSamplesOperationResult = function(permIds) {
		this.permIds = permIds;
	};
	stjs.extend(CreateSamplesOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.CreateSamplesOperationResult';
		prototype.permIds = null;
		prototype.getPermIds = function() {
			return this.permIds;
		};
	}, {
		permIds : {
			name : "List",
			arguments : [ "SamplePermId" ]
		}
	});
	return CreateSamplesOperationResult;
})