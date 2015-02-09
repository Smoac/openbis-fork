define([ "support/stjs", "dto/entity/ListUpdateAction" ], function(stjs, ListUpdateAction) {
	ListUpdateActionSet = function() {
		ListUpdateAction.call(this);
	};
	stjs.extend(ListUpdateActionSet, ListUpdateAction, [ ListUpdateAction ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.ListUpdateActionSet';
		constructor.serialVersionUID = 1;
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateActionSet;
})