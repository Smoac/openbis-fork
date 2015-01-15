/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, ListUpdateValue) {
    var IdListUpdateValue = function() {
        ListUpdateValue.call(this);
    };
    stjs.extend(IdListUpdateValue, ListUpdateValue, [ListUpdateValue], function(constructor, prototype) {
        prototype['@type'] = 'IdListUpdateValue';
        constructor.serialVersionUID = 1;
    }, {actions: {name: "List", arguments: [{name: "ListUpdateValue.ListUpdateAction", arguments: ["ACTION"]}]}});
    return IdListUpdateValue;
})