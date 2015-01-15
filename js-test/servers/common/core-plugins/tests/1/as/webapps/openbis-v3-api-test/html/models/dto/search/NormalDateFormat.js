/**
 *  @author pkupczyk
 */
define(["support/stjs", "dto/search/IDateFormat"], function (stjs, IDateFormat) {
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