/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/operation/IOperation" ], function(stjs, IOperation) {
	var CreateSamplesOperation = function(creations) {
		this.creations = creations;
	};
	stjs.extend(CreateSamplesOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.operation.sample.CreateSamplesOperation';
		prototype.creations = null;
		prototype.getCreations = function() {
			return this.creations;
		};
	}, {
		creations : {
			name : "List",
			arguments : [ "SampleCreation" ]
		}
	});
	return CreateSamplesOperation;
})