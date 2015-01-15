/**
 *  Holds information that uniquely identifies an entity type in openBIS.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var IEntityTypeId = function() {};
    stjs.extend(IEntityTypeId, null, [IObjectId], null, {});
    return IEntityTypeId;
})