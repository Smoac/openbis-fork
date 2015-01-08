/**
 *  Holds information that uniquely identifies a space in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var ISpaceId = function() {};
    stjs.extend(ISpaceId, null, [IObjectId], null, {});
    return ISpaceId;
})