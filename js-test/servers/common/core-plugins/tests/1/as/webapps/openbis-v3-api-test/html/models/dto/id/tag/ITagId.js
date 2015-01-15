/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/id/IObjectId"], function (stjs, IObjectId) {
    var ITagId = function() {};
    stjs.extend(ITagId, null, [IObjectId], null, {});
    return ITagId;
})