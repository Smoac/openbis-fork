define([ "stjs", "as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult" ], function(stjs, DeleteObjectsWithoutTrashOperationResult) {
	var DeleteEntityTypesOperationResult = function() {
		DeleteObjectsWithoutTrashOperationResult.call(this);
	};
	stjs.extend(DeleteEntityTypesOperationResult, DeleteObjectsWithoutTrashOperationResult, [ DeleteObjectsWithoutTrashOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.delete.DeleteEntityTypesOperationResult';
		prototype.getMessage = function() {
			return "DeleteEntityTypesOperationResult";
		};
	}, {});
	return DeleteEntityTypesOperationResult;
})
