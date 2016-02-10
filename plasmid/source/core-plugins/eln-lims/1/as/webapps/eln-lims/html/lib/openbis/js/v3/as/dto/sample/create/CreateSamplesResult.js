/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operations/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateSamplesResult = function(permIds) {
		this.permIds = permIds;
	};
	stjs.extend(CreateSamplesResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.create.CreateSamplesResult';
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