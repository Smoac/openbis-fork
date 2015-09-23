/**
 * @author pkupczyk
 */
define([ "stjs", "dto/operation/IOperation" ], function(stjs, IOperation) {
	var UpdateSamplesOperation = function(updates) {
		this.updates = updates;
	};
	stjs.extend(UpdateSamplesOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.sample.UpdateSamplesOperation';
		prototype.updates = null;
		prototype.getUpdates = function() {
			return this.updates;
		};
	}, {
		updates : {
			name : "List",
			arguments : [ "SampleUpdate" ]
		}
	});
	return UpdateSamplesOperation;
})