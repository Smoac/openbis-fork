/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var Material = function() {};
    stjs.extend(Material, null, [], function(constructor, prototype) {
        prototype['@type'] = 'Material';
        constructor.serialVersionUID = 1;
    }, {});
    return Material;
})