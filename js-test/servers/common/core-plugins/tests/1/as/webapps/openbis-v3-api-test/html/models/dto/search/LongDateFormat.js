/**
 *  @author pkupczyk
 */
define(["dto/search/IDateFormat"], function (IDateFormat) {
    var LongDateFormat = function() {};
    stjs.extend(LongDateFormat, null, [IDateFormat], function(constructor, prototype) {
        prototype['@type'] = 'LongDateFormat';
        prototype.getFormat = function() {
            return "y-M-d HH:mm:ss";
        };
        prototype.toString = function() {
            return this.getFormat();
        };
    }, {});
    return LongDateFormat;
})