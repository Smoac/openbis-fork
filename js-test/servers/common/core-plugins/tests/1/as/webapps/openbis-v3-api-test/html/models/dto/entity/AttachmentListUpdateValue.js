/**
 *  @author pkupczyk
 */
define([], function (ListUpdateValue) {
    var AttachmentListUpdateValue = function() {
        ListUpdateValue.call(this);
    };
    stjs.extend(AttachmentListUpdateValue, ListUpdateValue, [ListUpdateValue], function(constructor, prototype) {
        prototype['@type'] = 'AttachmentListUpdateValue';
        constructor.serialVersionUID = 1;
    }, {actions: {name: "List", arguments: [{name: "ListUpdateValue.ListUpdateAction", arguments: ["ACTION"]}]}});
    return AttachmentListUpdateValue;
})