define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var HistoryEntrySortOptions = function() {
	};
	stjs.extend(HistoryEntrySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.history.HistoryEntrySortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return HistoryEntrySortOptions;
})