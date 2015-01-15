/**
 *  Holds information that uniquely identifies a data set in openBIS.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var IDataSetId = function() {};
    stjs.extend(IDataSetId, null, [IObjectId], null, {});
    return IDataSetId;
})