define([ "support/stjs" ], function(stjs) {
	ListUpdateAction = function() {
	};
	stjs.extend(ListUpdateAction, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.ListUpdateAction';
		constructor.serialVersionUID = 1;
		prototype.items = null;
		prototype.getItems = function() {
			return this.items;
		};
		prototype.setItems = function(items) {
			this.items = items;
		};
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateAction;
})