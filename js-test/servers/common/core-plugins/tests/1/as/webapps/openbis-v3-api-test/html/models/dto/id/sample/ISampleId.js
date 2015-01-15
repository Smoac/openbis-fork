/**
 *  Holds information that uniquely identifies a sample in openBIS.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var ISampleId = function() {};
    stjs.extend(ISampleId, null, [IObjectId], null, {});
    return ISampleId;
})