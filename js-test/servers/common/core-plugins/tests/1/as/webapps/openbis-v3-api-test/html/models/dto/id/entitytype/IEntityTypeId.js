/**
 *  Holds information that uniquely identifies an entity type in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IEntityTypeId = function() {};
    stjs.extend(IEntityTypeId, null, [IObjectId], null, {});
    return IEntityTypeId;
})