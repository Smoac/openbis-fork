/**
 *  @author pkupczyk
 */
define(["dto/search/IDateFormat"], function (IDateFormat) {
    var ShortDateFormat = function() {};
    stjs.extend(ShortDateFormat, null, [IDateFormat], function(constructor, prototype) {
        prototype['@type'] = 'ShortDateFormat';
        prototype.getFormat = function() {
            return "y-M-d";
        };
        prototype.toString = function() {
            return this.getFormat();
        };
    }, {});
    return ShortDateFormat;
})