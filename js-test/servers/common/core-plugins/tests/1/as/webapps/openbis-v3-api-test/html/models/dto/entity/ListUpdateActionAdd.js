define([ "support/stjs", "dto/entity/ListUpdateAction" ], function(stjs, ListUpdateAction) {
	ListUpdateActionAdd = function() {
		ListUpdateAction.call(this);
	};
	stjs.extend(ListUpdateActionAdd, ListUpdateAction, [ ListUpdateAction ], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.ListUpdateActionAdd';
		constructor.serialVersionUID = 1;
	}, {
		items : {
			name : "Collection",
			arguments : [ "T" ]
		}
	});
	return ListUpdateActionAdd;
})