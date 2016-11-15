/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperationResult" ], function(stjs, IOperationResult) {
	var GetObjectsOperationResult = function(objectMap) {
		this.objectMap = objectMap;
	};
	stjs.extend(GetObjectsOperationResult, null, [ IOperationResult ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.get.GetObjectsOperationResult';
		prototype.objectMap = null;
		prototype.getObjectMap = function() {
			return this.objectMap;
		};
		prototype.getMessage = function() {
			return "GetObjectsOperationResult";
		};
	}, {
		objectMap : {
			name : "Map",
			arguments : [ "IObjectId", "Object" ]
		}
	});
	return GetObjectsOperationResult;
})