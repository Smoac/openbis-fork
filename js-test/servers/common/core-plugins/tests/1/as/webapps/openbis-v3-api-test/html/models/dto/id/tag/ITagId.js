/**
 *  @author pkupczyk
 */
define(["dto/id/IObjectId"], function (IObjectId) {
    var ITagId = function() {};
    stjs.extend(ITagId, null, [IObjectId], null, {});
    return ITagId;
})