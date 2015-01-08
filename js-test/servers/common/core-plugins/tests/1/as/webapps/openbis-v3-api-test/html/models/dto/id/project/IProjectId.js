/**
 *  Holds information that uniquely identifies a project in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IProjectId = function() {};
    stjs.extend(IProjectId, null, [IObjectId], null, {});
    return IProjectId;
})