/**
 *  Holds information that uniquely identifies a material in openBIS.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var IMaterialId = function() {};
    stjs.extend(IMaterialId, null, [IObjectId], null, {});
    return IMaterialId;
})