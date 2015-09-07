define([ "require", "stjs" ], function(require, stjs) {
	var Sorting = function(field, order) {
		this.field = field;
		this.order = order;
	};

	stjs.extend(Sorting, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.sort.Sorting';
		constructor.serialVersionUID = 1;
		prototype.getField = function() {
			return this.field;
		};
		prototype.getOrder = function() {
			return this.order;
		};
	}, {});
	return Sorting;
})