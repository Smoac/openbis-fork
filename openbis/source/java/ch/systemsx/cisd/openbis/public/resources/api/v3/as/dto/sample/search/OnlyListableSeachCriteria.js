/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/sample/search/AbstractCompositeSearchCriteria" ], function(stjs, AbstractCompositeSearchCriteria) {
	var OnlyListableSearchCriteria = function() {
	};
	stjs.extend(AbstractCompositeSearchCriteria, OnlyListableSearchCriteria, [ OnlyListableSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.OnlyListableSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return OnlyListableSearchCriteria;
})
