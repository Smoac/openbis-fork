/**
 *  Holds information that uniquely identifies an experiment in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IExperimentId = function() {};
    stjs.extend(IExperimentId, null, [IObjectId], null, {});
    return IExperimentId;
})