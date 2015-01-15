/**
 *  Holds information that uniquely identifies an attachment in openBIS.
 *  
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var IAttachmentId = function() {};
    stjs.extend(IAttachmentId, null, [IObjectId], null, {});
    return IAttachmentId;
})