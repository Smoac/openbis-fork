/**
 *  Holds information that uniquely identifies a sample in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var ISampleId = function() {};
    stjs.extend(ISampleId, null, [IObjectId], null, {});
    return ISampleId;
})