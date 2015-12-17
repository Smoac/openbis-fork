/**
 * Holds information that uniquely identifies a locator type in openBIS.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var ILocatorTypeId = function() {
	};
	stjs.extend(ILocatorTypeId, null, [ IObjectId ], null, {});
	return ILocatorTypeId;
})