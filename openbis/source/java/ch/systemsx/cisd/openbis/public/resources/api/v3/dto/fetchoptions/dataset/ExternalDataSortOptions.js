define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var ExternalDataSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(ExternalDataSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.ExternalDataSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return ExternalDataSortOptions;
})