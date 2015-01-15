/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/search/ITimeZone"], function (stjs, ITimeZone) {
    var ServerTimeZone = function() {};
    stjs.extend(ServerTimeZone, null, [ITimeZone], null, {});
    return ServerTimeZone;
})