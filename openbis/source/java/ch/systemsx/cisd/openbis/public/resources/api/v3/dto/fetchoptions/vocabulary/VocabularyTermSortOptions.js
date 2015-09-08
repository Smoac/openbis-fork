define([ "require", "stjs", "dto/fetchoptions/sort/SortOptions" ], function(require, stjs, SortOptions) {
	var VocabularyTermSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(VocabularyTermSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.vocabulary.VocabularyTermSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return VocabularyTermSortOptions;
})