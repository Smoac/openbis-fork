/**
 *  @author pkupczyk
 */
define(["dto/search/IDateFormat"], function (IDateFormat) {
    var NormalDateFormat = function() {};
    stjs.extend(NormalDateFormat, null, [IDateFormat], function(constructor, prototype) {
        prototype['@type'] = 'NormalDateFormat';
        prototype.getFormat = function() {
            return "y-M-d HH:mm";
        };
        prototype.toString = function() {
            return this.getFormat();
        };
    }, {});
    return NormalDateFormat;
})