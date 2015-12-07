/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var CreateSamplesOperation = function(creations) {
		this.creations = creations;
	};
	stjs.extend(CreateSamplesOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.create.CreateSamplesOperation';
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