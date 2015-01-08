/**
 *  Holds information that uniquely identifies an attachment in openBIS.
 *  
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var IAttachmentId = function() {};
    stjs.extend(IAttachmentId, null, [IObjectId], null, {});
    return IAttachmentId;
})