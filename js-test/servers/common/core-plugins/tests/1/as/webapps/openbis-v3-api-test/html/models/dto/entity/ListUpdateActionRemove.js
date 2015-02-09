define([ "support/stjs", "dto/entity/ListUpdateAction" ], function(stjs, ListUpdateAction) {
	ListUpdateActionRemove = function() {
		ListUpdateAction.call(this);
	};
	stjs.extend(ListUpdateActionRemove, ListUpdateAction, [ ListUpdateAction ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.ListUpdateActionRemove';
		constructor.serialVersionUID = 1;
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateActionRemove;
})