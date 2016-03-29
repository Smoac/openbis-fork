/**
 * Holds information that uniquely identifies a file format type in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IFileFormatTypeId = function() {
	};
	stjs.extend(IFileFormatTypeId, null, [ IObjectId ], null, {});
	return IFileFormatTypeId;
})