/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operations/IOperation" ], function(stjs, IOperation) {
	var UpdateSamplesOperation = function(updates) {
		this.updates = updates;
	};
	stjs.extend(UpdateSamplesOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.update.UpdateSamplesOperation';
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