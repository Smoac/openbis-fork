define([ "require", "stjs", "dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
	var VocabularySortOptions = function() {
		SortOptions.call(this);
	};
	stjs.extend(VocabularySortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.vocabulary.fetchoptions.VocabularySortOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return VocabularySortOptions;
})