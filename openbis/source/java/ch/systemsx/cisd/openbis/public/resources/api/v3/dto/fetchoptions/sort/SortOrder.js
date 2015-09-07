define([ "require", "stjs" ], function(require, stjs) {
	var SortOrder = function() {
		this.asc = true;
	};

	stjs.extend(SortOrder, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.sort.SortOrder';
		constructor.serialVersionUID = 1;
		prototype.asc = function() {
			this.asc = true;
		};
		prototype.desc = function() {
			this.asc = false;
		};
		prototype.isAsc = function() {
			return this.asc;
		};
	}, {});
	return SortOrder;
})