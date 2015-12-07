/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var CreateSamplesResult = function(permIds) {
		this.permIds = permIds;
	};
	stjs.extend(CreateSamplesResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.create.CreateSamplesResult';
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