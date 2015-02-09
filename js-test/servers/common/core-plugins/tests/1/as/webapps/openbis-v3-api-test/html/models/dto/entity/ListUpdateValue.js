define([ "support/stjs", "dto/entity/ListUpdateActionRemove", "dto/entity/ListUpdateActionAdd", "dto/entity/ListUpdateActionSet" ], function(stjs, ListUpdateActionRemove, ListUpdateActionAdd,
		ListUpdateActionSet) {
	var ListUpdateValue = function() {
	};
	stjs.extend(ListUpdateValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.ListUpdateValue';
		constructor.serialVersionUID = 1;
		prototype.actions = [];
		prototype.setActions = function(actions) {
			this.actions = actions;
		};
		prototype.getActions = function() {
			return this.actions;
		};
		prototype.hasActions = function() {
			return this.getActions() && this.getActions().length > 0;
		};
		prototype.remove = function(items) {
			var action = new ListUpdateActionRemove();
			action.setItems(items);
			this.actions.add(action);
		};
		prototype.add = function(items) {
			var action = new ListUpdateActionAdd();
			action.setItems(items);
			this.actions.add(action);
		};
		prototype.set = function(items) {
			var action = new ListUpdateActionSet();
			if (items == null) {
				action.setItems([]);
			} else {
				action.setItems(items);
			}
			this.actions.add(action);
		};
	}, {
		actions : {
			name : "List",
			arguments : [ {
				name : "ListUpdateValue.ListUpdateAction",
				arguments : [ "ACTION" ]
			} ]
		}
	});
	return ListUpdateValue;
})