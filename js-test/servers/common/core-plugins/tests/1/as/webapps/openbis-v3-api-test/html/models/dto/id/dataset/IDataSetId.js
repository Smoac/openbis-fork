/**
 *  Holds information that uniquely identifies a data set in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IDataSetId = function() {};
    stjs.extend(IDataSetId, null, [IObjectId], null, {});
    return IDataSetId;
})