/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/AbstractCompositeSearchCriteria" ], function(stjs, AbstractCompositeSearchCriteria) {
	var OnlyListableSearchCriteria = function() {
		AbstractCompositeSearchCriteria.call(this);
	};
	stjs.extend(OnlyListableSearchCriteria, AbstractCompositeSearchCriteria, [ AbstractCompositeSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.OnlyListableSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return OnlyListableSearchCriteria;
})
