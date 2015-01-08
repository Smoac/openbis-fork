/**
 *  @author pkupczyk
 */
define(["dto/search/ITimeZone"], function (ITimeZone) {
    var ServerTimeZone = function() {};
    stjs.extend(ServerTimeZone, null, [ITimeZone], null, {});
    return ServerTimeZone;
})