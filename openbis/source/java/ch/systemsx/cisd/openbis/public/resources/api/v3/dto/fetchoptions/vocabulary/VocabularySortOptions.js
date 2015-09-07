define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var VocabularySortOptions = function() {
	};
	stjs.extend(VocabularySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.vocabulary.VocabularySortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return VocabularySortOptions;
})