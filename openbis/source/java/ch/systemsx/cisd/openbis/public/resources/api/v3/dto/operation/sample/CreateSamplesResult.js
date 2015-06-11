/**
 * @author pkupczyk
 */
define([ "stjs", "dto/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateSamplesResult = function(permIds) {
		this.permIds = permIds;
	};
	stjs.extend(CreateSamplesResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.sample.CreateSamplesResult';
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
	return CreateSamplesResult;
})