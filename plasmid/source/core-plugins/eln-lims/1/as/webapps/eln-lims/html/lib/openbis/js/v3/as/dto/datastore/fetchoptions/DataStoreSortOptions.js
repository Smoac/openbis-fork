define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var DataStoreSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(DataStoreSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.fetchoptions.DataStoreSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DataStoreSortOptions;
})