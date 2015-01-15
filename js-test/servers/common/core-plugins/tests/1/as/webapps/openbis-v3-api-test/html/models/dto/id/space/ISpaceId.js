/**
 *  Holds information that uniquely identifies a space in openBIS.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var ISpaceId = function() {};
    stjs.extend(ISpaceId, null, [IObjectId], null, {});
    return ISpaceId;
})