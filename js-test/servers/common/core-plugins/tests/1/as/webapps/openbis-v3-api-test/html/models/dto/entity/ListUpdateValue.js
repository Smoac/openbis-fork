define(["support/stjs"], function (stjs) {
    var ListUpdateValue = function() {};
    stjs.extend(ListUpdateValue, null, [], function(constructor, prototype) {
        prototype['@type'] = 'ListUpdateValue';
        constructor.serialVersionUID = 1;
        define(["support/stjs"], function (stjs) {
            constructor.ListUpdateAction = function() {};
            stjs.extend(ListUpdateValue.ListUpdateAction, null, [], function(constructor, prototype) {
                prototype['@type'] = 'ListUpdateAction';
                constructor.serialVersionUID = 1;
                prototype.items = null;
                prototype.getItems = function() {
                    return this.items;
                };
                prototype.setItems = function(items) {
                    this.items = items;
                };
            }, {items: {name: "Collection", arguments: ["T"]}});
            return ListUpdateValue.ListUpdateAction;
        })define(["support/stjs"], function (stjs, ListUpdateValue.ListUpdateAction) {
            constructor.ListUpdateActionAdd = function() {
                ListUpdateValue.ListUpdateAction.call(this);
            };
            stjs.extend(ListUpdateValue.ListUpdateActionAdd, ListUpdateValue.ListUpdateAction, [ListUpdateValue.ListUpdateAction], function(constructor, prototype) {
                prototype['@type'] = 'ListUpdateActionAdd';
                constructor.serialVersionUID = 1;
            }, {items: {name: "Collection", arguments: ["T"]}});
            return ListUpdateValue.ListUpdateActionAdd;
        })define(["support/stjs"], function (stjs, ListUpdateValue.ListUpdateAction) {
            constructor.ListUpdateActionRemove = function() {
                ListUpdateValue.ListUpdateAction.call(this);
            };
            stjs.extend(ListUpdateValue.ListUpdateActionRemove, ListUpdateValue.ListUpdateAction, [ListUpdateValue.ListUpdateAction], function(constructor, prototype) {
                prototype['@type'] = 'ListUpdateActionRemove';
                constructor.serialVersionUID = 1;
            }, {items: {name: "Collection", arguments: ["T"]}});
            return ListUpdateValue.ListUpdateActionRemove;
        })define(["support/stjs"], function (stjs, ListUpdateValue.ListUpdateAction) {
            constructor.ListUpdateActionSet = function() {
                ListUpdateValue.ListUpdateAction.call(this);
            };
            stjs.extend(ListUpdateValue.ListUpdateActionSet, ListUpdateValue.ListUpdateAction, [ListUpdateValue.ListUpdateAction], function(constructor, prototype) {
                prototype['@type'] = 'ListUpdateActionSet';
                constructor.serialVersionUID = 1;
            }, {items: {name: "Collection", arguments: ["T"]}});
            return ListUpdateValue.ListUpdateActionSet;
        })prototype.actions = new LinkedList();
        prototype.setActions = function(actions) {
            this.actions = new LinkedList(actions);
        };
        prototype.getActions = function() {
            return this.actions;
        };
        prototype.hasActions = function() {
            return this.getActions() != null && this.getActions().size() > 0;
        };
        prototype.remove = function(items) {
            var action = new ListUpdateValue.ListUpdateActionRemove();
            action.setItems(Arrays.asList(items));
            this.actions.add(action);
        };
        prototype.add = function(items) {
            var action = new ListUpdateValue.ListUpdateActionAdd();
            action.setItems(Arrays.asList(items));
            this.actions.add(action);
        };
        prototype.set = function(items) {
            var action = new ListUpdateValue.ListUpdateActionSet();
            if (items == null) {
                action.setItems(Collections.emptyList());
            } else {
                action.setItems(Arrays.asList(items));
            }
            this.actions.add(action);
        };
    }, {actions: {name: "List", arguments: [{name: "ListUpdateValue.ListUpdateAction", arguments: ["ACTION"]}]}});
    return ListUpdateValue;
})