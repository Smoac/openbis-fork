define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var PersonSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(PersonSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.person.PersonSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonSortOptions;
})