/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/sample/search/AbstractCompositeSearchCriteria" ], function(stjs, AbstractCompositeSearchCriteria) {
	var OnlyListableSeachCriteria = function() {
	};
	stjs.extend(AbstractCompositeSearchCriteria, OnlyListableSeachCriteria, [ OnlyListableSeachCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.search.OnlyListableSeachCriteria';
		constructor.serialVersionUID = 1;
	}, {});
	return OnlyListableSeachCriteria;
})
