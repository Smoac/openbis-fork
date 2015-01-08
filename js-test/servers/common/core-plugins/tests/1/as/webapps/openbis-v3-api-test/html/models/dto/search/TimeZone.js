/**
 *  @author pkupczyk
 */
define(["dto/search/ITimeZone"], function (ITimeZone) {
    var TimeZone = function(hourOffset) {
        this.hourOffset = hourOffset;
    };
    stjs.extend(TimeZone, null, [ITimeZone], function(constructor, prototype) {
        prototype['@type'] = 'TimeZone';
        prototype.hourOffset = 0;
        prototype.getHourOffset = function() {
            return this.hourOffset;
        };
    }, {});
    return TimeZone;
})