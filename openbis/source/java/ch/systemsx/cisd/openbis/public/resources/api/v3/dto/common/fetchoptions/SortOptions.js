define([ "require", "stjs", "dto/common/fetchoptions/SortOrder", "dto/common/fetchoptions/Sorting" ], function(require, stjs, SortOrder, Sorting) {
	var SortOptions = function() {
		this.sortings = [];
	};

	stjs.extend(SortOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.fetchoptions.SortOptions';
		constructor.serialVersionUID = 1;
		prototype.getOrCreateSorting = function(field) {
			var order = this.getSorting(field);
			if (order == null) {
				order = new SortOrder();
				this.sortings.push(new Sorting(field, order));
			}
			return order;
		};
		prototype.getSorting = function(field) {
			var order = null;
			this.sortings.forEach(function(sorting) {
				if (field == sorting.getField()) {
					order = sorting.getOrder();
				}
			});
			return order;
		};
	}, {});
	return SortOptions;
})