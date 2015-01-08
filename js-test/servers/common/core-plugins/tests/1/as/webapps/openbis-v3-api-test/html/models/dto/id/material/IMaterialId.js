/**
 *  Holds information that uniquely identifies a material in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IMaterialId = function() {};
    stjs.extend(IMaterialId, null, [IObjectId], null, {});
    return IMaterialId;
})