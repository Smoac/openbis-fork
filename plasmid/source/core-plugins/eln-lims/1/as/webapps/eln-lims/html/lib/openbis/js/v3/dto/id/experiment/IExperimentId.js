/**
 *  Holds information that uniquely identifies an experiment in openBIS.
 *  
 *  @author pkupczyk
 */
define(["stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var IExperimentId = function() {};
    stjs.extend(IExperimentId, null, [IObjectId], null, {});
    return IExperimentId;
})