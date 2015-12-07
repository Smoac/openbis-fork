define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var PersonSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(PersonSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.person.fetchoptions.PersonSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonSortOptions;
})