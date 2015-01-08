/**
 *  Holds information that uniquely identifies a deletion in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IDeletionId = function() {};
    stjs.extend(IDeletionId, null, [IObjectId], null, {});
    return IDeletionId;
})