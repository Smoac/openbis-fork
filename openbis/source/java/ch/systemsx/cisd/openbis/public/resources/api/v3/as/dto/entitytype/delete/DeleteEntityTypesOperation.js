define([ "stjs", "as/dto/common/delete/DeleteObjectsOperation" ], function(stjs, DeleteObjectsOperation) {
	var DeleteEntityTypesOperation = function(objectIds, options) {
		DeleteObjectsOperation.call(this, objectIds, options);
	};
	stjs.extend(DeleteEntityTypesOperation, DeleteObjectsOperation, [ DeleteObjectsOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.delete.DeleteEntityTypesOperation';
		prototype.getMessage = function() {
			return "DeleteEntityTypesOperation";
		};
	}, {});
	return DeleteEntityTypesOperation;
})
