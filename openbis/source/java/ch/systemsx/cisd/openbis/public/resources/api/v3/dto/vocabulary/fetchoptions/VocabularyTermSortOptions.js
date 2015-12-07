define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var VocabularyTermSortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(VocabularyTermSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.vocabulary.fetchoptions.VocabularyTermSortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return VocabularyTermSortOptions;
})